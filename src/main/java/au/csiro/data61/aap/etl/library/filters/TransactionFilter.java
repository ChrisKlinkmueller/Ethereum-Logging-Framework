package au.csiro.data61.aap.etl.library.filters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import au.csiro.data61.aap.etl.core.EtlException;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.ValueAccessor;
import au.csiro.data61.aap.rpc.EthereumTransaction;

/**
 * TransactionScope
 */
public class TransactionFilter extends Filter {
    private ValueAccessor senders;
    private ValueAccessor recipients;

    public TransactionFilter(ValueAccessor senders, ValueAccessor recipients, Instruction... instructions) {
        this(senders, recipients, Arrays.asList(instructions));
    }

    public TransactionFilter(ValueAccessor senders, ValueAccessor recipients, List<Instruction> instructions) {
        super(instructions);
        assert senders != null;
        assert recipients != null;
        this.recipients = recipients;
        this.senders = senders;
    }

    public void execute(ProgramState state) throws EtlException {
        final Predicate<String> senderFilter = this.createAddressFilter(state, this.senders);
        final Predicate<String> recipientFilter = this.createAddressFilter(state, this.recipients);

        for (EthereumTransaction tx : state.getDataSource().getCurrentBlock()) {
            if (senderFilter.test(tx.getFrom()) && recipientFilter.test(tx.getTo())) {
                try {
                    state.getDataSource().setCurrentTransaction(tx);
                    this.executeInstructions(state);
                }
                catch (Throwable cause) {
                    final String message = String.format("Error mapping transaction '%s' in block '%s'.", tx.getTransactionIndex(), tx.getBlockNumber());
                    final boolean abort = state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, cause);
                    if (abort) {
                        throw new EtlException(message, cause);
                    }
                }
                finally {
                    state.getDataSource().setCurrentTransaction(null);
                }
            }
        }
    }

    private Predicate<String> createAddressFilter(ProgramState state, ValueAccessor addresses) throws EtlException {
        final Object addressList = addresses.getValue(state);
        if (addressList == null) {
            return address -> true;
        }
        else if (addressList instanceof String) {
            final String expectedAddress = (String)addressList;
            return address -> address != null && address.equals(expectedAddress);
        }
        else if (List.class.isAssignableFrom(addressList.getClass())) {
            try {
                @SuppressWarnings("unchecked")
                final Set<String> expectedAddresses = new HashSet<String>((List<String>)addressList);
                if (expectedAddresses.isEmpty()) {
                    return address -> true;
                }
                else {
                    return address -> expectedAddresses.contains(address);
                }
            }
            catch (ClassCastException ex) {
                final String message = "Cannot convert address list.";
                throw new EtlException(message, ex);
            }
        }
        else {
            throw new EtlException(String.format("Value of type '%s' for addresslist not supported.", addressList.getClass()));
        }
    }
}
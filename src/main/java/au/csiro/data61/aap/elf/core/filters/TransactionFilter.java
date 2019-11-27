package au.csiro.data61.aap.elf.core.filters;

import java.util.Arrays;
import java.util.List;

import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.readers.EthereumTransaction;

/**
 * TransactionScope
 */
public class TransactionFilter extends Filter {
    private FilterPredicate<String> senderCriterion;
    private FilterPredicate<String> recipientCiterion;

    public TransactionFilter(FilterPredicate<String> senderCriterion, FilterPredicate<String> recipientCiterion, Instruction... instructions) {
        this(senderCriterion, recipientCiterion, Arrays.asList(instructions));
    }

    public TransactionFilter(FilterPredicate<String> senderCriterion, FilterPredicate<String> recipientCiterion, List<Instruction> instructions) {
        super(instructions);
        assert senderCriterion != null;
        assert recipientCiterion != null;
        this.recipientCiterion = recipientCiterion;
        this.senderCriterion = senderCriterion;
    }

    public void execute(ProgramState state) throws ProgramException {
        for (EthereumTransaction tx : state.getReader().getCurrentBlock()) {
            if (this.senderCriterion.test(state, tx.getFrom()) && this.recipientCiterion.test(state, tx.getTo())) {
                try {
                    state.getReader().setCurrentTransaction(tx);
                    this.executeInstructions(state);
                }
                catch (Throwable cause) {
                    final String message = String.format("Error mapping transaction '%s' in block '%s'.", tx.getTransactionIndex(), tx.getBlockNumber());
                    final boolean abort = state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, cause);
                    if (abort) {
                        throw new ProgramException(message, cause);
                    }
                }
                finally {
                    state.getReader().setCurrentTransaction(null);
                }
            }
        }
    }
}
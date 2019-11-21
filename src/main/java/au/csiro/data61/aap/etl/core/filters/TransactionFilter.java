package au.csiro.data61.aap.etl.core.filters;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.readers.EthereumTransaction;

/**
 * TransactionScope
 */
public class TransactionFilter extends Filter {
    private BiPredicate<ProgramState, String> senderCriterion;
    private BiPredicate<ProgramState, String> recipientCiterion;

    public TransactionFilter(BiPredicate<ProgramState, String> senderCriterion, BiPredicate<ProgramState, String> recipientCiterion, Instruction... instructions) {
        this(senderCriterion, recipientCiterion, Arrays.asList(instructions));
    }

    public TransactionFilter(BiPredicate<ProgramState, String> senderCriterion, BiPredicate<ProgramState, String> recipientCiterion, List<Instruction> instructions) {
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
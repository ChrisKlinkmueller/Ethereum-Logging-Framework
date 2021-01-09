package blf.core.filters;

import java.util.Arrays;
import java.util.List;

import blf.core.Instruction;
import blf.core.ProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.readers.EthereumTransaction;
import io.reactivex.annotations.NonNull;

/**
 * TransactionScope
 */
public class TransactionFilter extends Filter {
    private final FilterPredicate<String> senderCriterion;
    private final FilterPredicate<String> recipientCiterion;

    public TransactionFilter(
        FilterPredicate<String> senderCriterion,
        FilterPredicate<String> recipientCiterion,
        Instruction... instructions
    ) {
        this(senderCriterion, recipientCiterion, Arrays.asList(instructions));
    }

    public TransactionFilter(
        @NonNull FilterPredicate<String> senderCriterion,
        @NonNull FilterPredicate<String> recipientCiterion,
        List<Instruction> instructions
    ) {
        super(instructions);
        this.recipientCiterion = recipientCiterion;
        this.senderCriterion = senderCriterion;
    }

    public void execute(ProgramState state) throws ProgramException {
        for (EthereumTransaction tx : state.getReader().getCurrentBlock()) {
            if (this.senderCriterion.test(state, tx.getFrom()) && this.recipientCiterion.test(state, tx.getTo())) {
                try {
                    state.getReader().setCurrentTransaction(tx);
                    this.executeInstructions(state);
                } catch (Exception cause) {
                    final String message = String.format(
                        "Error mapping transaction '%s' in block '%s'.",
                        tx.getTransactionIndex(),
                        tx.getBlockNumber()
                    );
                    final boolean abort = state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, cause);
                    if (abort) {
                        throw new ProgramException(message, cause);
                    }
                } finally {
                    state.getReader().setCurrentTransaction(null);
                }
            }
        }
    }
}

package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.reader.EthereumDataReader;
import blf.blockchains.ethereum.reader.EthereumTransaction;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.instructions.Instruction;
import blf.core.interfaces.FilterPredicate;
import blf.core.state.ProgramState;
import io.reactivex.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 * TransactionScope
 */
public class EthereumTransactionFilterInstruction extends Instruction {
    private final FilterPredicate<String> senderCriterion;
    private final FilterPredicate<String> recipientCriterion;

    public EthereumTransactionFilterInstruction(
        FilterPredicate<String> senderCriterion,
        FilterPredicate<String> recipientCriterion,
        Instruction... instructions
    ) {
        this(senderCriterion, recipientCriterion, Arrays.asList(instructions));
    }

    public EthereumTransactionFilterInstruction(
        @NonNull FilterPredicate<String> senderCriterion,
        @NonNull FilterPredicate<String> recipientCriterion,
        List<Instruction> instructions
    ) {
        super(instructions);
        this.recipientCriterion = recipientCriterion;
        this.senderCriterion = senderCriterion;
    }

    @Override
    public void execute(ProgramState state) {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;
        final EthereumDataReader ethereumReader = ethereumProgramState.getReader();

        for (EthereumTransaction tx : ethereumReader.getCurrentBlock()) {
            try {
                if (this.senderCriterion.test(state, tx.getFrom()) && this.recipientCriterion.test(state, tx.getTo())) {
                    try {
                        ethereumReader.setCurrentTransaction(tx);
                        this.executeNestedInstructions(state);
                    } catch (Exception cause) {
                        final String message = String.format(
                            "Error mapping transaction '%s' in block '%s'.",
                            tx.getTransactionIndex(),
                            tx.getBlockNumber()
                        );
                        state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, cause);
                    } finally {
                        ethereumReader.setCurrentTransaction(null);
                    }
                }
            } catch (ProgramException e) {
                // TODO: remove throw of ProgramException
                state.getExceptionHandler().handleExceptionAndDecideOnAbort(e.getMessage(), e);
            }
        }
    }
}

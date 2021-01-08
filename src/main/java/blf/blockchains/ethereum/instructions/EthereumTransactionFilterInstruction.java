package blf.blockchains.ethereum.instructions;

import java.util.Arrays;
import java.util.List;

import blf.blockchains.ethereum.reader.EthereumDataReader;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.state.ProgramState;
import blf.core.interfaces.Instruction;
import blf.core.exceptions.ProgramException;
import blf.blockchains.ethereum.reader.EthereumTransaction;
import blf.core.instructions.FilterInstruction;
import blf.core.interfaces.FilterPredicate;
import io.reactivex.annotations.NonNull;

/**
 * TransactionScope
 */
public class EthereumTransactionFilterInstruction extends FilterInstruction {
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
    public void execute(ProgramState state) throws ProgramException {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;
        final EthereumDataReader ethereumReader = ethereumProgramState.getReader();

        for (EthereumTransaction tx : ethereumReader.getCurrentBlock()) {
            if (this.senderCriterion.test(state, tx.getFrom()) && this.recipientCriterion.test(state, tx.getTo())) {
                try {
                    ethereumReader.setCurrentTransaction(tx);
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
                    ethereumReader.setCurrentTransaction(null);
                }
            }
        }
    }
}

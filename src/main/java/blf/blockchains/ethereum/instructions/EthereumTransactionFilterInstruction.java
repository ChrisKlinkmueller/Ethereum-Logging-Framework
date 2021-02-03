package blf.blockchains.ethereum.instructions;

import blf.blockchains.ethereum.reader.EthereumDataReader;
import blf.blockchains.ethereum.reader.EthereumTransaction;
import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.instructions.Instruction;
import blf.core.interfaces.FilterPredicate;
import blf.core.state.ProgramState;

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
        FilterPredicate<String> senderCriterion,
        FilterPredicate<String> recipientCriterion,
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
            if (this.senderCriterion.test(state, tx.getFrom()) && this.recipientCriterion.test(state, tx.getTo())) {
                ethereumReader.setCurrentTransaction(tx);

                this.executeNestedInstructions(state);

                ethereumReader.setCurrentTransaction(null);
            }
        }
    }
}

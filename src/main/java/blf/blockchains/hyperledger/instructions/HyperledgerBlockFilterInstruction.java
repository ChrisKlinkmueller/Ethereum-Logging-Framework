package blf.blockchains.hyperledger.instructions;

import blf.blockchains.hyperledger.helpers.HyperledgerListenerHelper;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.instructions.FilterInstruction;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;

import blf.core.values.ValueStore;
import blf.grammar.BcqlParser;
import org.antlr.v4.runtime.misc.Pair;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.gateway.Network;

import java.math.BigInteger;
import java.util.List;
import java.util.logging.Logger;
import java.lang.InterruptedException;

import static blf.blockchains.hyperledger.variables.HyperledgerBlockVariables.*;

/**
 * This class handles the 'BLOCKS (fromBlock) (toBlock)' filter of the .bcql file.
 */
public class HyperledgerBlockFilterInstruction extends FilterInstruction {

    private final BcqlParser.BlockFilterContext blockCtx;

    private final Logger logger;
    private final ExceptionHandler exceptionHandler;

    public HyperledgerBlockFilterInstruction(BcqlParser.BlockFilterContext blockCtx, List<Instruction> nestedInstructions) {
        // here the list of nested instructions is created
        super(nestedInstructions);

        this.blockCtx = blockCtx;
        this.logger = Logger.getLogger(HyperledgerBlockFilterInstruction.class.getName());
        this.exceptionHandler = new ExceptionHandler();
    }

    public void execute(final ProgramState state) throws ProgramException {

        final HyperledgerProgramState hyperledgerProgramState = (HyperledgerProgramState) state;
        final ValueStore valueStore = hyperledgerProgramState.getValueStore();

        final Pair<BigInteger, BigInteger> pairOfBlockNumbers = HyperledgerListenerHelper.parseBlockFilterCtx(
            hyperledgerProgramState,
            this.blockCtx
        );

        final BigInteger fromBlockNumber = pairOfBlockNumbers.a;
        final BigInteger toBlockNumber = pairOfBlockNumbers.b;

        Network network = hyperledgerProgramState.getNetwork();

        network.addBlockListener(fromBlockNumber.longValue(), (BlockEvent blockEvent) -> {
            BigInteger currentBlockNumber = BigInteger.valueOf(blockEvent.getBlockNumber());
            // If the currentBlockNumber is greater than the toBlockNumber, we want to stop
            if (currentBlockNumber.compareTo(toBlockNumber) > 0) {
                synchronized (network) {
                    network.notifyAll();
                }
            } else {
                valueStore.setValue(BLOCK_NUMBER, BigInteger.valueOf(blockEvent.getBlockNumber()));
                valueStore.setValue(BLOCK_HASH, BigInteger.valueOf(blockEvent.hashCode()));
                valueStore.setValue(BLOCK_TRANSACTION_COUNT, BigInteger.valueOf(blockEvent.getTransactionCount()));

                hyperledgerProgramState.setCurrentBlockNumber(currentBlockNumber);

                String infoMsg = currentBlockNumber.toString();
                this.logger.info("Extracting block number: " + infoMsg);
                hyperledgerProgramState.setCurrentBlock(blockEvent);

                try {
                    this.executeInstructions(hyperledgerProgramState);
                } catch (ProgramException err) {
                    String errorMsg = "Unable to execute instructions";
                    exceptionHandler.handleExceptionAndDecideOnAbort(errorMsg, err);
                }

                if (currentBlockNumber.compareTo(toBlockNumber) == 0) {
                    synchronized (network) {
                        network.notifyAll();
                    }
                }
            }
        });
        synchronized (network) {
            try {
                network.wait();
            } catch (InterruptedException err) {
                String errorMsg = "Failed when iterating over blocks.";
                exceptionHandler.handleExceptionAndDecideOnAbort(errorMsg, err);
            }
        }
    }

}

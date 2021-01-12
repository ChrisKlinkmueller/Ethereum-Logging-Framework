package blf.blockchains.ethereum.instructions;

import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;

import blf.blockchains.ethereum.state.EthereumProgramState;
import blf.core.exceptions.ProgramException;
import blf.core.interfaces.Instruction;
import blf.core.state.ProgramState;
import blf.core.instructions.FilterInstruction;
import blf.core.interfaces.FilterPredicate;
import blf.core.values.ValueAccessor;
import blf.blockchains.ethereum.reader.EthereumBlock;
import io.reactivex.annotations.NonNull;

/**
 * This class handles the `BLOCKS (fromBlock) (toBlock)` filter of the .bcql file.
 */
public class EthereumBlockFilterInstruction extends FilterInstruction {
    private static final Logger LOGGER = Logger.getLogger(EthereumBlockFilterInstruction.class.getName());
    // TODO (by Mykola Digtiar): move this constant into Constants.java
    private static final int BLOCK_QUERY_DELAY_MILLISECONDS = 3000;

    private final ValueAccessor fromBlock;
    private final FilterPredicate<BigInteger> stopCriteria;

    public EthereumBlockFilterInstruction(
        @NonNull final ValueAccessor fromBlock,
        @NonNull FilterPredicate<BigInteger> stopCriteria,
        @NonNull List<Instruction> instructions
    ) {
        super(instructions);
        this.fromBlock = fromBlock;
        this.stopCriteria = stopCriteria;
    }

    /**
     * This method iterates through the blocks in the specified range and
     * also waits for the block
     * (by calling the {@link #waitUntilBlockExists(EthereumProgramState, BigInteger) waitForBlockExistence} method)
     * in case that the specified block number in the range is not on the Blockchain yet.
     * To retrieve the block numbers on the blockchain the {@link ProgramState ProgramState} readers and writers are used.
     *
     * @param state the current state of the program
     * @throws ProgramException this exception seems to be never thrown and should be removed
     * @see ProgramState
     */
    public void execute(final ProgramState state) throws ProgramException {
        final EthereumProgramState ethereumProgramState = (EthereumProgramState) state;

        BigInteger currentBlock = (BigInteger) fromBlock.getValue(ethereumProgramState);

        while (!this.stopCriteria.test(ethereumProgramState, currentBlock)) {
            try {
                this.waitUntilBlockExists(ethereumProgramState, currentBlock);

                final EthereumBlock block = ethereumProgramState.getReader().getClient().queryBlockData(currentBlock);

                final String blockProcessingStartMessage = String.format("Processing of block %s started", currentBlock);
                final String blockProcessingFinishMessage = String.format("Processing of block %s finished", currentBlock);

                LOGGER.info(blockProcessingStartMessage);
                ethereumProgramState.getWriters().startNewBlock(currentBlock);

                ethereumProgramState.getReader().setCurrentBlock(block);
                this.executeInstructions(ethereumProgramState);

                ethereumProgramState.getWriters().writeBlock();
                LOGGER.info(blockProcessingFinishMessage);

            } catch (final Throwable throwable) {
                // TODO (by Mykola Digtiar): handle this exception inside the method that throws it
                final String message = String.format("Error when processing block number '%s'", currentBlock.toString());
                ethereumProgramState.getExceptionHandler().handleExceptionAndDecideOnAbort(message, throwable);
            } finally {
                ethereumProgramState.getReader().setCurrentBlock(null);
            }

            currentBlock = currentBlock.add(BigInteger.ONE);
        }
    }

    /**
     *
     * This methods queries the most actual block number of the blockchain
     * and waits until the up-to-date block number is >= then the {@param expectedBlockNumber}.
     *
     * @param ethereumProgramState current {@link EthereumProgramState ethereumProgramState of the program}
     * @param expectedBlockNumber number of the block to be waited for
     */
    private void waitUntilBlockExists(final EthereumProgramState ethereumProgramState, final BigInteger expectedBlockNumber) {

        Timer timer = new Timer();
        // Poll for the up-to-date block number and stop timer if up-to-date block number >= expectedBlockNumber
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                boolean newBlockAvailable = false;
                try {
                    // queryBlockNumber().compareTo(expectedBlockNumber) is >= 0 iff queryBlockNumber() >= expectedBlockNumber
                    newBlockAvailable = ethereumProgramState.getReader().getClient().queryBlockNumber().compareTo(expectedBlockNumber) >= 0;
                } catch (Throwable throwable) {
                    // TODO (by Mykola Digtiar): the exception should be handled in queryBlockNumber() method

                    final String queryBlockNumberErrorMessage = String.format(
                        "An error occurred while querying the block with number '%s'",
                        expectedBlockNumber.toString()
                    );

                    ethereumProgramState.getExceptionHandler().handleExceptionAndDecideOnAbort(queryBlockNumberErrorMessage, throwable);
                }

                if (newBlockAvailable) {
                    timer.cancel();
                }
            }
        }, 0, BLOCK_QUERY_DELAY_MILLISECONDS);
    }

}

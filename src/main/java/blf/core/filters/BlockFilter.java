package blf.core.filters;

import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;

import blf.core.exceptions.ProgramException;
import blf.core.Instruction;
import blf.core.ProgramState;
import blf.core.values.ValueAccessor;
import blf.core.readers.EthereumBlock;
import io.reactivex.annotations.NonNull;

/**
 * This class handles the `BLOCKS (fromBlock) (toBlock)` filter of the .bcql file.
 */
public class BlockFilter extends Filter {
    private static final Logger LOGGER = Logger.getLogger(BlockFilter.class.getName());
    // TODO (by Mykola Digtiar): move this constant into Constants.java
    private static final int BLOCK_QUERY_DELAY_MILLISECONDS = 3000;

    private final ValueAccessor fromBlock;
    private final FilterPredicate<BigInteger> stopCriteria;

    public BlockFilter(
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
     * (by calling the {@link #waitUntilBlockExists(ProgramState, BigInteger) waitForBlockExistence} method)
     * in case that the specified block number in the range is not on the Blockchain yet.
     * To retrieve the block numbers on the blockchain the {@link ProgramState ProgramState} readers and writers are used.
     *
     * @param state the current state of the program
     * @throws ProgramException this exception seems to be never thrown and should be removed
     * @see ProgramState
     */
    public void execute(final ProgramState state) throws ProgramException {

        BigInteger currentBlock = (BigInteger) fromBlock.getValue(state);

        while (!this.stopCriteria.test(state, currentBlock)) {
            try {
                this.waitUntilBlockExists(state, currentBlock);

                final EthereumBlock block = state.getReader().getClient().queryBlockData(currentBlock);

                final String blockProcessingStartMessage = String.format("Processing of block %s started", currentBlock);
                final String blockProcessingFinishMessage = String.format("Processing of block %s finished", currentBlock);

                LOGGER.info(blockProcessingStartMessage);
                state.getWriters().startNewBlock(currentBlock);

                state.getReader().setCurrentBlock(block);
                this.executeInstructions(state);

                state.getWriters().writeBlock();
                LOGGER.info(blockProcessingFinishMessage);

            } catch (final Throwable throwable) {
                // TODO (by Mykola Digtiar): handle this exception inside the method that throws it
                final String message = String.format("Error when processing block number '%s'", currentBlock.toString());
                state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, throwable);
            } finally {
                state.getReader().setCurrentBlock(null);
            }

            currentBlock = currentBlock.add(BigInteger.ONE);
        }
    }

    /**
     *
     * This methods queries the most actual block number of the blockchain
     * and waits until the up-to-date block number is >= then the {@param expectedBlockNumber}.
     *
     * @param state current {@link ProgramState state of the program}
     * @param expectedBlockNumber number of the block to be waited for
     */
    private void waitUntilBlockExists(final ProgramState state, final BigInteger expectedBlockNumber) {

        Timer timer = new Timer();
        // Poll for the up-to-date block number and stop timer if up-to-date block number >= expectedBlockNumber
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                boolean newBlockAvailable = false;
                try {
                    // queryBlockNumber().compareTo(expectedBlockNumber) is >= 0 iff queryBlockNumber() >= expectedBlockNumber
                    newBlockAvailable = state.getReader().getClient().queryBlockNumber().compareTo(expectedBlockNumber) >= 0;
                } catch (Throwable throwable) {
                    // TODO (by Mykola Digtiar): the exception should be handled in queryBlockNumber() method

                    final String queryBlockNumberErrorMessage = String.format(
                        "An error occurred while querying the block with number '%s'",
                        expectedBlockNumber.toString()
                    );

                    state.getExceptionHandler().handleExceptionAndDecideOnAbort(queryBlockNumberErrorMessage, throwable);
                }

                if (newBlockAvailable) {
                    timer.cancel();
                }
            }
        }, 0, BLOCK_QUERY_DELAY_MILLISECONDS);
    }

}

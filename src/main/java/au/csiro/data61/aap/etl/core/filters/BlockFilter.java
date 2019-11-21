package au.csiro.data61.aap.etl.core.filters;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;
import au.csiro.data61.aap.etl.core.readers.EthereumBlock;

/**
 * BlockRange
 */
public class BlockFilter extends Filter {
    private static final int KNOWN_BLOCKS_LENGTH = 30;
    private final ValueAccessor fromBlock;
    private final BiPredicate<ProgramState, BigInteger> stopCriteria;

    public BlockFilter(final ValueAccessor fromBlock, BiPredicate<ProgramState, BigInteger> stopCriteria, final Instruction... instructions) {
        this(fromBlock, stopCriteria, Arrays.asList(instructions));
    }

    public BlockFilter(final ValueAccessor fromBlock, final BiPredicate<ProgramState, BigInteger> stopCriteria, final List<Instruction> instructions) {
        super(instructions);
        assert fromBlock != null;
        assert stopCriteria != null;
        assert instructions != null && instructions.stream().allMatch(Objects::nonNull);
        this.fromBlock = fromBlock;
        this.stopCriteria = stopCriteria;
    }

    public void execute(final ProgramState state) throws ProgramException {
        final LinkedList<EthereumBlock> knownBlocks = new LinkedList<>();
        final BigInteger startBlock = (BigInteger)fromBlock.getValue(state);
        BigInteger currentBlock = startBlock;
        while (!this.stopCriteria.test(state, currentBlock)) {
            try {
                this.waitForBlockExistence(state, currentBlock);

                final EthereumBlock block = queryConfirmedBlock(state, currentBlock, knownBlocks);
                if (!block.getNumber().equals(currentBlock)) {
                    currentBlock = block.getNumber();
                }

                state.getWriters().startNewBlock(currentBlock);
                state.getReader().setCurrentBlock(block);

                this.executeInstructions(state);
                state.getWriters().writeBlock();

            } catch (final Throwable throwable) {
                final String message = String.format("Error when processing block number '%s'.", currentBlock.toString());
                final boolean abort = state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, throwable);
                if (abort) {
                    return;
                }
            } finally {
                state.getReader().setCurrentBlock(null);
            }

            currentBlock = currentBlock.add(BigInteger.ONE);
        }
    }

    private void waitForBlockExistence(final ProgramState state, final BigInteger currentBlock)
            throws Throwable, InterruptedException {
        while (state.getReader().getClient().queryBlockNumber().compareTo(currentBlock) < 0) {
            Thread.sleep(3000);
        }
    }

    private static EthereumBlock queryConfirmedBlock(final ProgramState state, final BigInteger currentBlock,
            final LinkedList<EthereumBlock> knownBlocks) throws Throwable {
        BigInteger queryBlockNumber = currentBlock;
        do {
            final EthereumBlock block = state.getReader().getClient().queryBlockData(queryBlockNumber);
            if (knownBlocks.isEmpty() || knownBlocks.getLast().getHash().equals(block.getParentHash())) {
                appendBlock(knownBlocks, block);
                return block;
            }

            queryBlockNumber = queryBlockNumber.subtract(BigInteger.ONE);
            knownBlocks.removeLast();
        } while (true);
    }

    public static void appendBlock(final LinkedList<EthereumBlock> knownBlocks, final EthereumBlock block) {
        knownBlocks.addLast(block);
        if (KNOWN_BLOCKS_LENGTH < knownBlocks.size()) {
            knownBlocks.removeFirst();
        }
    }
}
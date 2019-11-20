package au.csiro.data61.aap.etl.library.filters;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import au.csiro.data61.aap.etl.core.EtlException;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.ValueAccessor;
import au.csiro.data61.aap.rpc.EthereumBlock;

/**
 * BlockRange
 */
public class BlockRangeFilter extends Filter {
    private static final int KNOWN_BLOCKS_LENGTH = 30;
    private final ValueAccessor fromBlock;
    private final ValueAccessor toBlock;

    public BlockRangeFilter(final ValueAccessor fromBlock, final ValueAccessor toBlock, final Instruction... instructions) {
        this(fromBlock, toBlock, Arrays.asList(instructions));
    }

    public BlockRangeFilter(final ValueAccessor fromBlock, final ValueAccessor toBlock, final List<Instruction> instructions) {
        super(instructions);
        assert fromBlock != null;
        assert toBlock != null;
        assert instructions != null && instructions.stream().allMatch(Objects::nonNull);
        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
    }

    public void execute(final ProgramState state) throws EtlException {
        final LinkedList<EthereumBlock> knownBlocks = new LinkedList<>();
        final BigInteger startBlock = (BigInteger) fromBlock.getValue(state);
        final BigInteger stopBlock = (BigInteger) toBlock.getValue(state);
        BigInteger currentBlock = startBlock;
        while (currentBlock.compareTo(stopBlock) <= 0) {
            try {
                this.waitForBlockExistence(state, currentBlock);

                final EthereumBlock block = queryConfirmedBlock(state, currentBlock, knownBlocks);
                if (!block.getNumber().equals(currentBlock)) {
                    currentBlock = block.getNumber();
                }

                state.getWriters().startNewBlock(currentBlock);
                state.getDataSource().setCurrentBlock(block);
                this.executeInstructions(state);
                state.getWriters().writeBlock();

            } catch (final Throwable throwable) {
                final String message = String.format("Error when processing block number '%s'.", currentBlock.toString());
                final boolean abort = state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, throwable);
                if (abort) {
                    return;
                }
            } finally {
                state.getDataSource().setCurrentBlock(null);
            }

            currentBlock = currentBlock.add(BigInteger.ONE);
        }
    }

    private void waitForBlockExistence(final ProgramState state, final BigInteger currentBlock)
            throws Throwable, InterruptedException {
        while (state.getDataSource().getClient().queryBlockNumber().compareTo(currentBlock) < 0) {
            Thread.sleep(3000);
        }
    }

    private static EthereumBlock queryConfirmedBlock(final ProgramState state, final BigInteger currentBlock,
            final LinkedList<EthereumBlock> knownBlocks) throws Throwable {
        BigInteger queryBlockNumber = currentBlock;
        do {
            final EthereumBlock block = state.getDataSource().getClient().queryBlockData(queryBlockNumber);
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
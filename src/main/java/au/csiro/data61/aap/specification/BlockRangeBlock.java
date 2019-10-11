package au.csiro.data61.aap.specification;

import au.csiro.data61.aap.specification.types.IntegerType;

/**
 * BlockRangeBlock
 */
public class BlockRangeBlock extends Block {
    private final ValueSource fromBlock;
    private final ValueSource toBlock;

    public BlockRangeBlock(ValueSource fromBlock, ValueSource toBlock) {
        assert fromBlock != null && fromBlock.getType().getClass().isAssignableFrom(IntegerType.class);
        assert toBlock != null && toBlock.getType().getClass().isAssignableFrom(IntegerType.class);
        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
    }

    public ValueSource getFromBlock() {
        return this.fromBlock;
    }

    public ValueSource getToBlock() {
        return this.toBlock;
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException();
    }
        
}
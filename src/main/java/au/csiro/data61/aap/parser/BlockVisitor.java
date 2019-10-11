package au.csiro.data61.aap.parser;

import java.math.BigInteger;

import au.csiro.data61.aap.parser.XbelParser.BlockBodyContext;
import au.csiro.data61.aap.parser.XbelParser.BlockContext;
import au.csiro.data61.aap.parser.XbelParser.BlockHeadContext;
import au.csiro.data61.aap.parser.XbelParser.BlockRangeHeadContext;
import au.csiro.data61.aap.parser.XbelParser.BlockRangeNumberContext;
import au.csiro.data61.aap.parser.XbelParser.BlockStartRuleContext;
import au.csiro.data61.aap.specification.Block;
import au.csiro.data61.aap.specification.BlockRangeBlock;
import au.csiro.data61.aap.specification.Constant;
import au.csiro.data61.aap.specification.ValueSource;
import au.csiro.data61.aap.specification.types.IntegerType;

/**
 * BlockVisitor
 */
class BlockVisitor extends XbelBaseVisitor<SpecificationParserResult<Block>> {
    private static final BigInteger PENDING_BLOCK_NUMBER = new BigInteger("99999999999999999999999999999999999999999999");

    @Override
    public SpecificationParserResult<Block> visitBlockStartRule(BlockStartRuleContext ctx) {
        return this.visitBlock(ctx.block());
    }

    @Override
    public SpecificationParserResult<Block> visitBlock(BlockContext ctx) {
        final SpecificationParserResult<Block> creationResult = createBlock(ctx.blockHead());
        if (!creationResult.isSuccessful()) {
            return creationResult;
        }

        return this.addInstructions(creationResult.getResult(), ctx.blockBody());
    }

    private SpecificationParserResult<Block> createBlock(BlockHeadContext ctx) {
        if (ctx.blockRangeHead() != null) {
            return this.createBlockRangeBlock(ctx.blockRangeHead());
        }

        throw new UnsupportedOperationException("Tests shouldn't reach this point");
    }

    private SpecificationParserResult<Block> createBlockRangeBlock(BlockRangeHeadContext ctx) {
        if (ctx.from.KEY_PENDING() != null) {
            return SpecificationParserResult.ofError(ctx.from.KEY_PENDING().getSymbol(),
                    "PENDING isn't a valid value for parameter 'from'.");
        }

        if (ctx.to.KEY_EARLIEST() != null) {
            return SpecificationParserResult.ofError(ctx.to.KEY_EARLIEST().getSymbol(),
                    "EARLIEST isn't a valid value for parameter 'to'");
        }

        final ValueSource from = this.mapToValueSource(ctx.from, true);
        final ValueSource to = this.mapToValueSource(ctx.to, false);

        if (from instanceof Constant && to instanceof Constant) {
            final BigInteger fromValue = (BigInteger)from.getValue();
            final BigInteger toValue = (BigInteger)to.getValue();
            if (fromValue.compareTo(toValue) >= 0) {
                return SpecificationParserResult.ofError(
                    ctx.getStart(),
                    String.format("The 'from' parameter has to be smaller than the 'to' parameter, but wasn't (%s >= %s)", fromValue, toValue)
                );
            }
        } 

        return SpecificationParserResult.ofResult(new BlockRangeBlock(from, to));
    }

    private ValueSource mapToValueSource(BlockRangeNumberContext ctx, boolean from) {
        if (ctx.variableName() != null) {
            // TODO: return lookup of variable needs to be implemented
            throw new UnsupportedOperationException("variablenames currently not supported as BlockRange parameters");
        }
        else if (ctx.methodCall() != null) {
            // TODO: return lookup of variable needs to be implemented
            throw new UnsupportedOperationException("methodcalls currently not supported as BlockRange parameters");
        }
        else if (ctx.KEY_CURRENT() != null) {
            // TODO: return method that retrieves current block number
            throw new UnsupportedOperationException("CURRENT currently not supported as BlockRange parameters");
        } 
        else if (ctx.INT_VALUE() != null) {
            final BigInteger value = new BigInteger(ctx.INT_VALUE().getText());
            return createConstant(value, from);
        }
        else if (ctx.KEY_EARLIEST() != null) {
            return createConstant(BigInteger.ZERO, from);
        }
        else if (ctx.KEY_PENDING() != null) {
            return createConstant(PENDING_BLOCK_NUMBER, from);
        }
        else {
            throw new UnsupportedOperationException("This option for specifying block range parameters is not supported.");
        }
    }

    private ValueSource createConstant(BigInteger value, boolean from) {
        return new Constant(IntegerType.getDefaultInstance(), 
                            String.format("const block range %s", from ? "from" : "to"), 
                            value
        );
    }

    private SpecificationParserResult<Block> addInstructions(Block block, BlockBodyContext ctx) {
        if (!ctx.blockBodyElements().isEmpty()) {
            // TODO: implement mapping nested instructions to block children
            throw new UnsupportedOperationException("Nesting of instructions into blocks not yet supported.");
        }
        return SpecificationParserResult.ofResult(block);
    }
}
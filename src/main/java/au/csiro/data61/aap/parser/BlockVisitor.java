package au.csiro.data61.aap.parser;

import java.math.BigInteger;
import java.util.stream.IntStream;

import au.csiro.data61.aap.parser.XbelParser.AddressListContext;
import au.csiro.data61.aap.parser.XbelParser.BlockBodyContext;
import au.csiro.data61.aap.parser.XbelParser.BlockContext;
import au.csiro.data61.aap.parser.XbelParser.BlockHeadContext;
import au.csiro.data61.aap.parser.XbelParser.BlockRangeNumberContext;
import au.csiro.data61.aap.parser.XbelParser.BlockStartRuleContext;
import au.csiro.data61.aap.parser.XbelParser.BlocksRangeContext;
import au.csiro.data61.aap.parser.XbelParser.TransactionsRangeContext;
import au.csiro.data61.aap.specification.Block;
import au.csiro.data61.aap.specification.BlockRangeBlock;
import au.csiro.data61.aap.specification.Constant;
import au.csiro.data61.aap.specification.TransactionRangeBlock;
import au.csiro.data61.aap.specification.ValueSource;
import au.csiro.data61.aap.specification.types.AddressType;
import au.csiro.data61.aap.specification.types.ArrayType;
import au.csiro.data61.aap.specification.types.IntegerType;

/**
 * BlockVisitor
 */
class BlockVisitor extends XbelBaseVisitor<SpecificationParserResult<Block>> {
    private static final BigInteger PENDING_BLOCK_NUMBER = new BigInteger(
            "99999999999999999999999999999999999999999999");

    @Override
    public SpecificationParserResult<Block> visitBlockStartRule(BlockStartRuleContext ctx) {
        return this.visitBlock(ctx.block());
    }

    @Override
    public SpecificationParserResult<Block> visitBlock(BlockContext ctx) {
        final SpecificationParserResult<Block> creationResult = mapBlockHeadToBlock(ctx.blockHead());
        if (!creationResult.isSuccessful()) {
            return creationResult;
        }

        return this.addInstructions(creationResult.getResult(), ctx.blockBody());
    }

    private SpecificationParserResult<Block> mapBlockHeadToBlock(BlockHeadContext ctx) {
        if (ctx.blocksRange() != null) {
            return this.mapBlockRangeToBlock(ctx.blocksRange());
        } else if (ctx.transactionsRange() != null) {
            return this.mapTransactionsRangeToBlock(ctx.transactionsRange());
        }

        throw new UnsupportedOperationException("Tests shouldn't reach this point");
    }

    // #region block range mapping

    private SpecificationParserResult<Block> mapBlockRangeToBlock(BlocksRangeContext ctx) {
        if (ctx.from.KEY_PENDING() != null) {
            return SpecificationParserResult.ofError(ctx.from.KEY_PENDING().getSymbol(),
                    "PENDING isn't a valid value for parameter 'from'.");
        }

        if (ctx.to.KEY_EARLIEST() != null) {
            return SpecificationParserResult.ofError(ctx.to.KEY_EARLIEST().getSymbol(),
                    "EARLIEST isn't a valid value for parameter 'to'");
        }

        final ValueSource from = this.mapBlockRangeNumberToValueSource(ctx.from, true);
        final ValueSource to = this.mapBlockRangeNumberToValueSource(ctx.to, false);

        if (from instanceof Constant && to instanceof Constant) {
            final BigInteger fromValue = (BigInteger) from.getValue();
            final BigInteger toValue = (BigInteger) to.getValue();
            if (fromValue.compareTo(toValue) >= 0) {
                return SpecificationParserResult.ofError(ctx.getStart(),
                        String.format(
                                "The 'from' parameter has to be smaller than the 'to' parameter, but wasn't (%s >= %s)",
                                fromValue, toValue));
            }
        }

        return SpecificationParserResult.ofResult(new BlockRangeBlock(from, to));
    }

    private ValueSource mapBlockRangeNumberToValueSource(BlockRangeNumberContext ctx, boolean from) {
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
            return createBlockRangeNumberConstant(value, from);
        } 
        else if (ctx.KEY_EARLIEST() != null) {
            return createBlockRangeNumberConstant(BigInteger.ZERO, from);
        } 
        else if (ctx.KEY_PENDING() != null) {
            return createBlockRangeNumberConstant(PENDING_BLOCK_NUMBER, from);
        } 
        else {
            throw new UnsupportedOperationException(
                    "This option for specifying block range parameters is not supported.");
        }
    }

    private ValueSource createBlockRangeNumberConstant(BigInteger value, boolean from) {
        return new Constant(IntegerType.getDefaultInstance(),
                String.format("const block range %s", from ? "from" : "to"), value);
    }

    // #endregion block range mapping

    // #region transaction range mapping

    private SpecificationParserResult<Block> mapTransactionsRangeToBlock(TransactionsRangeContext ctx) {
        final ValueSource senders = this.mapAddressListToValueSource(ctx.senders, true);
        final ValueSource recipients = this.mapAddressListToValueSource(ctx.recipients, false);
        return SpecificationParserResult.ofResult(new TransactionRangeBlock(senders, recipients));
    }

    private ValueSource mapAddressListToValueSource(AddressListContext ctx, boolean senders) {
        if (ctx.KEY_ANY() != null) {
             // TODO: return lookup of any function needs to be implemented
             throw new UnsupportedOperationException("ANY is currently not supported as a TransactionRange parameter.");
        }
        else if (ctx.variableName() != null) {
             // TODO: return lookup of any function needs to be implemented
             throw new UnsupportedOperationException("Variable names are currently not supported as TransactionRange parameters.");
        }
        else if (ctx.methodCall() != null) {
             // TODO: return lookup of any function needs to be implemented
             throw new UnsupportedOperationException("Method calls are currently not supported as TransactionRange parameters.");
        }
        else if (ctx.BYTE_AND_ADDRESS_VALUE() != null) {
            final String[] addresses = new String[ctx.BYTE_AND_ADDRESS_VALUE().size()];
            IntStream.range(0, addresses.length).forEach(i -> addresses[i] = ctx.BYTE_AND_ADDRESS_VALUE(i).getText());
            return new Constant(ArrayType.defaultInstance(AddressType.defaultInstance()), 
                                String.format("transaction range %s", senders ? "senders" : "recipients"),
                                addresses);
        }
        else {
            throw new UnsupportedOperationException("This option for specifying block range parameters is not supported.");
        }
    }


    // #endregion transaction range mapping

    private SpecificationParserResult<Block> addInstructions(Block block, BlockBodyContext ctx) {
        if (!ctx.blockBodyElements().isEmpty()) {
            // TODO: implement mapping nested instructions to block children
            throw new UnsupportedOperationException("Nesting of instructions into blocks not yet supported.");
        }
        return SpecificationParserResult.ofResult(block);
    }
}
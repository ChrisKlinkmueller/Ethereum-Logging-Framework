package au.csiro.data61.aap.parser;

import java.math.BigInteger;
import java.util.List;

import org.antlr.v4.runtime.Token;

import au.csiro.data61.aap.parser.XbelParser.BlockFilterContext;
import au.csiro.data61.aap.parser.XbelParser.BlockNumberContext;
import au.csiro.data61.aap.parser.XbelParser.DocumentContext;
import au.csiro.data61.aap.spec.BlockScope;
import au.csiro.data61.aap.spec.GlobalScope;
import au.csiro.data61.aap.spec.Instruction;
import au.csiro.data61.aap.spec.Scope;
import au.csiro.data61.aap.spec.Variable;
import au.csiro.data61.aap.spec.types.IntegerType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * ScopeVisitor
 */
class ScopeBuilderVisitor extends XbelBaseVisitor<SpecBuilder<Scope>> {
    
    /**
     * returns the specbuilder for building a global scope
     */
    @Override
    public SpecBuilder<Scope> visitDocument(DocumentContext ctx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SpecBuilder<Scope> visitBlockFilter(BlockFilterContext ctx) {
        
        return new BlockScopeBuilder(ctx);
    }

    private static class BlockScopeBuilder implements SpecBuilder<Scope> {
        private static final String FROM_BLOCK_NUMBER = "fromBlockNumber";
        private static final String TO_BLOCK_NUMBER = "toBlockNumber";

        private final Token token;
        private final BlockNumberContext fromCtx;
        private final BlockNumberContext toCtx;
        private Variable from;
        private Variable to;

        BlockScopeBuilder(BlockFilterContext ctx) {
            this.token = ctx.start;
            this.toCtx = ctx.from;
            this.fromCtx = ctx.to;
        } 

        @Override
        public SpecificationParserError verify(Scope block) {
            if (!(block instanceof GlobalScope)) {
                return new SpecificationParserError(this.token, "A block scope can only be added to a global scope.");
            }

            final MethodResult<Variable> fromResult = this.parseBlockNumber(this.fromCtx, FROM_BLOCK_NUMBER);
            if (!fromResult.isSuccessful()) {
                return new SpecificationParserError(this.token, fromResult.getErrorMessage());
            }

            final MethodResult<Variable> toResult = this.parseBlockNumber(this.toCtx, TO_BLOCK_NUMBER);
            if (!toResult.isSuccessful()) {
                return new SpecificationParserError(this.token, toResult.getErrorMessage());
            }

            final Variable from = fromResult.getResult();
            final Variable to = toResult.getResult();
            
            if (from == BlockScope.PENDING) {
                return new SpecificationParserError(this.token, "The 'from' block number cannot be set to 'PENDING'.");
            }

            if (to == BlockScope.EARLIEST) {
                return new SpecificationParserError(this.token, "The 'to' block number cannot be set to 'EARLIEST'.");
            }

            if (from.getType() instanceof IntegerType && to.getType() instanceof IntegerType) {
                final BigInteger f = (BigInteger)from.getValue().getResult();
                final BigInteger t = (BigInteger)to.getValue().getResult();
                if (f.compareTo(t) > 0) {
                    return new SpecificationParserError(
                        this.token, 
                        String.format("The 'from' parameter must be smaller than or equal to the 'to' parameter, but was not: $s > $s", f, t)
                    );
                }
            }            

            this.from = from;
            this.to = to;
            return null;
        }

        private MethodResult<Variable> parseBlockNumber(BlockNumberContext ctx, String varName) {
            if (ctx.INT_LITERAL() != null) {
                final MethodResult<BigInteger> numberResult = VariableVisitor.integerCast(ctx.INT_LITERAL().getText());
                if (!numberResult.isSuccessful()) {
                    return MethodResult.ofError(numberResult);
                }

                return MethodResult.ofResult(new Variable(IntegerType.DEFAULT_INSTANCE, varName, true, numberResult.getResult()));
            } 
            else if (ctx.KEY_CURRENT() != null) {
                return MethodResult.ofResult(BlockScope.CURRENT);
            }
            else if (ctx.KEY_EARLIEST() != null) {
                return MethodResult.ofResult(BlockScope.EARLIEST);
            }
            else if (ctx.KEY_PENDING() != null) {
                return MethodResult.ofResult(BlockScope.PENDING);
            }
            else {
                throw new UnsupportedOperationException("This way of defining a blockNumber is not supported.");
            }
        }

        @Override
        public Scope build() {
            assert from != null && to != null : "verify() either returned with an error or was not called.";
            return new BlockScope(this.to, this.from);
        }
    }
}
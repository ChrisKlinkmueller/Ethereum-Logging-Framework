package au.csiro.data61.aap.elf.parsing;

import java.math.BigInteger;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import au.csiro.data61.aap.elf.parsing.EthqlParser.AddressListContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockNumberContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ComparatorsContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ComparisonExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.TransactionFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ValueExpressionContext;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * FilterDefinitionAnalyzer
 */
public class FilterDefinitionAnalyzer extends SemanticAnalyzer {
    private final VariableExistenceAnalyzer variableAnalyzer;

    public FilterDefinitionAnalyzer(final ErrorCollector errorCollector,
            final VariableExistenceAnalyzer variableAnalyzer) {
        super(errorCollector);
        assert variableAnalyzer != null;
        this.variableAnalyzer = variableAnalyzer;
    }

    @Override
    public void clear() {
    }

    // #region block filter

    @Override
    public void exitBlockFilter(final BlockFilterContext ctx) {
        this.verifyBlockNumberLiterals(ctx);
        this.verifyBlockNumberVariable(ctx.to);
        this.verifyBlockNumberVariable(ctx.from);

        if (ctx.from.KEY_CONTINUOUS() != null) {
            this.errorCollector.addSemanticError(ctx.from.start, "The 'from' parameter cannot be set to CONTINUOUS.");
        }

        if (ctx.to.KEY_EARLIEST() != null) {
            this.errorCollector.addSemanticError(ctx.to.start, "The 'to' parameter cannot be set to EARLIEST.");
        }
    }

    private void verifyBlockNumberLiterals(BlockFilterContext ctx) {
        if (!this.checkBlockNumberLiteral(ctx.to) || !this.checkBlockNumberLiteral(ctx.from)) {
            return;
        }

        final BigInteger from = TypeUtils.integerFromLiteral(ctx.from.getText());
        final BigInteger to = TypeUtils.integerFromLiteral(ctx.to.getText());
        if (from.compareTo(to) > 0) {
            this.errorCollector.addSemanticError(ctx.from.start,
                    "The 'from' parameter must be smaller than or equal to the 'to' parameter.");
        }
    }

    private boolean checkBlockNumberLiteral(BlockNumberContext ctx) {
        if (   ctx.valueExpression() != null 
            && ctx.valueExpression().literal() != null 
            && ctx.valueExpression().literal().INT_LITERAL() == null
        ) {
            this.addError(ctx.start, "BlockNumber must be an integer value or variable.");
            return true;
        }
        return false;
    }

    private void verifyBlockNumberVariable(final BlockNumberContext ctx) {
        if (ctx.valueExpression() != null && ctx.valueExpression().variableName() != null) {
            final String solType = this.variableAnalyzer.getVariableType(ctx.getText());
            if (solType != null && !TypeUtils.isIntegerType(solType)) {
                this.addError(ctx.start, String.format("'%s' must be an integer variable.", ctx.getText()));
            }
        }
    }

    // #endregion block filter

    // #region transaction filter

    @Override
    public void exitTransactionFilter(final TransactionFilterContext ctx) {
        if (ctx.senders != null) {
            this.verifyAddressList(ctx.senders);
        }

        this.verifyAddressList(ctx.recipients);
    }

    private void verifyAddressList(final AddressListContext ctx) {
        if (ctx.variableName() != null) {
            final String solType = this.variableAnalyzer.getVariableType(ctx.variableName().getText());
            if (solType != null && !TypeUtils.isAddressType(solType)) {
                this.addError(ctx.start, String.format("'%s' must be a string variable", ctx.variableName().getText()));
            }
            return;
        }

        if (ctx.BYTES_LITERAL() != null) {
            for (final TerminalNode node : ctx.BYTES_LITERAL()) {
                if (!TypeUtils.isAddressLiteral(node.getText())) {
                    this.addError(node.getSymbol(),
                            String.format("'%s' is not a valid address literal.", node.getText()));
                }
            }
        }
    }

    // #endregion transaction filter


    
    // #region log entry filter

    @Override
    public void exitLogEntryFilter(final LogEntryFilterContext ctx) {
        this.verifyAddressList(ctx.addressList());
    }

    // #endregion log entry filter



    // #region generic filter

    @Override
    public void exitComparisonExpression(final ComparisonExpressionContext ctx) {
        if (ctx.value != null) {
            this.verifyBooleanValue(ctx.value);
        } else {
            this.verifyComparison(ctx.leftHandSide, ctx.comparators(), ctx.rightHandSide);
        }
    }

    private void verifyBooleanValue(ValueExpressionContext ctx) {
        final String type = InterpreterUtils.determineType(ctx, this.variableAnalyzer);

        if (type == null) {
            this.addTypeInferenceError(ctx.start);
        }
        if (type != null && !TypeUtils.isBooleanType(type)) {
            this.addError(ctx.start, String.format("Expression must be a boolean value or variable.", ctx.literal().start));
        }    
    }

    private void verifyComparison(ValueExpressionContext leftHandSide, ComparatorsContext comparators, ValueExpressionContext rightHandSide) {
        String leftType = InterpreterUtils.determineType(leftHandSide, this.variableAnalyzer);
        String rightType = InterpreterUtils.determineType(rightHandSide, this.variableAnalyzer);
        if (rightType == null || leftType == null) {
            if (leftType == null) {
                this.addTypeInferenceError(leftHandSide.start);
            }
            if (rightType == null) {
                this.addTypeInferenceError(rightHandSide.start);
            }
            return;
        }
        
        if (comparators.KEY_IN() == null) {
            if (!TypeUtils.areCompatible(leftType, rightType)) {
                this.addError(leftHandSide.start, "Types are not compatible.");
            }
        } 
        else {
            if (!TypeUtils.isArrayType(rightType, leftType)) {
                this.addError(leftHandSide.start, String.format("Right hand side cannot be cast to array of type %s.", leftType));
            }
        }
    }

    private void addTypeInferenceError(Token token) {
        this.addError(token, "Expression type cannot be inferred.");
    }

    //#endregion generic filter

}
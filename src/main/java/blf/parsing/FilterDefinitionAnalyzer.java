package blf.parsing;

import java.math.BigInteger;
import java.util.Set;
import java.util.Stack;

import blf.grammar.BcqlParser;
import blf.util.TypeUtils;
import io.reactivex.annotations.NonNull;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import blf.parsing.BlockNumber.Type;

/**
 * FilterDefinitionAnalyzer
 */
public class FilterDefinitionAnalyzer extends SemanticAnalyzer {
    private static final String TYPE_ERROR_FLAG = "TYPE_ERROR";
    private final VariableExistenceListener variableAnalyzer;
    private final Stack<String> conditionalTypes;

    public FilterDefinitionAnalyzer(
        @NonNull final ErrorCollector errorCollector,
        @NonNull final VariableExistenceListener variableAnalyzer
    ) {
        super(errorCollector);
        this.variableAnalyzer = variableAnalyzer;
        this.conditionalTypes = new Stack<>();
    }

    @Override
    public void clear() {}

    // #region block filter

    @Override
    public void exitBlockFilter(final BcqlParser.BlockFilterContext ctx) {
        final BlockNumber from = this.determineBlockNumber(ctx.from);
        if (from.getType() == Type.INVALID || from.getType() == Type.CONTINUOUS) {
            this.addError(
                ctx.from.start,
                "The 'from' block number must be an integer variable, " + "an integer literal or one of the values {EARLIEST, CURRENT}."
            );
            return;
        }

        final BlockNumber to = this.determineBlockNumber(ctx.to);
        if (to.getType() == Type.INVALID || to.getType() == Type.EARLIEST) {
            this.addError(
                ctx.to.start,
                "The 'to' block number must be an integer variable, " + "an integer literal or one of the values {CONTINUOUS, CURRENT}."
            );
            return;
        }

        if (from.isDynamicValue() || to.isDynamicValue()) {
            return;
        }

        if (from.getValue().compareTo(BigInteger.ZERO) < 0) {
            this.addError(ctx.from.start, "The 'from' block number must be an integer larger than or equal to 0.");
        }

        if (to.getValue().compareTo(BigInteger.ZERO) < 0) {
            this.addError(ctx.to.start, "The 'to' block number must be an integer larger than or equal to 0.");
        }

        if (from.getValue().compareTo(to.getValue()) > 0) {
            this.addError(ctx.from.start, "The 'from' block number must be smaller than or equal to the 'to' block number.");
        }
    }

    private BlockNumber determineBlockNumber(BcqlParser.BlockNumberContext ctx) {
        if (ctx.KEY_CONTINUOUS() != null) {
            return BlockNumber.ofContinuous();
        }

        if (ctx.KEY_CURRENT() != null) {
            return BlockNumber.ofCurrent();
        }

        if (ctx.KEY_EARLIEST() != null) {
            return BlockNumber.ofEarliest();
        }

        if (ctx.valueExpression() == null) {
            return BlockNumber.ofInvalid();
        }

        if (ctx.valueExpression().literal() != null) {
            if (ctx.valueExpression().literal().INT_LITERAL() == null) {
                return BlockNumber.ofInvalid();
            }
            try {
                final BigInteger value = new BigInteger(ctx.getText());
                return BlockNumber.ofLiteral(value);
            } catch (NumberFormatException ex) {
                return BlockNumber.ofInvalid();
            }
        }

        final String variableName = ctx.getText();
        final String variableType = this.variableAnalyzer.getVariableType(variableName);
        if (!TypeUtils.isIntegerType(variableType)) {
            return BlockNumber.ofInvalid();
        }
        return BlockNumber.ofVariable();
    }

    // #endregion block filter

    // #region transaction filter

    @Override
    public void exitTransactionFilter(final BcqlParser.TransactionFilterContext ctx) {
        if (ctx.senders != null) {
            this.verifyAddressList(ctx.senders);
        }

        this.verifyAddressList(ctx.recipients);
    }

    private void verifyAddressList(final BcqlParser.AddressListContext ctx) {
        if (ctx.variableName() != null) {
            final String solType = this.variableAnalyzer.getVariableType(ctx.variableName().getText());
            if (solType != null && !TypeUtils.isAddressType(solType) && !TypeUtils.isStringType(solType)) {
                this.addError(ctx.start, String.format("'%s' must be a string variable", ctx.variableName().getText()));
            }
            return;
        }

        if (ctx.BYTES_LITERAL() != null) {
            for (final TerminalNode node : ctx.BYTES_LITERAL()) {
                if (!TypeUtils.isAddressLiteral(node.getText())) {
                    this.addError(node.getSymbol(), String.format("'%s' is not a valid address literal.", node.getText()));
                }
            }
        }
    }

    // #endregion transaction filter

    // #region log entry filter

    @Override
    public void exitLogEntryFilter(final BcqlParser.LogEntryFilterContext ctx) {
        this.verifyAddressList(ctx.addressList());
    }

    // #endregion log entry filter

    // #region generic filter

    @Override
    public void exitConditionalExpression(BcqlParser.ConditionalExpressionContext ctx) {
        this.conditionalTypes.clear();
    }

    @Override
    public void exitConditionalOrExpression(BcqlParser.ConditionalOrExpressionContext ctx) {
        if (ctx.conditionalOrExpression() != null) {
            this.verifyBooleanBinaryOperation(ctx.conditionalOrExpression().start, ctx.conditionalAndExpression().start);
        }
    }

    @Override
    public void exitConditionalAndExpression(BcqlParser.ConditionalAndExpressionContext ctx) {
        if (ctx.conditionalAndExpression() != null) {
            this.verifyBooleanBinaryOperation(ctx.conditionalAndExpression().start, ctx.conditionalComparisonExpression().start);
        }
    }

    private void verifyBooleanBinaryOperation(Token left, Token right) {
        String typeRight = this.conditionalTypes.peek() == null ? null : this.conditionalTypes.pop();
        String typeLeft = this.conditionalTypes.peek() == null ? null : this.conditionalTypes.pop();

        if (typeRight == null || typeLeft == null) {
            return;
        }

        String result = TypeUtils.BOOL_TYPE_KEYWORD;
        if (typeLeft.equals(TYPE_ERROR_FLAG)) {
            result = TYPE_ERROR_FLAG;
        } else if (!TypeUtils.isBooleanType(typeLeft)) {
            this.addError(left, "Expression must return a boolean value.");
            result = TYPE_ERROR_FLAG;
        }

        if (typeRight.equals(TYPE_ERROR_FLAG)) {
            result = TYPE_ERROR_FLAG;
        } else if (!TypeUtils.isBooleanType(typeRight)) {
            this.addError(right, "Expression must return a boolean value.");
            result = TYPE_ERROR_FLAG;
        }

        this.conditionalTypes.push(result);
    }

    @Override
    public void exitConditionalComparisonExpression(BcqlParser.ConditionalComparisonExpressionContext ctx) {
        if (ctx.conditionalNotExpression().size() == 1) {
            return;
        }

        boolean containsBooleanExpression = false;
        for (BcqlParser.ConditionalNotExpressionContext notCtx : ctx.conditionalNotExpression()) {
            if (notCtx.KEY_NOT() != null) {
                this.addError(notCtx.start, "Cannot compare boolean expressions.");
                containsBooleanExpression = true;
            }
        }

        if (!containsBooleanExpression) {
            this.verifyComparison(ctx.start, ctx.comparators());
        }

    }

    private static final Set<String> EQUALITY_COMPARATORS = Set.of("==", "!=");
    private static final Set<String> INTEGER_COMPARATORS = Set.of("<=", "<", ">", ">=");

    private void verifyComparison(Token token, BcqlParser.ComparatorsContext comparators) {
        String typeRight = this.conditionalTypes.peek() == null ? null : this.conditionalTypes.pop();
        String typeLeft = this.conditionalTypes.peek() == null ? null : this.conditionalTypes.pop();

        if (typeRight == null || typeLeft == null) {
            return;
        }

        String result = TypeUtils.BOOL_TYPE_KEYWORD;
        if (typeRight.equals(TYPE_ERROR_FLAG) || typeLeft.equals(TYPE_ERROR_FLAG)) {
            result = TYPE_ERROR_FLAG;
        } else if (comparators.KEY_IN() != null) {
            if (!TypeUtils.isArrayType(typeRight, typeLeft)) {
                this.addError(token, String.format("Types are not compatible, cannot check containment of %s in %s.", typeLeft, typeRight));
                result = TYPE_ERROR_FLAG;
            }
        } else if (EQUALITY_COMPARATORS.contains(comparators.getText())) {
            if (!TypeUtils.areCompatible(typeLeft, typeRight)) {
                this.addError(
                    token,
                    String.format("Types are not compatible, cannot check equality of %s and %s values.", typeLeft, typeRight)
                );
                result = TYPE_ERROR_FLAG;
            }
        } else if (INTEGER_COMPARATORS.contains(comparators.getText())) {
            if (!TypeUtils.isIntegerType(typeLeft) && !TypeUtils.isIntegerType(typeRight)) {
                this.addError(
                    token,
                    String.format("Types are not compatible, can only compare int values, but not %s and %s.", typeLeft, typeRight)
                );
                result = TYPE_ERROR_FLAG;
            }
        }

        this.conditionalTypes.push(result);
    }

    @Override
    public void exitConditionalNotExpression(BcqlParser.ConditionalNotExpressionContext ctx) {
        String type = this.conditionalTypes.peek() == null ? null : this.conditionalTypes.pop();
        if (type == null) {
            return;
        }

        if (ctx.KEY_NOT() != null && !type.equals(TYPE_ERROR_FLAG) && !TypeUtils.isBooleanType(type)) {
            this.addError(ctx.start, "The NOT operator can only be applied to boolean expressions.");
            type = TYPE_ERROR_FLAG;
        }
        this.conditionalTypes.push(type);
    }

    @Override
    public void exitConditionalPrimaryExpression(BcqlParser.ConditionalPrimaryExpressionContext ctx) {
        if (ctx.valueExpression() != null) {
            final String type = InterpreterUtils.determineType(ctx.valueExpression(), this.variableAnalyzer);
            this.conditionalTypes.push(type);
        }
    }

    // #endregion generic filter

    // #region Smart contract filter

    @Override
    public void enterSmartContractFilter(BcqlParser.SmartContractFilterContext ctx) {
        final String type = InterpreterUtils.determineType(ctx.valueExpression(), this.variableAnalyzer);
        if (type == null) {
            return;
        }

        if (!(TypeUtils.isAddressType(type) || TypeUtils.isStringType(type))) {
            this.addError(ctx.valueExpression().start, "Smart contract address must be of address or string type.");
        }
    }

    @Override
    public void enterSmartContractQueryParameter(BcqlParser.SmartContractQueryParameterContext ctx) {
        if (ctx.solType() == null) {
            return;
        }

        final String literalType = InterpreterUtils.literalType(ctx.literal());
        final String solType = ctx.solType().getText();
        if (!TypeUtils.areCompatible(literalType, solType)) {
            this.addError(ctx.solType().start, String.format("Cannot cast %s literal to %s.", literalType, solType));
        }
    }

    // #endregion Smart contract filter
}

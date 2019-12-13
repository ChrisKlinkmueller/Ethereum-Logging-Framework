package au.csiro.data61.aap.elf.generation;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.library.compression.BitMapping;
import au.csiro.data61.aap.elf.library.compression.ValueDictionary;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ArrayLiteralContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ExpressionStatementContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.MethodInvocationContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ScopeContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ValueExpressionContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.VariableNameContext;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * BitMappingGenerator
 */
public class ItemGenerator extends BaseGenerator {
    private static final BigInteger MIN_SHIFT_VALUE = BigInteger.ZERO;
    private static final BigInteger MAX_SHIFT_VALUE = BigInteger.valueOf(255);
    private static final int VAR_NAME_INDEX = 0;
    private static final int FROM_INDEX = 1;
    private static final int TO_INDEX = 2;
    private static final int VALUES_INDEX = 3;
    
    private LogEntryItem logEntry;

    public ItemGenerator(CodeCollector codeCollector) {
        super(codeCollector);
    }

    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.logEntry = new LogEntryItem(
            ctx.logEntrySignature().start,
            ctx.logEntrySignature().getText(),
            ctx.logEntrySignature().methodName.getText());

        ctx.logEntrySignature().logEntryParameter()
            .forEach(
                par -> {
                    final String type = par.solType().getText();
                    final String name = par.variableName().getText();
                    this.logEntry.addParameter(type, name, par.KEY_INDEXED() != null);
                }
            )
        ;
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        if (this.logEntry == null) {
            return;
        }

        this.appendLogEntryHeader();
        this.appendErrorItems();

        this.logEntry = null;
    }

    private void appendLogEntryHeader() {
        final String header = String.format("Generation result for event '%s' (line: %s, column: %s)");
        this.codeCollector.addCommentLine(header);
        this.codeCollector.addEmptyLine();
    }

    private void appendErrorItems() {
        final List<ErrorItem> errors = this.logEntry.itemStream()
            .filter(item -> item instanceof ErrorItem)
            .map(item -> (ErrorItem)item)
            .collect(Collectors.toList());

        if (errors.isEmpty()) {
            return;
        }

        this.codeCollector.addCommentLine("The following function calls could not be considered for code generation.");
        this.codeCollector.addEmptyLine();
        for  (ErrorItem item : errors) {
            this.codeCollector.addCommentLine(String.format("Line: %s, column: %s: %s", item.getLine(), item.getColumn(), item.getSpecification()));
            item.messageStream().forEach(msg -> this.codeCollector.addCommentLine(msg));        
            this.codeCollector.addEmptyLine();
        }
    }

    @Override
    public void exitExpressionStatement(ExpressionStatementContext ctx) {
        if (this.logEntry == null) {
            return;
        }

        if (containsFunction(ctx, BitMapping.METHOD_NAME)) {
            this.createBitMappingItem(
                this.getVariableName(ctx), 
                getMethodInvocationContext(ctx)
            );
        }
        else if (containsFunction(ctx, ValueDictionary.METHOD_NAME)) {
            this.createValueDictionaryItem(
                this.getVariableName(ctx), 
                getMethodInvocationContext(ctx)
            );
        }
    }

    private MethodInvocationContext getMethodInvocationContext(ExpressionStatementContext ctx) {
        if (ctx.methodStatement() != null) {
            return ctx.methodStatement().methodInvocation();
        }
        else if (ctx.variableAssignmentStatement() != null 
            && ctx.variableAssignmentStatement().statementExpression().methodInvocation() != null) {
            return ctx.variableAssignmentStatement().statementExpression().methodInvocation();
        }
        else if (ctx.variableDeclarationStatement() != null 
            && ctx.variableDeclarationStatement().statementExpression().methodInvocation() != null) {
            return ctx.variableDeclarationStatement().statementExpression().methodInvocation();
        }
        return null;
    }

    private String getVariableName(ExpressionStatementContext ctx) {
        if (ctx.variableAssignmentStatement() != null) {
            return ctx.variableAssignmentStatement().variableName().getText();
        }
        
        if (ctx.variableDeclarationStatement() != null) {
            return ctx.variableDeclarationStatement().variableName().getText();
        }
        
        return null;
    }

    private boolean containsFunction(ExpressionStatementContext ctx, String methodName) {
        MethodInvocationContext mCtx = getMethodInvocationContext(ctx);
        return mCtx != null && containsFunction(mCtx, methodName);
    }

    private boolean containsFunction(MethodInvocationContext ctx, String methodName) {
        return ctx.methodName.getText().equals(methodName);
    }

    private void createValueDictionaryItem(String variableName, MethodInvocationContext methodInvocationContext) {

    }

    public void createBitMappingItem(String targetVariable, MethodInvocationContext ctx) {
        final String encodedVariable = this.getVariableName(ctx);
        final BigInteger from = this.getFrom(ctx);
        final BigInteger to = this.getTo(ctx);
        final List<?> values = this.getValues(ctx);

        if (this.areValidValues(encodedVariable, from, to, values)) {  
            final BitMappingItem item = new BitMappingItem(ctx.start, ctx.getText());
            item.setFrom(from);
            item.setTo(to);
            item.setEncodedAttribute(encodedVariable);
            item.setValues(values);
            item.setTargetVariable(targetVariable);
            
            final BitMappingItem overlap = this.findOverlappingBitMapping(item);
            if (overlap != null) {
                final ErrorItem error = new ErrorItem(ctx.start, ctx.getText());
                error.addMessage("Overlap between bitranges:");
                error.addMessage(String.format("%s at (Line: %s, Column: %s) and", item.getSpecification(), item.getLine(), item.getColumn()));
                error.addMessage(String.format("%s at (Line: %s, Column: %s)", item.getSpecification(), item.getLine(), item.getColumn()));
                this.logEntry.addItem(error);
            }
            else {
                this.logEntry.addItem(item);
            }
        }
        else {
            final ErrorItem item = new ErrorItem(ctx.start, ctx.getText());
            item.addMessage("Can only generate bit mapping code for the following type of calls: %s");
            item.addMessage(
                String.format(
                    "%s (int-variable from enclosing log entry signature, int-literal in [%s, %s], int-literal in [%s, %s], array-literal)", 
                    BitMapping.METHOD_NAME,
                    MIN_SHIFT_VALUE, MAX_SHIFT_VALUE,
                    MIN_SHIFT_VALUE, MAX_SHIFT_VALUE
                )
            );
            this.logEntry.addItem(item);
        }
    }  
    
    private BitMappingItem findOverlappingBitMapping(BitMappingItem item) {
        return this.logEntry.itemStream()
            .filter(i -> i instanceof BitMappingItem)
            .map(i -> (BitMappingItem)item)
            .filter(i -> i.getEncodedAttribute().equals(item.getEncodedAttribute()))
            .filter(i -> 
                (i.getFrom().compareTo(item.getFrom()) <= 0 && item.getFrom().compareTo(i.getTo()) >= 0) ||
                (i.getFrom().compareTo(item.getTo()) <= 0 && item.getTo().compareTo(i.getTo()) >= 0)
            )
            .findFirst().orElse(null);
    }

    private boolean areValidValues(String variableName, BigInteger from, BigInteger to, List<?> values) {
        if (variableName == null || from == null || to == null || values == null) {
            return false;
        }

        final String type = this.logEntry.parameterStream()
            .filter(param -> param.getName().equals(variableName))
            .map(param -> param.getType())
            .findFirst().orElse(null);
        if (!TypeUtils.isIntegerType(type)) {
            return false;
        } 

        return this.isInRange(from) && this.isInRange(to) && from.compareTo(to) > 0;
    }

    private boolean isInRange(BigInteger value) {
        return value.compareTo(MIN_SHIFT_VALUE) >= 0 && value.compareTo(MAX_SHIFT_VALUE) <= 0;
    }

    private String getVariableName(MethodInvocationContext ctx) {
        final VariableNameContext varCtx = ctx.valueExpression().get(VAR_NAME_INDEX).variableName(); 
        return varCtx == null ? null : varCtx.getText();
    } 

    private BigInteger getFrom(MethodInvocationContext ctx) {
        return this.getPosition(ctx, FROM_INDEX);
    }

    private BigInteger getTo(MethodInvocationContext ctx) {
        return this.getPosition(ctx, TO_INDEX);
    }

    private BigInteger getPosition(MethodInvocationContext ctx, int index) {
        final ValueExpressionContext posCtx = ctx.valueExpression().get(1);
        return posCtx.literal() == null || posCtx.literal().INT_LITERAL() == null 
            ? null : TypeUtils.integerFromLiteral(posCtx.getText());
    }

    private List<?> getValues(MethodInvocationContext ctx) {
        final ValueExpressionContext valuesCtx = ctx.valueExpression(VALUES_INDEX);
        if (valuesCtx.literal() == null || valuesCtx.literal().arrayLiteral() == null) {
            return null;
        }

        final ArrayLiteralContext literalCtx = valuesCtx.literal().arrayLiteral();
        final String literal = literalCtx.getText();
        if (literalCtx.booleanArrayLiteral() != null) {
            return TypeUtils.parseBoolArrayLiteral(literal);
        }
        else if (literalCtx.bytesArrayLiteral() != null) {
            return TypeUtils.parseBytesArrayLiteral(literal);
        }
        else if (literalCtx.intArrayLiteral() != null) {
            return TypeUtils.parseIntArrayLiteral(literal);
        }
        else if (literalCtx.stringArrayLiteral() != null) {
            return TypeUtils.parseStringArrayLiteral(literal);
        }
        else {
            throw new UnsupportedOperationException(String.format("This type of array literal ('%s') is not supported.", literal));
        }
    }
}
package au.csiro.data61.aap.elf.generation;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import au.csiro.data61.aap.elf.library.compression.BitMapping;
import au.csiro.data61.aap.elf.library.compression.ValueDictionary;
import au.csiro.data61.aap.elf.parsing.InterpreterUtils;
import au.csiro.data61.aap.elf.parsing.BcqlParser.ArrayLiteralContext;
import au.csiro.data61.aap.elf.parsing.BcqlParser.ExpressionStatementContext;
import au.csiro.data61.aap.elf.parsing.BcqlParser.LiteralContext;
import au.csiro.data61.aap.elf.parsing.BcqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.elf.parsing.BcqlParser.MethodInvocationContext;
import au.csiro.data61.aap.elf.parsing.BcqlParser.ScopeContext;
import au.csiro.data61.aap.elf.parsing.BcqlParser.ValueExpressionContext;
import au.csiro.data61.aap.elf.parsing.BcqlParser.VariableNameContext;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * BitMappingGenerator
 */
public class ItemGenerator extends BaseGenerator {
    private static final BigInteger BITMAP_MIN_SHIFT = BigInteger.ZERO;
    private static final BigInteger BITMAP_MAX_SHIFT = BigInteger.valueOf(255);
    private static final int BITMAP_VAR_INDEX = 0;
    private static final int BITMAP_FROM_INDEX = 1;
    private static final int BITMAP_TO_INDEX = 2;
    private static final int BITMAP_VALUES_INDEX = 3;
    private static final int VALDICT_VAR_INDEX = 0;
    private static final int VALDICT_DEFAULTVAL_INDEX = 1;
    private static final int VALDICT_FROM_INDEX = 2;
    private static final int VALDICT_TO_INDEX = 3;

    private LogEntryItem logEntry;
    private int enumValueCount;

    public ItemGenerator(final CodeCollector codeCollector) {
        super(codeCollector);
        this.enumValueCount = 0;
    }

    @Override
    public void enterLogEntryFilter(final LogEntryFilterContext ctx) {
        this.logEntry = new LogEntryItem(
            ctx.logEntrySignature().start,
            ctx.logEntrySignature().getText(),
            ctx.logEntrySignature().methodName.getText()
        );

        ctx.logEntrySignature().logEntryParameter().forEach(par -> {
            final String type = par.solType().getText();
            final String name = par.variableName().getText();
            this.logEntry.addParameter(type, name, par.KEY_INDEXED() != null);
        });
    }

    @Override
    public void exitScope(final ScopeContext ctx) {
        if (this.logEntry == null) {
            return;
        }

        this.appendLogEntryHeader();
        this.appendErrorItems();
        this.generateCode();

        this.logEntry = null;
    }

    private void appendLogEntryHeader() {
        final String header = String.format(
            "Generation result for event '%s' (line: %s, column: %s)",
            this.logEntry.getSpecification(),
            this.logEntry.getLine(),
            this.logEntry.getColumn()
        );
        this.codeCollector.addCommentLine(header);
        this.codeCollector.addEmptyLine();
    }

    private void appendErrorItems() {
        final List<ErrorItem> errors = this.logEntry.itemStream()
            .filter(item -> item instanceof ErrorItem)
            .map(item -> (ErrorItem) item)
            .collect(Collectors.toList());

        if (errors.isEmpty()) {
            return;
        }

        this.codeCollector.addCommentLine("The following function calls could not be considered for code generation.");
        this.codeCollector.addEmptyLine();
        for (final ErrorItem item : errors) {
            this.codeCollector.addCommentLine(
                String.format("Line: %s, column: %s: %s", item.getLine(), item.getColumn(), item.getSpecification())
            );
            item.messageStream().forEach(msg -> this.codeCollector.addCommentLine(msg));
            this.codeCollector.addEmptyLine();
        }
    }

    private void generateCode() {
        this.generateLogEntrySignature();
        this.generateEnumsAndMaps();
        this.codeCollector.addEmptyLine();

        this.generatorConstructor();

        this.openLoggingMethod();
        this.generateBitMaps();
        this.generateValueMaps();
        this.createEmit();
        this.closeLoggingMethod();
    }

    private void generateLogEntrySignature() {
        final String parameters = this.logEntry.parameterStream()
            .map(p -> String.format("%s %s%s", p.getType(), p.isIndexed() ? "indexed " : "", p.getName()))
            .collect(Collectors.joining(", "));
        String signature = String.format("event %s(%s);", this.logEntry.getEventName(), parameters);
        this.codeCollector.addCodeLine(signature);
    }

    private void generateEnumsAndMaps() {
        this.itemStream(BitMappingItem.class).forEach(item -> this.generateBitMappingEnum(item));

        this.itemStream(ValueDictionaryItem.class).forEach(item -> this.generateMapping(item));
    }

    private void generateMapping(ValueDictionaryItem item) {
        final String code = String.format("mapping (%s => %s) %ss;", item.getTargetType(), item.getEncodedType(), item.getTargetVariable());
        this.codeCollector.addCodeLine(code);
    }

    private void generatorConstructor() {
        if (this.itemStream(ValueDictionaryItem.class).count() == 0) {
            return;
        }

        this.codeCollector.addCodeLine("constructor() public {");

        this.itemStream(ValueDictionaryItem.class).forEach(item -> this.generateMappingInitialization(item));

        this.codeCollector.addCodeLine("}");
        this.codeCollector.addEmptyLine();
    }

    private void generateMappingInitialization(ValueDictionaryItem item) {
        IntStream.range(0, item.getToValues().size()).forEach(i -> {
            final String code = String.format(
                "\t%ss[%s] = %s;",
                item.getTargetVariable(),
                this.generateLiteral(item.getToValues().get(i)),
                this.generateLiteral(item.getFromValues().get(i))
            );
            this.codeCollector.addCodeLine(code);
        });
    }

    private String generateLiteral(Object value) {
        if (value instanceof String && !TypeUtils.isBytesLiteral(value.toString())) {
            return String.format("\"%s\"", value.toString());
        }
        return value.toString();
    }

    private void openLoggingMethod() {
        final String parameters = this.logEntry.parameterStream()
            .map(param -> this.getLogParameter(param))
            .collect(Collectors.joining(", "));

        final String code = String.format("function log%s(%s) internal {", this.logEntry.getEventName(), parameters);
        this.codeCollector.addCodeLine(code);
    }

    private String getLogParameter(LogEntryParameter parameter) {
        if (this.containsBitMappingForAttribute(parameter.getName())) {
            return this.bitMappingItemStream(parameter.getName())
                .map(item -> String.format("%s %s", this.getEnumName(item), item.getTargetVariable()))
                .collect(Collectors.joining(", "));
        } else if (this.containsValueDictForAttribute(parameter.getName())) {
            return this.valueDicItemStream(parameter.getName())
                .map(
                    item -> String.format(
                        "%s %s",
                        item.getTargetType() == "string" ? "string memory" : item.getTargetType(),
                        item.getTargetVariable()
                    )
                )
                .findFirst()
                .orElse(null);
        } else {
            return String.format("%s %s", parameter.getType(), parameter.getName());
        }
    }

    private boolean containsBitMappingForAttribute(String name) {
        return this.bitMappingItemStream(name).count() != 0;
    }

    private Stream<BitMappingItem> bitMappingItemStream(String name) {
        return this.itemStream(BitMappingItem.class).filter(i -> i.getEncodedAttribute().equals(name));
    }

    private boolean containsValueDictForAttribute(String name) {
        return this.itemStream(ValueDictionaryItem.class).filter(i -> i.getEncodedAttribute().equals(name)).count() != 0;
    }

    private Stream<ValueDictionaryItem> valueDicItemStream(String name) {
        return this.itemStream(ValueDictionaryItem.class).filter(i -> i.getEncodedAttribute().equals(name));
    }

    private void generateBitMaps() {
        final List<String> encodedVariables = this.itemStream(BitMappingItem.class)
            .map(item -> item.getEncodedAttribute())
            .distinct()
            .collect(Collectors.toList());

        for (String encodedVariable : encodedVariables) {
            this.generateBitMapMask(encodedVariable);

            final String code = String.format(
                "\tuint256 %s = %s;",
                encodedVariable,
                this.bitMappingItemStream(encodedVariable).map(item -> this.createMaskVariable(item)).collect(Collectors.joining(" | "))
            );
            this.codeCollector.addCodeLine(code);
        }
    }

    private void generateBitMapMask(String encodedVariable) {
        this.bitMappingItemStream(encodedVariable).forEach(item -> {
            final String code = String.format(
                "\tuint256 %s = uint(%s) << %s;",
                this.createMaskVariable(item),
                item.getTargetVariable(),
                item.getFrom().toString()
            );
            this.codeCollector.addCodeLine(code);
        });
    }

    private String createMaskVariable(BitMappingItem item) {
        return String.format("%sMask", item.getTargetVariable());
    }

    private void generateValueMaps() {
        this.itemStream(ValueDictionaryItem.class).forEach(item -> {
            final String code = String.format(
                "\t%s %s = %ss[%s];",
                item.getEncodedType(),
                item.getEncodedAttribute(),
                item.getTargetVariable(),
                item.getTargetVariable()
            );
            this.codeCollector.addCodeLine(code);
        });
    }

    private void createEmit() {
        final String code = String.format(
            "\temit %s(%s);",
            this.logEntry.getEventName(),
            this.logEntry.parameterStream().map(par -> par.getName()).collect(Collectors.joining(", "))
        );
        this.codeCollector.addCodeLine(code);
    }

    private void closeLoggingMethod() {
        this.codeCollector.addCodeLine("}");
    }

    private void generateBitMappingEnum(BitMappingItem item) {
        final Object values = item.getValues()
            .stream()
            .map(val -> val instanceof String ? val.toString().toUpperCase() : this.createEnumValue(val))
            // .map(val -> this.createEnumValue(val))
            .collect(Collectors.joining(", "));

        final String code = String.format("enum %s {%s}", this.getEnumName(item), values);
        this.codeCollector.addCodeLine(code);
    }

    private String getEnumName(BitMappingItem item) {
        return String.format("%s%s", item.getTargetVariable().substring(0, 1).toUpperCase(), item.getTargetVariable().substring(1));
    }

    private String createEnumValue(Object val) {
        return String.format("VAL_%s_%s", this.enumValueCount++, val.toString());
    }

    @SuppressWarnings("unchecked")
    private <T extends GeneratorItem> Stream<T> itemStream(Class<T> cl) {
        return this.logEntry.itemStream().filter(item -> item.getClass().equals(cl)).map(item -> (T) item);
    }

    @Override
    public void exitExpressionStatement(final ExpressionStatementContext ctx) {
        if (this.logEntry == null) {
            return;
        }

        if (this.containsFunction(ctx, BitMapping.METHOD_NAME)) {
            this.createBitMapping(this.getTargetVariable(ctx), this.getMethodInvocationContext(ctx));
        } else if (this.containsFunction(ctx, ValueDictionary.METHOD_NAME)) {
            this.createValueDictionary(this.getTargetVariable(ctx), this.getMethodInvocationContext(ctx));
        }
    }

    private MethodInvocationContext getMethodInvocationContext(final ExpressionStatementContext ctx) {
        if (ctx.methodStatement() != null) {
            return ctx.methodStatement().methodInvocation();
        } else if (ctx.variableAssignmentStatement() != null
            && ctx.variableAssignmentStatement().statementExpression().methodInvocation() != null) {
                return ctx.variableAssignmentStatement().statementExpression().methodInvocation();
            } else if (ctx.variableDeclarationStatement() != null
                && ctx.variableDeclarationStatement().statementExpression().methodInvocation() != null) {
                    return ctx.variableDeclarationStatement().statementExpression().methodInvocation();
                }
        return null;
    }

    private String getTargetVariable(final ExpressionStatementContext ctx) {
        if (ctx.variableAssignmentStatement() != null) {
            return ctx.variableAssignmentStatement().variableName().getText();
        }

        if (ctx.variableDeclarationStatement() != null) {
            return ctx.variableDeclarationStatement().variableName().getText();
        }

        return null;
    }

    private boolean containsFunction(final ExpressionStatementContext ctx, final String methodName) {
        final MethodInvocationContext mCtx = getMethodInvocationContext(ctx);
        return mCtx != null && containsFunction(mCtx, methodName);
    }

    private boolean containsFunction(final MethodInvocationContext ctx, final String methodName) {
        return ctx.methodName.getText().equals(methodName);
    }

    // #region ValueDictionaryItem collection

    private void createValueDictionary(final String targetVariable, final MethodInvocationContext ctx) {
        final String encodedAttribute = this.getValueMapDictVariable(ctx);
        final String encodedType = this.getValueMapDictType(ctx);
        final String targetType = this.getValueDictTargetType(ctx);
        final Object defaultValue = this.getValueDictDefaultValue(ctx);
        final List<?> fromValues = this.getLiteralValues(ctx, VALDICT_FROM_INDEX, true);
        final List<?> toValues = this.getLiteralValues(ctx, VALDICT_TO_INDEX, true);
        if (this.areValidValueDictValues(encodedAttribute, targetType, defaultValue, fromValues, toValues)) {
            final ValueDictionaryItem item = new ValueDictionaryItem(ctx.start, ctx.getText());
            item.setTargetVariable(targetVariable);
            item.setTargetType(targetType);
            item.setEncodedAttribute(encodedAttribute);
            item.setEncodedType(encodedType);
            item.setDefaultValue(defaultValue);
            item.setFromValues(fromValues);
            item.setToValues(toValues);

            ValueDictionaryItem overlap = findOverlappingValueDict(item);
            if (overlap == null) {
                this.logEntry.addItem(item);
            } else {
                final ErrorItem error = new ErrorItem(ctx.start, ctx.getText());
                error.addMessage("Same attribute used multiple times in different value mappings:");
                error.addMessage(
                    String.format("%s at (Line: %s, Column: %s) and", overlap.getSpecification(), overlap.getLine(), overlap.getColumn())
                );
                error.addMessage(String.format("%s at (Line: %s, Column: %s)", item.getSpecification(), item.getLine(), item.getColumn()));
                this.logEntry.addItem(error);
            }
        } else {
            final ErrorItem item = new ErrorItem(ctx.start, ctx.getText());
            item.addMessage("Can only generate value dictionary code for the following type of calls: %s");
            item.addMessage(
                String.format(
                    "%s (variable from enclosing log entry signature, literal, array-literal, array-literal)",
                    ValueDictionary.METHOD_NAME,
                    BITMAP_MIN_SHIFT,
                    BITMAP_MAX_SHIFT,
                    BITMAP_MIN_SHIFT,
                    BITMAP_MAX_SHIFT
                )
            );
            item.addMessage("where both array literals have the same length.");
            this.logEntry.addItem(item);
        }
    }

    private String getValueMapDictVariable(MethodInvocationContext ctx) {
        if (ctx.valueExpression().size() <= VALDICT_VAR_INDEX) {
            return null;
        }

        ValueExpressionContext val = ctx.valueExpression(VALDICT_VAR_INDEX);
        if (val.variableName() == null) {
            return null;
        }

        return val.variableName().getText();
    }

    private String getValueMapDictType(MethodInvocationContext ctx) {
        if (ctx.valueExpression().size() <= VALDICT_VAR_INDEX) {
            return null;
        }

        ValueExpressionContext val = ctx.valueExpression(VALDICT_VAR_INDEX);
        if (val.variableName() == null) {
            return null;
        }

        return this.logEntry.parameterStream()
            .filter(par -> par.getName().equals(val.variableName().getText()))
            .map(par -> par.getType())
            .findFirst()
            .orElse(null);
    }

    private String getValueDictTargetType(MethodInvocationContext ctx) {
        if (ctx.valueExpression().size() <= VALDICT_DEFAULTVAL_INDEX) {
            return null;
        }

        final ValueExpressionContext valueCtx = ctx.valueExpression(VALDICT_DEFAULTVAL_INDEX);
        return valueCtx.literal() == null ? null : InterpreterUtils.literalType(valueCtx.literal());
    }

    private Object getValueDictDefaultValue(MethodInvocationContext ctx) {
        final List<?> values = this.getLiteralValues(ctx, VALDICT_DEFAULTVAL_INDEX, false);
        return values == null || values.size() != 1 ? null : values.get(0);
    }

    private boolean areValidValueDictValues(
        String encodedVariable,
        String targetType,
        Object defaultValue,
        List<?> fromValues,
        List<?> toValues
    ) {
        if (encodedVariable == null || targetType == null || defaultValue == null || fromValues == null || toValues == null) {
            return false;
        }

        if (fromValues.size() != toValues.size()) {
            return false;
        }

        return this.logEntry.parameterStream().anyMatch(param -> param.getName().equals(encodedVariable));
    }

    private ValueDictionaryItem findOverlappingValueDict(final ValueDictionaryItem item) {
        return this.logEntry.itemStream()
            .filter(i -> i instanceof ValueDictionaryItem)
            .map(i -> (ValueDictionaryItem) item)
            .filter(i -> i.getEncodedAttribute().equals(item.getEncodedAttribute()))
            .findFirst()
            .orElse(null);
    }

    // #endregion ValueDictionaryItem collection

    // #region BitMappingItem collection

    public void createBitMapping(final String targetVariable, final MethodInvocationContext ctx) {
        final String encodedVariable = this.getBitMappingVariable(ctx);
        final BigInteger from = this.getBitMappingFrom(ctx);
        final BigInteger to = this.getBitMappingTo(ctx);
        final List<?> values = this.getLiteralValues(ctx, BITMAP_VALUES_INDEX, true);

        if (this.areValidBitMappingValues(encodedVariable, from, to, values)) {
            final BitMappingItem item = new BitMappingItem(ctx.start, ctx.getText());
            item.setFrom(from);
            item.setTo(to);
            item.setEncodedAttribute(encodedVariable);
            item.setValues(values);
            item.setTargetVariable(targetVariable);

            final BitMappingItem overlap = this.findOverlappingBitMapping(item);
            if (overlap == null) {
                this.logEntry.addItem(item);
            } else {
                final ErrorItem error = new ErrorItem(ctx.start, ctx.getText());
                error.addMessage("Overlap between bitranges:");
                error.addMessage(
                    String.format("%s at (Line: %s, Column: %s) and", overlap.getSpecification(), overlap.getLine(), overlap.getColumn())
                );
                error.addMessage(String.format("%s at (Line: %s, Column: %s)", item.getSpecification(), item.getLine(), item.getColumn()));
                this.logEntry.addItem(error);
            }
        } else {
            final ErrorItem item = new ErrorItem(ctx.start, ctx.getText());
            item.addMessage("Can only generate bit mapping code for the following type of calls: %s");
            item.addMessage(
                String.format(
                    "%s (int-variable from enclosing log entry signature, int-literal in [%s, %s], int-literal in [%s, %s], array-literal)",
                    BitMapping.METHOD_NAME,
                    BITMAP_MIN_SHIFT,
                    BITMAP_MAX_SHIFT,
                    BITMAP_MIN_SHIFT,
                    BITMAP_MAX_SHIFT
                )
            );
            this.logEntry.addItem(item);
        }
    }

    private BitMappingItem findOverlappingBitMapping(final BitMappingItem item) {
        return this.logEntry.itemStream()
            .filter(existingItem -> existingItem instanceof BitMappingItem)
            .map(existingItem -> (BitMappingItem) existingItem)
            .filter(existingItem -> existingItem.getEncodedAttribute().equals(item.getEncodedAttribute()))
            .filter(
                existingItem -> (existingItem.getFrom().compareTo(item.getFrom()) <= 0
                    && item.getFrom().compareTo(existingItem.getTo()) <= 0)
                    || (existingItem.getFrom().compareTo(item.getTo()) <= 0 && item.getTo().compareTo(existingItem.getTo()) <= 0)
            )
            .findFirst()
            .orElse(null);
    }

    private boolean areValidBitMappingValues(final String variableName, final BigInteger from, final BigInteger to, final List<?> values) {
        if (variableName == null || from == null || to == null || values == null) {
            return false;
        }

        if (to.compareTo(from) < 0) {
            return false;
        }

        final String type = this.logEntry.parameterStream()
            .filter(param -> param.getName().equals(variableName))
            .map(param -> param.getType())
            .findFirst()
            .orElse(null);
        if (!TypeUtils.isIntegerType(type)) {
            return false;
        }

        return this.isInBitMappingRange(from) && this.isInBitMappingRange(to) && from.compareTo(to) <= 0;
    }

    private boolean isInBitMappingRange(final BigInteger value) {
        return BITMAP_MIN_SHIFT.compareTo(value) <= 0 && value.compareTo(BITMAP_MAX_SHIFT) <= 0;
    }

    private String getBitMappingVariable(final MethodInvocationContext ctx) {
        final VariableNameContext varCtx = ctx.valueExpression().get(BITMAP_VAR_INDEX).variableName();
        return varCtx == null ? null : varCtx.getText();
    }

    private BigInteger getBitMappingFrom(final MethodInvocationContext ctx) {
        return this.getBitMappingPosition(ctx, BITMAP_FROM_INDEX);
    }

    private BigInteger getBitMappingTo(final MethodInvocationContext ctx) {
        return this.getBitMappingPosition(ctx, BITMAP_TO_INDEX);
    }

    private BigInteger getBitMappingPosition(final MethodInvocationContext ctx, final int index) {
        final ValueExpressionContext posCtx = ctx.valueExpression().get(index);
        return posCtx.literal() == null || posCtx.literal().INT_LITERAL() == null ? null : TypeUtils.integerFromLiteral(posCtx.getText());
    }

    // #endregion BitMappingItem collection

    private List<?> getLiteralValues(final MethodInvocationContext ctx, int index, boolean arrayOnly) {
        if (ctx.valueExpression().size() <= index) {
            return null;
        }

        final ValueExpressionContext valuesCtx = ctx.valueExpression(index);
        if (valuesCtx.literal() == null) {
            return null;
        }

        if (valuesCtx.literal().arrayLiteral() != null) {
            final ArrayLiteralContext literalCtx = valuesCtx.literal().arrayLiteral();
            final String literal = literalCtx.getText();
            if (literalCtx.booleanArrayLiteral() != null) {
                return TypeUtils.parseBoolArrayLiteral(literal);
            } else if (literalCtx.bytesArrayLiteral() != null) {
                return TypeUtils.parseBytesArrayLiteral(literal);
            } else if (literalCtx.intArrayLiteral() != null) {
                return TypeUtils.parseIntArrayLiteral(literal);
            } else if (literalCtx.stringArrayLiteral() != null) {
                return TypeUtils.parseStringArrayLiteral(literal);
            } else {
                throw new UnsupportedOperationException(String.format("This type of array literal ('%s') is not supported.", literal));
            }
        } else if (!arrayOnly) {
            final LiteralContext literalCtx = valuesCtx.literal();
            final String literal = literalCtx.getText();
            if (literalCtx.BOOLEAN_LITERAL() != null) {
                boolean val = TypeUtils.parseBoolLiteral(literal);
                return List.of(val);
            } else if (literalCtx.BYTES_LITERAL() != null) {
                String val = TypeUtils.parseBytesLiteral(literal);
                return List.of(val);
            }
            if (literalCtx.INT_LITERAL() != null) {
                BigInteger val = TypeUtils.parseIntLiteral(literal);
                return List.of(val);
            }
            if (literalCtx.STRING_LITERAL() != null) {
                String val = TypeUtils.parseStringLiteral(literal);
                return List.of(val);
            }
            return null;
        }
        return null;
    }
}

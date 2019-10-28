package au.csiro.data61.aap.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import au.csiro.data61.aap.parser.XbelParser.ArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.BooleanArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.ByteAndAddressArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.FixedArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.IntArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.LiteralContext;
import au.csiro.data61.aap.parser.XbelParser.LiteralRuleContext;
import au.csiro.data61.aap.parser.XbelParser.StringArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.VariableDefinitionContext;
import au.csiro.data61.aap.parser.XbelParser.VariableDefinitionRuleContext;
import au.csiro.data61.aap.spec.CodeBlock;
import au.csiro.data61.aap.spec.Statement;
import au.csiro.data61.aap.spec.Variable;
import au.csiro.data61.aap.spec.types.ArrayType;
import au.csiro.data61.aap.spec.types.BoolType;
import au.csiro.data61.aap.spec.types.BytesType;
import au.csiro.data61.aap.spec.types.FixedType;
import au.csiro.data61.aap.spec.types.IntegerType;
import au.csiro.data61.aap.spec.types.SolidityType;
import au.csiro.data61.aap.spec.types.StringType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * VariableVisitor
 */
class VariableVisitor extends XbelBaseVisitor<SpecificationParserResult<SpecBuilder<Variable>>> {
    private static final String BYTES_PREFIX = "0x";

    @Override
    public SpecificationParserResult<SpecBuilder<Variable>> visitVariableDefinitionRule(VariableDefinitionRuleContext ctx) {
        return this.visitVariableDefinition(ctx.variableDefinition());
    }

    @Override
    public SpecificationParserResult<SpecBuilder<Variable>> visitVariableDefinition(VariableDefinitionContext ctx) {
        return SpecificationParserResult.ofResult(new VariableDefinitionBuilder(ctx));
    }

    @Override
    public SpecificationParserResult<SpecBuilder<Variable>> visitLiteralRule(LiteralRuleContext ctx) {
        return this.visitLiteral(ctx.literal());
    }

    @Override
    public SpecificationParserResult<SpecBuilder<Variable>> visitLiteral(LiteralContext ctx) {
        assert ctx != null;

        if (ctx.BOOLEAN_LITERAL() != null) {
            return SpecificationParserResult.ofResult(new PlainLiteralBuilder(ctx.BOOLEAN_LITERAL(), BoolType.DEFAULT_INSTANCE, VariableVisitor::booleanCast));
        }
        else if (ctx.BYTE_AND_ADDRESS_LITERAL() != null) {
            return SpecificationParserResult.ofResult(new PlainLiteralBuilder(ctx.BYTE_AND_ADDRESS_LITERAL(), BytesType.DEFAULT_INSTANCE, VariableVisitor::bytesCast));
        }
        else if (ctx.FIXED_LITERAL() != null) {
            return SpecificationParserResult.ofResult(new PlainLiteralBuilder(ctx.FIXED_LITERAL(), FixedType.DEFAULT_INSTANCE, VariableVisitor::fixedCast));
        }
        else if (ctx.INT_LITERAL() != null) {
            return SpecificationParserResult.ofResult(new PlainLiteralBuilder(ctx.INT_LITERAL(), IntegerType.DEFAULT_INSTANCE, VariableVisitor::integerCast));
        }
        else if (ctx.STRING_LITERAL() != null) {
            return SpecificationParserResult.ofResult(new PlainLiteralBuilder(ctx.STRING_LITERAL(), StringType.DEFAULT_INSTANCE, VariableVisitor::stringCast));
        }
        else if (ctx.arrayValue() != null) {
            return this.visitArrayValue(ctx.arrayValue());
        }
        else {
            return SpecificationParserResult.ofError(String.format("'%s' is not recognized as valid code.", ctx.getText()));
        }
    }

    @Override
    public SpecificationParserResult<SpecBuilder<Variable>> visitArrayValue(ArrayValueContext ctx) {
        assert ctx != null;

        if (ctx.booleanArrayValue() != null) {
            return this.visitBooleanArrayValue(ctx.booleanArrayValue());
        }
        else if (ctx.byteAndAddressArrayValue() != null) {
            return this.visitByteAndAddressArrayValue(ctx.byteAndAddressArrayValue());
        }
        else if (ctx.fixedArrayValue() != null) {
            return this.visitFixedArrayValue(ctx.fixedArrayValue());
        }
        else if (ctx.intArrayValue() != null) {
            return this.visitIntArrayValue(ctx.intArrayValue());
        }
        else if (ctx.stringArrayValue() != null) {
            return this.visitStringArrayValue(ctx.stringArrayValue());
        }
        else {
            throw new InvalidParameterException(String.format("'%s' is not recognized as valid code.", ctx.getText()));
        }
    }

    @Override
    public SpecificationParserResult<SpecBuilder<Variable>> visitBooleanArrayValue(BooleanArrayValueContext ctx) {
        return SpecificationParserResult.ofResult(new ArrayLiteralBuilder(ctx.BOOLEAN_LITERAL(), BoolType.DEFAULT_INSTANCE, VariableVisitor::booleanCast));
    }

    @Override
    public SpecificationParserResult<SpecBuilder<Variable>> visitByteAndAddressArrayValue(ByteAndAddressArrayValueContext ctx) {
        return SpecificationParserResult.ofResult(new ArrayLiteralBuilder(ctx.BYTE_AND_ADDRESS_LITERAL(), BytesType.DEFAULT_INSTANCE, VariableVisitor::bytesCast));
    }

    @Override
    public SpecificationParserResult<SpecBuilder<Variable>> visitFixedArrayValue(FixedArrayValueContext ctx) {
        return SpecificationParserResult.ofResult(
            new ArrayLiteralBuilder(
                ctx.fixedArrayElement().stream()
                .map(el -> el.FIXED_LITERAL() == null ? el.INT_LITERAL() : el.FIXED_LITERAL())
                .collect(Collectors.toList()), 
                FixedType.DEFAULT_INSTANCE, 
                VariableVisitor::fixedCast
            )
        );
    }

    @Override
    public SpecificationParserResult<SpecBuilder<Variable>> visitIntArrayValue(IntArrayValueContext ctx) {
        return SpecificationParserResult.ofResult(new ArrayLiteralBuilder(ctx.INT_LITERAL(), IntegerType.DEFAULT_INSTANCE, VariableVisitor::integerCast));
    }

    @Override
    public SpecificationParserResult<SpecBuilder<Variable>> visitStringArrayValue(StringArrayValueContext ctx) {
        return SpecificationParserResult.ofResult(new ArrayLiteralBuilder(ctx.STRING_LITERAL(), StringType.DEFAULT_INSTANCE, VariableVisitor::stringCast));
    }

    private static class VariableDefinitionBuilder implements SpecBuilder<Variable> {
        private final SpecificationParserResult<Variable> parseResult;
        private final Token token;

        public VariableDefinitionBuilder(VariableDefinitionContext ctx) {
            this.token = ctx.start;
            
            final SpecificationParserResult<SolidityType> typeResult = VisitorRepository.SOLIDITY_TYPE_VISITOR.visitSolType(ctx.solType());
            if (typeResult.isSuccessful()) {
                this.parseResult = SpecificationParserResult.ofResult(
                    new Variable(typeResult.getResult(), ctx.variableName().getText())
                );
            }
            else {
                this.parseResult = SpecificationParserResult.ofUnsuccessfulParserResult(typeResult);
            }
        }

        @Override
        public SpecificationParserError verify(CodeBlock block) {
            if (!this.parseResult.isSuccessful()) {
                assert 0 < this.parseResult.errorCount();
                return this.parseResult.getError(0);
            }

            final Variable variable = this.parseResult.getResult();
            switch (this.variableAlreadyDefined(variable, block)) {
                case DEFINED : return new SpecificationParserError(this.token, String.format("A variable with name '%s' has already been defined."));
                case RESERVED : return new SpecificationParserError(this.token, String.format("The variable name '%s' is reserved."));
                default : return null;
            }
        }

        private VariableExistence variableAlreadyDefined(Variable variable, CodeBlock block) {
            if (block.isVariableNameReserved(variable.getName())) {
                return VariableExistence.RESERVED;
            }

            if (block.instructionStream().filter(i -> i instanceof Statement).anyMatch(i -> this.containsVariable((Statement)i, variable))) {
                return VariableExistence.DEFINED;
            }


            if (block.getEnclosingBlock() != null) {
                return this.variableAlreadyDefined(variable, block.getEnclosingBlock());
            }

            return VariableExistence.UNDEFINED;
        }

        private boolean containsVariable(Statement statement, Variable variable) {
            return statement.getVariable().isPresent() && statement.getVariable().get().getName().equals(variable.getName());
        }

        @Override
        public Variable build(CodeBlock block) {
            return this.parseResult.getResult();
        }
        
        private static enum VariableExistence {
            RESERVED,
            DEFINED,
            UNDEFINED
        }

    }

    private static class ArrayLiteralBuilder implements SpecBuilder<Variable> {
        private final SpecificationParserError error;
        private final Variable literal;

        public ArrayLiteralBuilder(List<TerminalNode> nodes, SolidityType baseType, Function<String,MethodResult<Object>> cast) {
            final List<Object> values = new ArrayList<>();
            for (TerminalNode node : nodes) {
                final MethodResult<Object> castResult = safeCast(node.getText(), cast);
                if (castResult.isSuccessful()) {
                    values.add(castResult.getResult());
                } 
                else {
                    this.literal = null;
                    this.error = new SpecificationParserError(node.getSymbol(), castResult.getErrorMessage());
                    return;
                }
            }
            this.literal = new Variable(new ArrayType(baseType), createLiteralName(), true, values);
            this.error = null;
        }

        @Override
        public SpecificationParserError verify(CodeBlock block) {
            return this.error;
        }

        @Override
        public Variable build(CodeBlock block) {
            return this.literal;
        }

    }

    private static class PlainLiteralBuilder implements SpecBuilder<Variable> {
        private final SpecificationParserError error;
        private final Variable literal;

        public PlainLiteralBuilder(TerminalNode node, SolidityType type, Function<String,MethodResult<Object>> cast) {
            final MethodResult<Object> castResult = safeCast(node.getText(), cast);
            if (castResult.isSuccessful()) {
                this.literal = new Variable(type, createLiteralName(), true, castResult.getResult());
                this.error = null;
            } 
            else {
                this.literal = null;
                this.error = new SpecificationParserError(node.getSymbol(), castResult.getErrorMessage());
            }
        }

        @Override
        public SpecificationParserError verify(CodeBlock block) {
            return this.error;
        }

        @Override
        public Variable build(CodeBlock block) {
            return this.literal;
        }

    }

    private static int literalCount = 0;
    private static String createLiteralName() {
        return String.format("$literal%s", ++literalCount);
    }

    private static MethodResult<Object> safeCast(String string, Function<String,MethodResult<Object>> cast) {
        return string == null 
            ? MethodResult.ofError("The 'string' parameter must not be null.")
            : cast.apply(string);
    }

    private static MethodResult<Object> booleanCast(String string) {
        return MethodResult.ofResult(Boolean.parseBoolean(string));
    }

    private static MethodResult<Object> bytesCast(String string) {
        if (!string.startsWith(BYTES_PREFIX)) {
            return MethodResult.ofError(String.format("'%s' is not a valid bytes or address value, as it does not start with '%s'.", string, BYTES_PREFIX));
        }

        if (string.length() < BYTES_PREFIX.length() + 1) {
            return MethodResult.ofError(String.format("'%s' is not a valid bytes or address value, as it does not contain any bytes.", string));
        }

        return MethodResult.ofResult(string);
    }

    private static MethodResult<Object> fixedCast(String string) {
        try {
            final BigDecimal number = new BigDecimal(string);
            return MethodResult.ofResult(number);
        }
        catch (NumberFormatException ex) {
            return MethodResult.ofError(String.format("%s is not a valid fixed value.", string), ex);
        } 
    }

    private static MethodResult<Object> integerCast(String string) {
        try {
            final BigInteger number = new BigInteger(string);
            return MethodResult.ofResult(number);
        }
        catch (NumberFormatException ex) {
            return MethodResult.ofError(String.format("%s is not a valid fixed value.", string), ex);
        } 
    }

    private static MethodResult<Object> stringCast(String string) {
        if (string.length() < 2 || string.charAt(0) != '\"' || string.charAt(string.length() - 1) != '\"') {
            return MethodResult.ofError(String.format("'%' is not a valid string value.", string));
        }

        return MethodResult.ofResult(string.substring(1, string.length() - 1));
    }


}
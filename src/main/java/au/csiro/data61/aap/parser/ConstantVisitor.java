package au.csiro.data61.aap.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import au.csiro.data61.aap.parser.XbelParser.ArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.BooleanArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.ByteAndAddressArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.FixedArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.IntArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.StaticValueContext;
import au.csiro.data61.aap.parser.XbelParser.StaticValueStartRuleContext;
import au.csiro.data61.aap.parser.XbelParser.StringArrayValueContext;
import au.csiro.data61.aap.specification.Constant;
import au.csiro.data61.aap.specification.ProgramState;
import au.csiro.data61.aap.specification.types.ArrayType;
import au.csiro.data61.aap.specification.types.BoolType;
import au.csiro.data61.aap.specification.types.BytesType;
import au.csiro.data61.aap.specification.types.FixedType;
import au.csiro.data61.aap.specification.types.IntegerType;
import au.csiro.data61.aap.specification.types.SolidityType;
import au.csiro.data61.aap.specification.types.StringType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * StaticValueVisitor
 */
public class ConstantVisitor extends StatefulVisitor<SpecificationParserResult<Constant>> {
    private static final String NAME_PREFIX = "CONSTANT";
    private static long constantCount = 0;

    public ConstantVisitor(ProgramState state) {
        super(state);
    }

    @Override
    public SpecificationParserResult<Constant> visitStaticValueStartRule(StaticValueStartRuleContext ctx) {
        return this.visitStaticValue(ctx.staticValue());
    }

    @Override
    public SpecificationParserResult<Constant> visitStaticValue(StaticValueContext ctx) {
        if (ctx.BOOLEAN_VALUE() != null) {
            return this.createBooleanConstant(ctx.BOOLEAN_VALUE());
        }
        else if (ctx.BYTE_AND_ADDRESS_VALUE() != null) {
            return this.createByteAndAddressConstant(ctx.BYTE_AND_ADDRESS_VALUE());
        }
        else if (ctx.FIXED_VALUE() != null) {
            return this.createFixedConstant(ctx.FIXED_VALUE());
        }
        else if (ctx.INT_VALUE() != null) {
            return this.createIntegerConstant(ctx.INT_VALUE());
        }
        else if (ctx.STRING_VALUE() != null) {
            return this.createStringConstant(ctx.STRING_VALUE());
        }
        else if (ctx.arrayValue() != null) {
            return this.visitArrayValue(ctx.arrayValue());
        }
        else {
            return SpecificationParserResult.ofError(ctx.start, String.format("'%s' is not a valid value.", ctx.getText()));
        }
        // TODO: create default instances in types
        // TODO: mapping of byte to array
    }

    @Override
    public SpecificationParserResult<Constant> visitArrayValue(ArrayValueContext ctx) {
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
            return SpecificationParserResult.ofError(ctx.start, String.format("'%s' is not a valid array value.", ctx.getText()));
        }
    }

    @Override
    public SpecificationParserResult<Constant> visitBooleanArrayValue(BooleanArrayValueContext ctx) {
        return this.createArrayConstant(
            ctx, 
            BooleanArrayValueContext::BOOLEAN_VALUE, 
            this::createBooleanConstant, 
            BoolType.DEFAULT_INSTANCE
        );
    }

    @Override
    public SpecificationParserResult<Constant> visitByteAndAddressArrayValue(ByteAndAddressArrayValueContext ctx) {
        return this.createArrayConstant(
            ctx, 
            ByteAndAddressArrayValueContext::BYTE_AND_ADDRESS_VALUE, 
            this::createByteAndAddressConstant, 
            BytesType.DEFAULT_INSTANCE
        );
    }

    @Override
    public SpecificationParserResult<Constant> visitFixedArrayValue(FixedArrayValueContext ctx) {
        return this.createArrayConstant(
            ctx, 
            this::convertToTerminalNodes, 
            this::createFixedConstant, 
            FixedType.DEFAULT_INSTANCE
        );
    }

    private List<TerminalNode> convertToTerminalNodes(FixedArrayValueContext ctx) {
        return ctx.fixedArrayElement().stream().map(e -> e.FIXED_VALUE() != null ? e.FIXED_VALUE() : e.INT_VALUE()).collect(Collectors.toList());
    }

    @Override
    public SpecificationParserResult<Constant> visitIntArrayValue(IntArrayValueContext ctx) {
        return this.createArrayConstant(
            ctx, 
            IntArrayValueContext::INT_VALUE, 
            this::createIntegerConstant, 
            IntegerType.DEFAULT_INSTANCE
        );
    }

    @Override
    public SpecificationParserResult<Constant> visitStringArrayValue(StringArrayValueContext ctx) {
        return this.createArrayConstant(
            ctx, 
            StringArrayValueContext::STRING_VALUE, 
            this::createStringConstant, 
            StringType.DEFAULT_INSTANCE
        );
    }

    public SpecificationParserResult<Constant> createBooleanConstant(TerminalNode node) {
        return this.createConstant(node, BoolType.DEFAULT_INSTANCE);
    }

    public SpecificationParserResult<Constant> createByteAndAddressConstant(TerminalNode node) {
        return this.createConstant(node, BytesType.DEFAULT_INSTANCE);
    }

    public SpecificationParserResult<Constant> createFixedConstant(TerminalNode node) {
        return this.createConstant(node, FixedType.DEFAULT_INSTANCE);
    }

    public SpecificationParserResult<Constant> createIntegerConstant(TerminalNode node) {
        return this.createConstant(node, IntegerType.DEFAULT_INSTANCE);
    }

    public SpecificationParserResult<Constant> createStringConstant(TerminalNode node) {
        return this.createConstant(node, StringType.DEFAULT_INSTANCE);
    }

    @SuppressWarnings("unchecked")
    private <S extends ParserRuleContext, T> SpecificationParserResult<Constant> createArrayConstant(
        S ctx, 
        Function<S,List<TerminalNode>> nodeListCreator,
        Function<TerminalNode, SpecificationParserResult<Constant>> nodeParser,
        SolidityType<?> baseType) {
        final List<T> values = new ArrayList<T>();
        
        for (TerminalNode node : nodeListCreator.apply(ctx)) {
            final SpecificationParserResult<Constant> parseResult = nodeParser.apply(node);
            if (!parseResult.isSuccessful()) {
                return parseResult;
            }
            values.add((T)parseResult.getResult().getValue());
        }

        return SpecificationParserResult.ofResult(
            new Constant(new ArrayType<>(baseType), createName(), values)
        );
    }

    private <T> SpecificationParserResult<Constant> createConstant(TerminalNode node, SolidityType<T> type) {
        final MethodResult<T> parseResult = type.cast(node.getText());
        if (!parseResult.isSuccessful()) {
            return SpecificationParserResult.ofUnsuccessfulMethodResult(parseResult);
        }
        return SpecificationParserResult.ofResult(new Constant(type, createName(), parseResult.getResult()));
    }
    
    private static String createName() {
        return String.format("%s %s", NAME_PREFIX, constantCount++);
    }
}
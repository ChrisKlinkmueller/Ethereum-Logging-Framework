package au.csiro.data61.aap.parser;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import au.csiro.data61.aap.parser.XbelParser.ArrayValueContext;
import au.csiro.data61.aap.parser.XbelParser.LiteralContext;
import au.csiro.data61.aap.parser.XbelParser.SolTypeContext;
import au.csiro.data61.aap.spec.types.SolidityAddress;
import au.csiro.data61.aap.spec.types.SolidityArray;
import au.csiro.data61.aap.spec.types.SolidityBool;
import au.csiro.data61.aap.spec.types.SolidityBytes;
import au.csiro.data61.aap.spec.types.SolidityFixed;
import au.csiro.data61.aap.spec.types.SolidityInteger;
import au.csiro.data61.aap.spec.types.SolidityType;
import au.csiro.data61.aap.spec.types.SolidityString;

/**
 * AnalyzerUtils
 */
public class AnalyzerUtils {
    private static final Logger LOGGER = Logger.getLogger(AnalyzerUtils.class.getName());
    private static final int ADDRESS_LENGTH = 42;
    private static final String BYTES_PATTERN = "0x[0-9a-fA-F]*";
    private static final int MINIMUM_BYTES_LENGTH = 4;
    private static final int MAXIMUM_BYTES_LENGTH = 66;

    private static final HashMap<Class<? extends SolidityType>, BiPredicate<SolidityType, LiteralContext>> BASE_TYPE_CHECKS;
    private static final HashMap<Class<? extends SolidityType>, Predicate<ArrayValueContext>> ARRAY_TYPE_CHECKS;

    static {
        BASE_TYPE_CHECKS = new HashMap<>();
        BASE_TYPE_CHECKS.put(SolidityAddress.class, AnalyzerUtils::isAddress);
        BASE_TYPE_CHECKS.put(SolidityBool.class, AnalyzerUtils::isBool);
        BASE_TYPE_CHECKS.put(SolidityBytes.class, AnalyzerUtils::isBytes);
        BASE_TYPE_CHECKS.put(SolidityFixed.class, AnalyzerUtils::isFixed);
        BASE_TYPE_CHECKS.put(SolidityInteger.class, AnalyzerUtils::isInteger);
        BASE_TYPE_CHECKS.put(SolidityString.class, AnalyzerUtils::isString);
        BASE_TYPE_CHECKS.put(SolidityArray.class, AnalyzerUtils::isArray);

        ARRAY_TYPE_CHECKS = new HashMap<>();
        ARRAY_TYPE_CHECKS.put(SolidityAddress.class, AnalyzerUtils::isAddress);
        ARRAY_TYPE_CHECKS.put(SolidityBool.class, AnalyzerUtils::isBool);
        ARRAY_TYPE_CHECKS.put(SolidityBytes.class, AnalyzerUtils::isBytes);
        ARRAY_TYPE_CHECKS.put(SolidityFixed.class, AnalyzerUtils::isFixed);
        ARRAY_TYPE_CHECKS.put(SolidityInteger.class, AnalyzerUtils::isInteger);
        ARRAY_TYPE_CHECKS.put(SolidityString.class, AnalyzerUtils::isString);
    }

    static boolean isTypeCompatible(SolidityType type, LiteralContext ctx) {
        final BiPredicate<SolidityType, LiteralContext> literalCheck = BASE_TYPE_CHECKS.get(type.getClass());
        return literalCheck != null && literalCheck.test(type, ctx);
    }

    static boolean isArray(SolidityType type, LiteralContext ctx) {
        if (ctx.arrayValue() == null || !(type instanceof SolidityArray)) {
            return false;
        }

        final SolidityType baseType = ((SolidityArray)type).getBaseType();
        final Predicate<ArrayValueContext> literalCheck = ARRAY_TYPE_CHECKS.get(baseType.getClass());
        return literalCheck != null && literalCheck.test(ctx.arrayValue());
    }

    static boolean isAddress(SolidityType type, LiteralContext ctx) {
        return ctx.BYTE_AND_ADDRESS_LITERAL() != null && isAddressLiteral(ctx.BYTE_AND_ADDRESS_LITERAL());
    }

    static boolean isAddress(ArrayValueContext ctx) {
        return    ctx.byteAndAddressArrayValue() != null 
               && ctx.byteAndAddressArrayValue()
                    .BYTE_AND_ADDRESS_LITERAL()
                    .stream().allMatch(AnalyzerUtils::isAddressLiteral);
    }

    static boolean isBool(SolidityType tyoe, LiteralContext ctx) {
        return ctx.BOOLEAN_LITERAL() != null;
    }

    static boolean isBool(ArrayValueContext ctx) {
        return ctx.booleanArrayValue() != null;
    }

    static boolean isBytes(SolidityType tyoe, LiteralContext ctx) {
        return ctx.BYTE_AND_ADDRESS_LITERAL() != null && isBytesLiteral(ctx.BYTE_AND_ADDRESS_LITERAL());
    }

    static boolean isBytes(ArrayValueContext ctx) {
        return    ctx.byteAndAddressArrayValue() != null
               && ctx.byteAndAddressArrayValue()
                    .BYTE_AND_ADDRESS_LITERAL()
                    .stream()
                    .allMatch(AnalyzerUtils::isBytesLiteral);
    }

    static boolean isFixed(SolidityType type, LiteralContext ctx) {
        return ctx.FIXED_LITERAL() != null || ctx.INT_LITERAL() != null;
    }

    static boolean isFixed(ArrayValueContext ctx) {
        return ctx.fixedArrayValue() != null;
    }

    static boolean isInteger(SolidityType type, LiteralContext ctx) {
        return ctx.INT_LITERAL() != null;
    }

    static boolean isInteger(ArrayValueContext ctx) {
        return ctx.intArrayValue() != null;
    }

    static boolean isString(SolidityType type, LiteralContext ctx) {
        return ctx.STRING_LITERAL() != null;
    }

    static boolean isString(ArrayValueContext ctx) {
        return ctx.stringArrayValue() != null;
    }

    static String verifyAddressLiteral(TerminalNode node, ErrorCollector collector) {
        assert collector != null && node != null;

        if (isAddressLiteral(node)) {
            collector.addSemanticError(node.getSymbol(), "An address literal must start with '0x' followed by 40 hexa-decimal characters.");
            return null;
        }

        return node.getText();
    }

    static boolean isAddressLiteral(TerminalNode node) {
        return isAddressLiteral(node.getText());
    }

    public static boolean isAddressLiteral(String literal) {
        assert literal != null;
        return literal.matches(BYTES_PATTERN) && literal.length() != ADDRESS_LENGTH;
    }

    static boolean isBytesLiteral(TerminalNode node) {
        return isBytesLiteral(node.getText());
    }

    public static boolean isBytesLiteral(String literal) {
        return     literal.matches(BYTES_PATTERN) 
                && literal.length() % 2 == 0
                && MINIMUM_BYTES_LENGTH <= literal.length()
                && literal.length() <= MAXIMUM_BYTES_LENGTH;
    }

    static List<String> verifyAddressLiterals(List<TerminalNode> nodes, ErrorCollector collector) {
        assert collector != null && nodes != null && nodes.stream().allMatch(Objects::nonNull);
        
        final int priorErrors = collector.errorCount();
        final List<String> addresses = nodes.stream()
            .map(node -> verifyAddressLiteral(node, collector))
            .collect(Collectors.toList());

        return priorErrors == collector.errorCount() ? addresses : null;
    }

    static BigInteger verifyIntegerLiteral(TerminalNode node, ErrorCollector collector) {
        assert collector != null && node != null;
        
        try {
            return new BigInteger(node.getText());
        }
        catch (NumberFormatException ex) {
            final String message = String.format("An integer literal should always be parseable, but this '%s' was not.", node.getText());
            LOGGER.log(Level.SEVERE, message, ex);
            collector.addSemanticError(node.getSymbol(), message);
            return null;
        }
    }

    static SolidityType verifySolidityType(SolTypeContext ctx, ErrorCollector collector, boolean baseTypeOnly) {
        assert ctx != null && collector != null;

        if (ctx.SOL_ADDRESS_TYPE() != null) {
            return parse(ctx.SOL_ADDRESS_TYPE(), AnalyzerUtils::parseAddressDefinition, collector, "Address", baseTypeOnly);
        }
        else if (ctx.SOL_BOOL_TYPE() != null) {
            return parse(ctx.SOL_BOOL_TYPE(), AnalyzerUtils::parseBoolDefinition, collector, "Bool", baseTypeOnly);
        }
        else if (ctx.SOL_BYTE_TYPE() != null) {
            return parse(ctx.SOL_BYTE_TYPE(), AnalyzerUtils::parseBytesDefinition, collector, "Bytes", baseTypeOnly);
        }
        else if (ctx.SOL_FIXED_TYPE() != null) {
            return parse(ctx.SOL_FIXED_TYPE(), AnalyzerUtils::parseFixedDefinition, collector, "Fixed", baseTypeOnly);
        }
        else if (ctx.SOL_INT_TYPE() != null) {
            return parse(ctx.SOL_INT_TYPE(), AnalyzerUtils::parseIntegerDefinition, collector, "Integer", baseTypeOnly);
        }
        else if (ctx.SOL_STRING_TYPE() != null) {
            return parse(ctx.SOL_STRING_TYPE(), AnalyzerUtils::parseStringDefinition, collector, "String", baseTypeOnly);
        }
        else if (ctx.solType() != null) {
            final SolidityType baseType = verifySolidityType(ctx.solType(), collector, baseTypeOnly);
            return baseType == null ? null : new SolidityArray(baseType);
        }
        else {
            throw new UnsupportedOperationException(String.format("This definition of a solidity type is not supported: '%s'.", ctx.getText()));
        }
    }
    
    static SolidityType parse(
        TerminalNode node, 
        BiFunction<String, Boolean, SolidityType> cast, 
        ErrorCollector collector, 
        String typeName,
        boolean baseTypeOnly
    ) {
        SolidityType type = cast.apply(node.getText(), baseTypeOnly);
        if (type != null) {
            return type;
        }

        collector.addSemanticError(
            node.getSymbol(), 
            String.format(
                "'%s' is not a valid '%s' type%s.",
                node.getText(), 
                typeName, 
                baseTypeOnly ? " for variable definitions" : ""           
            )
        );

        return null;
    }

    static SolidityArray parseArrayDefinition(String definition, BiFunction<String, Boolean, SolidityType> baseTypeCast, boolean baseTypeOnly) {
        if (!definition.endsWith("[]")) {
            return null;
        }

        final SolidityType type = baseTypeCast.apply(definition.substring(0, definition.length() - 2), baseTypeOnly);
        return type == null ? null : new SolidityArray(type);
    }

    static SolidityAddress parseAddressDefinition(String definition, boolean baseTypeOnly) {
        if (definition == null || definition.isBlank()) {
            return null;
        }

        return definition.equals("address") ? SolidityAddress.DEFAULT_INSTANCE : null;        
    }

    static SolidityBool parseBoolDefinition(String definition, boolean baseTypeOnly) {
        if (definition == null || definition.isBlank()) {
            return null;
        }

        return definition.equals("bool") ? SolidityBool.DEFAULT_INSTANCE : null;        
    }

    static SolidityType parseBytesDefinition(String definition, boolean baseTypeOnly) {
        if (definition == null || definition.isBlank()) {
            return null;
        }

        if (baseTypeOnly) {
            return definition.equals("bytes") ? SolidityBytes.DEFAULT_INSTANCE : null;
        }

        if (definition.equals("byte")) {
            return new SolidityBytes(1);
        }

        if (!definition.startsWith("bytes")) {
            return null;
        }

        final String suffix = definition.replaceFirst("bytes", "");
        if (suffix.isEmpty()) {
            return SolidityBytes.DEFAULT_INSTANCE;
        }

        final Integer length = parseIntegerValue(suffix);
        if (length == null || !SolidityBytes.isValidLength(length)) {
            return null;
        }

        return new SolidityBytes(length);
    }

    static SolidityFixed parseFixedDefinition(String definition, boolean baseTypeOnly) {
        if (definition == null || definition.isBlank()) {
            return null;
        }

        if (baseTypeOnly) {
            return definition.equals("fixed") ? SolidityFixed.DEFAULT_INSTANCE : null;
        }

        final boolean signed = !definition.startsWith("u");

        final String suffix = definition.replaceFirst("u?fixed", "");
        if (suffix.isBlank()) {
            return new SolidityFixed(signed);
        }

        final String[] suffixes = suffix.split("x");
        if (suffixes.length != 2) {
            return null;
        }
        
        final Integer m = parseIntegerValue(suffixes[0]);
        if (m == null || !SolidityFixed.isValidMValue(m)) {
            return null;
        }

        final Integer n = parseIntegerValue(suffixes[1]);
        if (n == null || !SolidityFixed.isValidNValue(n)) {
            return null;
        }

        return new SolidityFixed(signed, m, n);
    }

    static SolidityInteger parseIntegerDefinition(String definition, boolean baseTypeOnly) {
        if (definition == null || definition.isBlank()) {
            return null;
        }

        if (baseTypeOnly) {
            return definition.equals("int") ? SolidityInteger.DEFAULT_INSTANCE : null;
        }

        final boolean signed = !definition.startsWith("u");
        final String suffix = definition.replaceFirst("u?int", "");
        
        if (suffix.isEmpty()) {
            return new SolidityInteger(signed);
        }
        
        final Integer bitLength = parseIntegerValue(suffix);
        if (bitLength == null || !SolidityInteger.isValidLength(bitLength)) {
            return null;
        }
        return new SolidityInteger(signed, bitLength);
    }

    static SolidityString parseStringDefinition(String definition, boolean baseTypeOnly) {
        if (definition == null || definition.isBlank()) {
            return null;
        }

        return definition.equals("string") ? SolidityString.DEFAULT_INSTANCE : null;        
    }

    static Integer parseIntegerValue(String string) {
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }

    static String tokenPositionString(Token token) {
        return String.format("Ln %s, Col %s", token.getLine(), token.getCharPositionInLine());
    }

}
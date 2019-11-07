package au.csiro.data61.aap.parser;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.TerminalNode;

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
class AnalyzerUtils {
    private static final Logger LOGGER = Logger.getLogger(AnalyzerUtils.class.getName());
    private static final int ADDRESS_LENGTH = 42;

    static String verifyAddressLiteral(TerminalNode node, ErrorCollector collector) {
        assert collector != null && node != null;

        if (node.getText().length() != ADDRESS_LENGTH) {
            collector.addSemanticError(node.getSymbol(), "An address literal must start with '0x' followed by 40 hexa-decimal characters.");
            return null;
        }

        return node.getText();
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

    static SolidityType verifySolidityType(SolTypeContext ctx, ErrorCollector collector) {
        assert ctx != null && collector != null;

        // ADDRESS TYPES
        if (ctx.SOL_ADDRESS_TYPE() != null) {
            return parse(ctx.SOL_ADDRESS_TYPE(), AnalyzerUtils::parseAddressDefinition, collector, "Address");
        }
        else if (ctx.SOL_ADDRESS_ARRAY_TYPE() != null) {
            return parse(ctx.SOL_ADDRESS_TYPE(), def -> AnalyzerUtils.parseArrayDefinition(def, AnalyzerUtils::parseAddressDefinition), collector, "Address Array");
        }
        // BOOL TYPES
        else if (ctx.SOL_BOOL_TYPE() != null) {
            return parse(ctx.SOL_BOOL_TYPE(), AnalyzerUtils::parseBoolDefinition, collector, "Bool");
        }
        else if (ctx.SOL_BOOL_ARRAY_TYPE() != null) {
            return parse(ctx.SOL_BOOL_ARRAY_TYPE(), def -> AnalyzerUtils.parseArrayDefinition(def, AnalyzerUtils::parseBoolDefinition), collector, "Bool Array");
        }
        // BYTE TYPES
        else if (ctx.SOL_BYTE_TYPE() != null) {
            return parse(ctx.SOL_BYTE_TYPE(), AnalyzerUtils::parseBytesDefinition, collector, "Bytes");
        }
        else if (ctx.SOL_BYTE_ARRAY_TYPE() != null) {
            return parse(ctx.SOL_BYTE_ARRAY_TYPE(), def -> AnalyzerUtils.parseArrayDefinition(def, AnalyzerUtils::parseBytesDefinition), collector, "Bytes Array");
        }
        // FIXED TYPES
        else if (ctx.SOL_FIXED_TYPE() != null) {
            return parse(ctx.SOL_FIXED_TYPE(), AnalyzerUtils::parseFixedDefinition, collector, "Fixed");
        }
        else if (ctx.SOL_FIXED_ARRAY_TYPE() != null) {
            return parse(ctx.SOL_FIXED_ARRAY_TYPE(), def -> AnalyzerUtils.parseArrayDefinition(def, AnalyzerUtils::parseBoolDefinition), collector, "Fixed Array");
        }
        // INT TYPES
        else if (ctx.SOL_INT_TYPE() != null) {
            return parse(ctx.SOL_INT_TYPE(), AnalyzerUtils::parseIntegerDefinition, collector, "Integer");
        }
        else if (ctx.SOL_INT_ARRAY_TYPE() != null) {
            return parse(ctx.SOL_INT_ARRAY_TYPE(), def -> AnalyzerUtils.parseArrayDefinition(def, AnalyzerUtils::parseIntegerDefinition), collector, "Integer Array");
        }
        // STRING TYPES
        else if (ctx.SOL_STRING_TYPE() != null) {
            return parse(ctx.SOL_STRING_TYPE(), AnalyzerUtils::parseStringDefinition, collector, "String");
        }
        // UNKNOWN TYPES
        else {
            throw new UnsupportedOperationException(String.format("This definition of a solidity type is not supported: '%s'.", ctx.getText()));
        }
    }
    
    static SolidityType parse(TerminalNode node, Function<String, SolidityType> cast, ErrorCollector collector, String typeName) {
        SolidityType type = cast.apply(node.getText());
        if (type != null) {
            return type;
        }

        collector.addSemanticError(node.getSymbol(), String.format("'%s' is not a valid '%s' type", node.getText(), typeName));
        return null;
    }

    static SolidityArray parseArrayDefinition(String definition, Function<String, SolidityType> baseTypeCast) {
        if (!definition.endsWith("[]")) {
            return null;
        }

        final SolidityType type = baseTypeCast.apply(definition.substring(0, definition.length() - 2));
        return type == null ? null : new SolidityArray(type);
    }

    static SolidityAddress parseAddressDefinition(String definition) {
        if (definition == null || definition.isBlank()) {
            return null;
        }

        return definition.equals("address") ? SolidityAddress.DEFAULT_INSTANCE : null;        
    }

    static SolidityBool parseBoolDefinition(String definition) {
        if (definition == null || definition.isBlank()) {
            return null;
        }

        return definition.equals("bool") ? SolidityBool.DEFAULT_INSTANCE : null;        
    }

    static SolidityType parseBytesDefinition(String definition) {
        if (definition == null || definition.isBlank()) {
            return null;
        }

        if (definition.equals("byte")) {
            return SolidityBytes.DEFAULT_INSTANCE;
        }

        if (!definition.startsWith("bytes")) {
            return null;
        }

        final String suffix = definition.replaceFirst("bytes", "");
        if (suffix.isEmpty()) {
            return new SolidityArray(SolidityBytes.DEFAULT_INSTANCE);
        }

        final Integer length = parseIntegerValue(suffix);
        if (length == null || !SolidityBytes.isValidLength(length)) {
            return null;
        }

        return new SolidityBytes(length);
    }

    static SolidityFixed parseFixedDefinition(String definition) {
        if (definition == null || definition.isBlank()) {
            return null;
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

    static SolidityInteger parseIntegerDefinition(String definition) {
        if (definition == null || definition.isBlank()) {
            return null;
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

    static SolidityString parseStringDefinition(String definition) {
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

}
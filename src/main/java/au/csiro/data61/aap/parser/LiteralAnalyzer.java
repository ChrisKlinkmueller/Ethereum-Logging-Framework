package au.csiro.data61.aap.parser;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * LiteralVerifier
 */
class LiteralAnalyzer {
    private static final Logger LOGGER = Logger.getLogger(LiteralAnalyzer.class.getName());
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
    
}
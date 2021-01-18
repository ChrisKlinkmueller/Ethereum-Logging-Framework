package blf.blockchains.hyperledger.helpers;

import blf.blockchains.hyperledger.HyperledgerListener;
import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;
import blf.grammar.BcqlParser;
import blf.util.TypeUtils;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public interface HyperledgerListenerHelper {

    static List<String> parseAddressListCtx(HyperledgerProgramState hyperledgerProgramState, BcqlParser.AddressListContext addressListCtx) {

        if (addressListCtx == null) {
            return new LinkedList<>();
        }

        final List<TerminalNode> addressListStringLiteral = addressListCtx.STRING_LITERAL();
        final List<TerminalNode> addressListBytesLiteral = addressListCtx.BYTES_LITERAL();
        final BcqlParser.VariableNameContext addressListVariableNameCtx = addressListCtx.variableName();
        final TerminalNode addressListAny = addressListCtx.KEY_ANY();

        if (addressListAny != null) {
            return new LinkedList<>();
        }

        List<String> addressNames = null;

        if (addressListVariableNameCtx != null) {
            final String variableName = addressListVariableNameCtx.getText();
            final ValueAccessor accessor = ValueAccessor.createVariableAccessor(variableName);

            String value = null;
            try {
                value = (String) accessor.getValue(hyperledgerProgramState);
            } catch (ClassCastException e) {
                String errorMsg = String.format(
                    "Variable '%s' in manifest file is not an instance of String.",
                    addressListVariableNameCtx.getText()
                );

                hyperledgerProgramState.getExceptionHandler().handleExceptionAndDecideOnAbort(errorMsg, e);
            } catch (ProgramException e) {
                hyperledgerProgramState.getExceptionHandler().handleExceptionAndDecideOnAbort("Unexpected exception occurred.", e);
            }

            if (value != null) {
                addressNames = new LinkedList<>(Collections.singletonList(value));
            }
        }

        if (addressListStringLiteral != null && !addressListStringLiteral.isEmpty()) {
            addressNames = addressListStringLiteral.stream()
                .map(ParseTree::getText)
                .map(TypeUtils::parseStringLiteral)
                .collect(Collectors.toList());
        }

        if (addressListBytesLiteral != null && !addressListBytesLiteral.isEmpty()) {
            addressNames = addressListBytesLiteral.stream()
                .map(ParseTree::getText)
                .map(TypeUtils::parseBytesLiteral)
                .collect(Collectors.toList());
        }

        if (addressNames == null) {
            hyperledgerProgramState.getExceptionHandler()
                .handleExceptionAndDecideOnAbort("Variable 'addressNames' is null.", new NullPointerException());
        }

        return addressNames;
    }

    static Triple<String, List<Pair<String, String>>, List<String>> parseLogEntryFilterCtx(
        HyperledgerProgramState hyperledgerProgramState,
        BcqlParser.LogEntryFilterContext logEntryCtx
    ) {

        final ExceptionHandler exceptionHandler = hyperledgerProgramState.getExceptionHandler();
        final BcqlParser.AddressListContext addressListCtx = logEntryCtx.addressList();
        final BcqlParser.LogEntrySignatureContext logEntrySignatureCtx = logEntryCtx.logEntrySignature();

        List<BcqlParser.LogEntryParameterContext> logEntryParameterContextList = logEntrySignatureCtx.logEntryParameter();

        if (logEntryParameterContextList == null) {
            exceptionHandler.handleExceptionAndDecideOnAbort(
                "Variable 'logEntryParameterContextList' is null.",
                new NullPointerException()
            );

            logEntryParameterContextList = new LinkedList<>();
        }

        final String eventName = logEntrySignatureCtx.methodName.getText();

        final List<Pair<String, String>> entryParameters = new LinkedList<>();

        for (BcqlParser.LogEntryParameterContext logEntryParameterCtx : logEntryParameterContextList) {
            entryParameters.add(new Pair<>(logEntryParameterCtx.solType().getText(), logEntryParameterCtx.variableName().getText()));
        }

        List<String> addressNames = HyperledgerListenerHelper.parseAddressListCtx(hyperledgerProgramState, addressListCtx);

        return new Triple<>(eventName, entryParameters, addressNames);
    }

    static Pair<BigInteger, BigInteger> parseBlockFilterCtx(
        HyperledgerProgramState hyperledgerProgramState,
        BcqlParser.BlockFilterContext blockCtx
    ) {
        return new Pair<>(parseBlockNumber(hyperledgerProgramState, blockCtx.from), parseBlockNumber(hyperledgerProgramState, blockCtx.to));
    }

    private static BigInteger parseBlockNumber(
        HyperledgerProgramState hyperledgerProgramState,
        BcqlParser.BlockNumberContext blockNumberContext
    ) {
        final BcqlParser.ValueExpressionContext valueExpressionCtx = blockNumberContext.valueExpression();
        final TerminalNode continuousKey = blockNumberContext.KEY_CONTINUOUS();
        final TerminalNode earliestKey = blockNumberContext.KEY_EARLIEST();
        final TerminalNode currentKey = blockNumberContext.KEY_CURRENT();

        if (continuousKey != null) {
            return BigInteger.valueOf(Long.MAX_VALUE);
        }

        if (earliestKey != null) {
            return BigInteger.ZERO;
        }

        if (currentKey != null) {
            return null;
        }

        final BcqlParser.LiteralContext literalCtx = valueExpressionCtx.literal();
        final BcqlParser.VariableNameContext variableNameCtx = valueExpressionCtx.variableName();

        final ExceptionHandler exceptionHandler = hyperledgerProgramState.getExceptionHandler();

        BigInteger blockNumber = null;
        if (literalCtx != null && literalCtx.INT_LITERAL() != null) {
            // Normal int
            blockNumber = TypeUtils.parseIntLiteral(literalCtx.INT_LITERAL().getText());
        } else if (variableNameCtx != null) {
            // Variable
            final String fromBlockVariableName = variableNameCtx.getText();
            final ValueAccessor fromBlockNumberAccessor = ValueAccessor.createVariableAccessor(fromBlockVariableName);

            try {
                blockNumber = (BigInteger) fromBlockNumberAccessor.getValue(hyperledgerProgramState);
            } catch (NumberFormatException e) {
                String errorMsg = String.format("Variable '%s' in manifest file is not an instance of Int.", fromBlockVariableName);

                exceptionHandler.handleExceptionAndDecideOnAbort(errorMsg, e);
            } catch (ProgramException e) {
                hyperledgerProgramState.getExceptionHandler().handleExceptionAndDecideOnAbort("Unexpected exception occurred.", e);
            }

        } else {
            // Fallback
            exceptionHandler.handleExceptionAndDecideOnAbort(
                "Hyperledger BLOCKS (`from`)() parameter should be an Integer or a valid variable name.",
                new NullPointerException()
            );
        }

        return blockNumber;
    }
}

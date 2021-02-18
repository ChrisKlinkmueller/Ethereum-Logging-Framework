package blf.blockchains.hyperledger.helpers;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.values.ValueAccessor;
import blf.grammar.BcqlParser;
import blf.util.TypeUtils;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public interface HyperledgerInstructionHelper {

    BigInteger CURRENT_BLOCK = BigInteger.valueOf(-1);

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
                    "Variable '%s' in manifest file is not an instance of a String.",
                    addressListVariableNameCtx.getText()
                );

                ExceptionHandler.getInstance().handleException(errorMsg, e);
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
            ExceptionHandler.getInstance().handleException("Variable 'addressNames' is null.", new NullPointerException());
        }

        return addressNames;
    }

    static Pair<String, List<HyperledgerQueryParameters>> parseSmartContractFilterCtx(
        HyperledgerProgramState hyperledgerProgramState,
        BcqlParser.SmartContractFilterContext smartContractFilterCtx
    ) {

        String contractAddress = null;

        if (smartContractFilterCtx.contractAddress.variableName() != null) {
            final String variableName = smartContractFilterCtx.contractAddress.getText();
            final ValueAccessor valueAccessor = ValueAccessor.createVariableAccessor(variableName);

            try {
                contractAddress = (String) valueAccessor.getValue(hyperledgerProgramState);
            } catch (ClassCastException e) {
                String errorMsg = String.format(
                    "Variable '%s' in manifest file is not an instance of a String.",
                    smartContractFilterCtx.contractAddress.getText()
                );

                ExceptionHandler.getInstance().handleException(errorMsg, e);
            }
        }

        if (smartContractFilterCtx.contractAddress.literal() != null) {
            contractAddress = smartContractFilterCtx.contractAddress.getText();
        }

        List<BcqlParser.SmartContractQueryContext> smartContractQueries = smartContractFilterCtx.smartContractQuery();

        List<HyperledgerQueryParameters> hyperledgerQueryParameters = new LinkedList<>();

        for (BcqlParser.SmartContractQueryContext smartContractQuery : smartContractQueries) {

            if (smartContractQuery.publicVariableQuery() != null) {
                hyperledgerQueryParameters.add(parsePublicVariableQuery(smartContractQuery));
            }

            if (smartContractQuery.publicFunctionQuery() != null) {
                hyperledgerQueryParameters.add(parsePublicFunctionQuery(smartContractQuery));
            }
        }

        return new Pair<>(contractAddress, hyperledgerQueryParameters);
    }

    static HyperledgerQueryParameters parsePublicVariableQuery(BcqlParser.SmartContractQueryContext smartContractQuery) {
        BcqlParser.SmartContractParameterContext smartContractParameterContext = smartContractQuery.publicVariableQuery()
            .smartContractParameter();

        Pair<String, String> outputParameter = new Pair<>(
            smartContractParameterContext.solType().getText(),
            smartContractParameterContext.variableName().getText()
        );
        return (new HyperledgerQueryParameters(outputParameter));
    }

    static HyperledgerQueryParameters parsePublicFunctionQuery(BcqlParser.SmartContractQueryContext smartContractQuery) {

        BcqlParser.PublicFunctionQueryContext publicFunctionQuery = smartContractQuery.publicFunctionQuery();

        List<BcqlParser.SmartContractParameterContext> smartContractParameters = publicFunctionQuery.smartContractParameter();
        List<Pair<String, String>> outputParameters = new LinkedList<>();

        for (BcqlParser.SmartContractParameterContext smartContractParameter : smartContractParameters) {
            Pair<String, String> outputParameter = new Pair<>(
                smartContractParameter.solType().getText(),
                smartContractParameter.variableName().getText()
            );
            outputParameters.add(outputParameter);
        }

        final String methodName = publicFunctionQuery.methodName.getText();

        List<BcqlParser.SmartContractQueryParameterContext> smartContractQueryParameters = publicFunctionQuery
            .smartContractQueryParameter();
        List<Pair<String, String>> inputParameters = new LinkedList<>();

        for (BcqlParser.SmartContractQueryParameterContext smartContractQueryParameter : smartContractQueryParameters) {
            if (smartContractQueryParameter.solType() != null && smartContractQueryParameter.literal() != null) {
                Pair<String, String> outputParameter = new Pair<>(
                    smartContractQueryParameter.solType().getText(),
                    smartContractQueryParameter.literal().getText()
                );
                inputParameters.add(outputParameter);
            }

            if (smartContractQueryParameter.variableName() != null) {
                // TODO (Tom Knoche): Name and TYPE have to be refered from the Valuestore (achieved only name)
                inputParameters.add(null);
            }

        }
        return (new HyperledgerQueryParameters(outputParameters, methodName, inputParameters));
    }

    static Triple<String, List<Pair<String, String>>, List<String>> parseLogEntryFilterCtx(
        HyperledgerProgramState hyperledgerProgramState,
        BcqlParser.LogEntryFilterContext logEntryCtx
    ) {

        final BcqlParser.AddressListContext addressListCtx = logEntryCtx.addressList();
        final BcqlParser.LogEntrySignatureContext logEntrySignatureCtx = logEntryCtx.logEntrySignature();

        List<BcqlParser.LogEntryParameterContext> logEntryParameterContextList = logEntrySignatureCtx.logEntryParameter();

        if (logEntryParameterContextList == null) {
            ExceptionHandler.getInstance().handleException("Variable 'logEntryParameterContextList' is null.", new NullPointerException());

            logEntryParameterContextList = new LinkedList<>();
        }

        final String eventName = logEntrySignatureCtx.methodName.getText();

        final List<Pair<String, String>> entryParameters = new LinkedList<>();

        for (BcqlParser.LogEntryParameterContext logEntryParameterCtx : logEntryParameterContextList) {
            entryParameters.add(new Pair<>(logEntryParameterCtx.solType().getText(), logEntryParameterCtx.variableName().getText()));
        }

        List<String> addressNames = HyperledgerInstructionHelper.parseAddressListCtx(hyperledgerProgramState, addressListCtx);

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
            return CURRENT_BLOCK;
        }

        final BcqlParser.LiteralContext literalCtx = valueExpressionCtx.literal();
        final BcqlParser.VariableNameContext variableNameCtx = valueExpressionCtx.variableName();

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

                ExceptionHandler.getInstance().handleException(errorMsg, e);
            }

        } else {
            // Fallback
            ExceptionHandler.getInstance()
                .handleException(
                    "Hyperledger BLOCKS (`from`)() parameter should be an Integer or a valid variable name.",
                    new NullPointerException()
                );
        }

        return blockNumber;
    }

    // inspired from: https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    static String bytesToHexString(byte[] bytes) {
        final byte[] hexArray = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    static PrivateKey readPrivateKeyFromFile(String path) {
        PrivateKey privateKey = null;
        try {
            Path serverKeyPath = Paths.get(path);

            byte[] serverKeyBytes = Files.readAllBytes(serverKeyPath);

            String key = new String(serverKeyBytes, Charset.defaultCharset());

            String privateKeyPEM = key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

            byte[] base64EncodedPrivateKey = Base64.decodeBase64(privateKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(base64EncodedPrivateKey);

            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (NoSuchFileException e) {
            ExceptionHandler.getInstance().handleException(String.format("Private key file does not exist on path '%s'", path), e);
        } catch (IOException e) {
            ExceptionHandler.getInstance()
                .handleException(String.format("Input error when trying to read from private key file: %s.", path), e);
        } catch (NoSuchAlgorithmException e) {
            ExceptionHandler.getInstance().handleException("Provided algorithm not found.", e);
        } catch (InvalidKeySpecException e) {
            ExceptionHandler.getInstance().handleException("Provided key spec is invalid.", e);
        } catch (Exception e) {
            ExceptionHandler.getInstance().handleException("Unhandled exception has occurred.", e);
        }

        return privateKey;
    }
}

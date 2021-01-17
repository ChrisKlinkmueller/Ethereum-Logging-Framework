package blf.blockchains.hyperledger.helpers;

import blf.blockchains.hyperledger.state.HyperledgerProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;
import blf.grammar.BcqlParser;
import blf.util.TypeUtils;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class HyperledgerListenerHelper {

    private final HyperledgerProgramState hyperledgerProgramState;
    private final ExceptionHandler exceptionHandler;

    public HyperledgerListenerHelper(HyperledgerProgramState hyperledgerProgramState) {
        this.hyperledgerProgramState = hyperledgerProgramState;
        this.exceptionHandler = hyperledgerProgramState.getExceptionHandler();
    }

    public List<String> parseAddressListContext(BcqlParser.AddressListContext addressListCtx) {

        final List<TerminalNode> addressListStringLiteral = addressListCtx.STRING_LITERAL();
        final List<TerminalNode> addressListBytesLiteral = addressListCtx.BYTES_LITERAL();
        final BcqlParser.VariableNameContext addressListVariableNameCtx = addressListCtx.variableName();
        final TerminalNode addressListAny = addressListCtx.KEY_ANY();

        if (addressListAny != null) {
            return new LinkedList<>();
        }

        List<String> addressNames = null;

        if (addressListVariableNameCtx != null) {
            // TODO: this part does not work (value is always null)
            // because exitVariableDeclarationStatement is not defined in HyperledgerListener
            // hence the specified variables are not stored anywhere
            // -> define a process of storing variables for hyperledger
            String variableName = addressListVariableNameCtx.getText();
            final ValueAccessor accessor = ValueAccessor.createVariableAccessor(variableName);
            String value = "";
            try {
                value = (String) accessor.getValue(this.hyperledgerProgramState);
            } catch (ClassCastException e) {
                String errorMsg = String.format(
                    "Variable '%s' in manifest file is not an instance of String.",
                    addressListVariableNameCtx.getText()
                );

                this.exceptionHandler.handleExceptionAndDecideOnAbort(errorMsg, e);
            } catch (ProgramException e) {
                this.exceptionHandler.handleExceptionAndDecideOnAbort("Unexpected exception occurred.", e);
            }

            addressNames = new LinkedList<>(Collections.singletonList(value));
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
            this.exceptionHandler.handleExceptionAndDecideOnAbort("Variable 'addressNames' is null.", new NullPointerException());
        }

        return addressNames;
    }
}

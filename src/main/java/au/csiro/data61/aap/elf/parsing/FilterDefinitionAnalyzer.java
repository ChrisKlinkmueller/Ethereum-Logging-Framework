package au.csiro.data61.aap.elf.parsing;

import java.math.BigInteger;

import org.antlr.v4.runtime.tree.TerminalNode;

import au.csiro.data61.aap.elf.parsing.EthqlParser.AddressListContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.BlockNumberContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.TransactionFilterContext;
import au.csiro.data61.aap.elf.util.TypeUtils;

/**
 * FilterDefinitionAnalyzer
 */
public class FilterDefinitionAnalyzer extends SemanticAnalyzer {
    private final VariableExistenceAnalyzer variableAnalyzer;

    public FilterDefinitionAnalyzer(ErrorCollector errorCollector, VariableExistenceAnalyzer variableAnalyzer) {
        super(errorCollector);
        assert variableAnalyzer != null;
        this.variableAnalyzer = variableAnalyzer;
    }

    @Override
    public void clear() {
    }

    // #region block filter

    @Override
    public void exitBlockFilter(BlockFilterContext ctx) {
        if (ctx.from.INT_LITERAL() != null && ctx.to.INT_LITERAL() != null) {
            final BigInteger from = TypeUtils.integerFromLiteral(ctx.from.getText());
            final BigInteger to = TypeUtils.integerFromLiteral(ctx.to.getText());
            if (from.compareTo(to) > 0) {
                this.errorCollector.addSemanticError(ctx.from.start,
                        "The 'from' parameter must be smaller than or equal to the 'to' parameter.");
            }
            return;
        }

        if (ctx.from.KEY_CONTINUOUS() != null) {
            this.errorCollector.addSemanticError(ctx.from.start, "The 'from' parameter cannot be set to CONTINUOUS.");
        }

        if (ctx.to.KEY_EARLIEST() != null) {
            this.errorCollector.addSemanticError(ctx.to.start, "The 'to' parameter cannot be set to EARLIEST.");
        }

        if (ctx.from.variableName() != null) {
            this.verifyBlockNumberVariable(ctx.from);
        }

        if (ctx.to.variableName() != null) {
            this.verifyBlockNumberVariable(ctx.to);
        }
    }

    private void verifyBlockNumberVariable(BlockNumberContext ctx) {
        String solType = this.variableAnalyzer.getVariableType(ctx.variableName().getText());
        if (solType != null && !TypeUtils.isIntegerType(solType)) {
            this.addError(ctx.start, String.format("'%s' must be an integer variable.", ctx.variableName().getText()));
        }
    }

    // #endregion block filter

    // #region transaction filter

    @Override
    public void exitTransactionFilter(TransactionFilterContext ctx) {
        if (ctx.senders != null) {
            this.verifyAddressList(ctx.senders);
        }

        this.verifyAddressList(ctx.recipients);
    }

    private void verifyAddressList(AddressListContext ctx) {
        if (ctx.variableName() != null) {
            String solType = this.variableAnalyzer.getVariableType(ctx.variableName().getText());
            if (solType != null && !TypeUtils.isAddressType(solType)) {
                this.addError(ctx.start, String.format("'%s' must be a string variable", ctx.variableName().getText()));
            }
            return;
        }

        if (ctx.BYTES_LITERAL() != null) {
            for (TerminalNode node : ctx.BYTES_LITERAL()) {
                if (!TypeUtils.isAddressLiteral(node.getText())) {
                    this.addError(node.getSymbol(), String.format("'%s' is not a valid address literal.", node.getText()));
                }
            }
        }
    }

    //#endregion transaction filter


    //#region log entry filter

    @Override
    public void exitLogEntryFilter(LogEntryFilterContext ctx) {
        this.verifyAddressList(ctx.addressList());
    }

    //#endregion log entry filter
}
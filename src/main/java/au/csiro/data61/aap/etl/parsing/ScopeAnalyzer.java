package au.csiro.data61.aap.etl.parsing;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ParserRuleContext;

import au.csiro.data61.aap.etl.TypeUtils;
import au.csiro.data61.aap.etl.parsing.EthqlParser.AddressListContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.BlockFilterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.BlockNumberContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.LogEntryFilterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.LogEntryParameterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.ScopeContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.SmartContractVariableContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.SmartContractsFilterContext;
import au.csiro.data61.aap.etl.parsing.EthqlParser.TransactionFilterContext;

/**
 * FilterVerifier
 */
class ScopeAnalyzer extends SemanticAnalyzer {
    private static final Logger LOGGER = Logger.getLogger(ScopeAnalyzer.class.getName());
    private static final String BLOCK_SCOPE = "block";
    private static final String TRANSACTION_SCOPE = "transaction";
    private static final String SMART_CONTRACT_SCOPE = "smartContract";
    private static final String LOG_ENTRY_SCOPE = "logEntry";

    private final Stack<String> enclosingScopes;
    private final VariableAnalyzer variableAnalyzer;

    public ScopeAnalyzer(ErrorCollector errorCollector, VariableAnalyzer variableAnalyzer) {
        super(errorCollector);
        assert variableAnalyzer != null;
        this.variableAnalyzer = variableAnalyzer;
        this.enclosingScopes = new Stack<>();
    }

    @Override
    public void clear() {
        this.enclosingScopes.clear();
    }

    @Override
    public void exitBlockFilter(BlockFilterContext ctx) {
        this.verifyFilterContexts(ctx, Objects::isNull, "A block scope cannot be embedded in any scope.");

        if (!this.isBlockNumberSpecified(ctx.from) || !this.isBlockNumberSpecified(ctx.to)) {
            assert false;
            this.addError(ctx.start, "The 'from' and 'to' block numbers must be specified");
        }

        if (ctx.from.KEY_PENDING() != null) {
            this.addError(ctx.from.start, "The 'from' parameter cannot be set to 'PENDING'");
        }

        if (ctx.to.KEY_EARLIEST() != null) {
            this.addError(ctx.to.start, "The 'to' parameter cannot be set to 'EARLIEST'");
        }

        if (ctx.from.INT_LITERAL() != null && ctx.to.INT_LITERAL() != null) {
            final BigInteger from = new BigInteger(ctx.from.INT_LITERAL().getText());
            final BigInteger to =  new BigInteger(ctx.to.INT_LITERAL().getText());
            if (from.compareTo(BigInteger.ZERO) < 0) {
                this.addError(ctx.from.start, String.format("The 'from' parameter must be positive, but wasn't: %s.", from));
            }

            if (to.compareTo(BigInteger.ZERO) < 0) {
                this.addError(ctx.to.start, String.format("The 'to' parameter must be positive, but wasn't: %s.", to));
            }
            
            if (from != null && to != null && from.compareTo(to) > 0) {
                this.addError(ctx.from.start, String.format("The 'to' parameter must not be greater than 'to' paremeter, but was: %s > %s.", from, to));
            }
        }

        this.verifyBlockVariableReference(ctx.from);
        this.verifyBlockVariableReference(ctx.to);

        this.addScopeToStack(BLOCK_SCOPE);
    }

    private boolean isBlockNumberSpecified(BlockNumberContext ctx) {
        return    ctx.INT_LITERAL() != null 
               || ctx.KEY_CURRENT() != null 
               || ctx.KEY_EARLIEST() != null 
               || ctx.KEY_PENDING() != null
               || ctx.variableReference() != null;
    }

    private void verifyBlockVariableReference(BlockNumberContext ctx) {
        if (ctx.variableReference() == null) {
            return;
        }

        final String varType = this.variableAnalyzer.getVariableType(ctx.variableReference().variableName().getText());
        if (varType != null && TypeUtils.hasBaseType(varType, TypeUtils.INT_TYPE_KEYWORD)) {
            this.addError(
                ctx.variableReference().start,
                String.format(
                    "The variable '%s' with type '' is not applicable for the blocknumber parameter which must be an integer or an integer array", 
                    ctx.variableReference().variableName().getText(),
                    varType.toString()
                )
            );
        }
    }

    @Override
    public void exitTransactionFilter(TransactionFilterContext ctx) {
        this.verifyFilterContexts(
            ctx, 
            ScopeAnalyzer::isBlockFilter,
            "A smart contract scope must be embedded in a block scope."
        );

        this.verifyAddressList(ctx.recipients);
        this.verifyAddressList(ctx.senders);

        this.addScopeToStack(TRANSACTION_SCOPE);
    }

    @Override
    public void exitSmartContractsFilter(SmartContractsFilterContext ctx) {
        this.verifyFilterContexts(
            ctx, 
            ScopeAnalyzer::isBlockFilter,
            "A smart contract scope must be embedded in a block scope."
        );

        this.verifyAddressList(ctx.addressList());

        if (!this.containsVariables(ctx)) {
            this.addError(ctx.start, "There must be at least one variable in a smart contract signature.");
        }

        if (!this.areVariableNamesDifferent(ctx)) {
            this.addError(ctx.start, "The variables in a smart contract signature must have distinct names.");
        }

        this.addScopeToStack(SMART_CONTRACT_SCOPE);
    }
    
    private boolean areVariableNamesDifferent(SmartContractsFilterContext ctx) {
        final long varCount = ctx.smartContractSignature()
            .smartContractVariable()
            .stream()
            .filter(this::isConcreteVariable)
            .count();

        final long nameCount = ctx.smartContractSignature()
            .smartContractVariable()
            .stream()
            .filter(this::isConcreteVariable)
            .map(varCtx -> varCtx.variableName().getText())
            .distinct()
            .count();

        return nameCount == varCount;
    }

    private boolean containsVariables(SmartContractsFilterContext ctx) {
        return ctx.smartContractSignature()
            .smartContractVariable()
            .stream()
            .anyMatch(this::isConcreteVariable);
    }

    private boolean isConcreteVariable(SmartContractVariableContext ctx) {
        return ctx.solType() != null && ctx.variableName() != null;
    }

    @Override
    public void exitLogEntryFilter(LogEntryFilterContext ctx) {
        this.verifyFilterContexts(
            ctx, 
            enclosingFilter -> isBlockFilter(enclosingFilter) || isTransactionFilter(enclosingFilter), 
            "A log entry scope must be embedded in a block or transaction scope."
        );
        
        this.verifyAddressList(ctx.addressList());

        if (!this.containsVariables(ctx)) {
            this.addError(ctx.start, "A log entry signature must at least contain one variable.");
        }

        if (!this.areVariableNamesDifferent(ctx)) {
            this.addError(ctx.start, "The variables in a log entry signature must have distinct names.");
        }

        this.addScopeToStack(LOG_ENTRY_SCOPE);
    }
    
    private boolean areVariableNamesDifferent(LogEntryFilterContext ctx) {
        final long varCount = this.logEntryParameterStream(ctx)
            .count();

        final long nameCount = this.logEntryParameterStream(ctx)
            .map(varCtx -> varCtx.variableName().getText())
            .distinct()
            .count();

        return nameCount == varCount;
    }

    private final Stream<LogEntryParameterContext> logEntryParameterStream(LogEntryFilterContext ctx) {
        if (ctx.logEntrySignature() != null) {
            return ctx.logEntrySignature().logEntryParameter().stream();
        }
        
        return ctx.skippableLogEntrySignature()
            .skippableLogEntryParameter()
            .stream()
            .filter(skipParam -> skipParam.logEntryParameter() != null)
            .map(skipParam -> skipParam.logEntryParameter());
    }

    private boolean containsVariables(LogEntryFilterContext ctx) {
        if (ctx.logEntrySignature() != null) {
            return true;
        }

        return ctx.skippableLogEntrySignature()
            .skippableLogEntryParameter()
            .stream()
            .anyMatch(par -> par.logEntryParameter() != null);
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.removeScopeFromStack();
    }

    private void verifyAddressList(AddressListContext ctx) {
        if (ctx.KEY_ANY() == null) {
            if (ctx.BYTE_AND_ADDRESS_LITERAL().isEmpty()) {
                final String message = "The use of this address list option is not supported!";
                this.addError(ctx.start, message);
                LOGGER.severe(message);
            }
            return;
        }

        if (ctx.variableReference() != null) {
            final String varType = this.variableAnalyzer.getVariableType(ctx.variableReference().variableName().getText());
            if (varType != null && !TypeUtils.areCompatible(varType, TypeUtils.ADDRESS_TYPE_KEYWORD)) {
                this.addError(
                    ctx.variableReference().start,
                    String.format(
                        "The variable '%s' with type '' is not applicable for the address list parameter which must be an address or an address array", 
                        ctx.variableReference().variableName().getText(),
                        varType.toString()
                    )
                );
            }
        }
    }

    private void verifyFilterContexts(ParserRuleContext ctx, 
        Predicate<String> enclosingFilterPredicate, 
        String errorMessage) {
    
        final String scope = this.enclosingScopes.isEmpty() ? null : this.enclosingScopes.peek();

        if (!enclosingFilterPredicate.test(scope)) {
            this.addError(ctx.start, errorMessage);
        }

    }

    private void addScopeToStack(String scope) {
        this.enclosingScopes.push(scope);
    }

    private void removeScopeFromStack() {
        this.enclosingScopes.pop();
    }

    private static boolean isBlockFilter(String scope) {
        return scope == BLOCK_SCOPE;
    }

    private static boolean isTransactionFilter(String scope) {
        return scope == TRANSACTION_SCOPE;
    }
}
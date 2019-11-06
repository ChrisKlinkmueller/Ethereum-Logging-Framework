package au.csiro.data61.aap.parser;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.antlr.v4.runtime.ParserRuleContext;

import au.csiro.data61.aap.parser.XbelParser.AddressListContext;
import au.csiro.data61.aap.parser.XbelParser.BlockFilterContext;
import au.csiro.data61.aap.parser.XbelParser.BlockNumberContext;
import au.csiro.data61.aap.parser.XbelParser.LogEntryFilterContext;
import au.csiro.data61.aap.parser.XbelParser.LogEntryParameterContext;
import au.csiro.data61.aap.parser.XbelParser.ScopeContext;
import au.csiro.data61.aap.parser.XbelParser.SmartContractVariableContext;
import au.csiro.data61.aap.parser.XbelParser.SmartContractsFilterContext;
import au.csiro.data61.aap.parser.XbelParser.TransactionFilterContext;

/**
 * FilterVerifier
 */
class FilterAnalyzer extends SemanticAnalyzer {
    private static final Logger LOGGER = Logger.getLogger(FilterAnalyzer.class.getName());
    private static final String BLOCK_SCOPE = "block";
    private static final String TRANSACTION_SCOPE = "transaction";
    private static final String SMART_CONTRACT_SCOPE = "smartContract";
    private static final String LOG_ENTRY_SCOPE = "logEntry";

    private final Stack<String> enclosingScopes;

    public FilterAnalyzer(ErrorCollector errorCollector) {
        super(errorCollector);
        this.enclosingScopes = new Stack<>();
    }

    @Override
    public void clear() {
        this.enclosingScopes.clear();
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.verifyFilterContexts(ctx, Objects::isNull, "A block scope cannot be embedded in any scope.");

        if (ctx.from.KEY_PENDING() != null) {
            this.errorCollector.addSemanticError(ctx.from.start, "The 'from' parameter cannot be set to 'PENDING'");
        }

        if (ctx.to.KEY_EARLIEST() != null) {
            this.errorCollector.addSemanticError(ctx.to.start, "The 'to' parameter cannot be set to 'EARLIEST'");
        }

        if (ctx.from.INT_LITERAL() != null && ctx.to.INT_LITERAL() != null) {
            final BigInteger from = AnalyzerUtils.verifyIntegerLiteral(ctx.from.INT_LITERAL(), this.errorCollector);
            final BigInteger to = AnalyzerUtils.verifyIntegerLiteral(ctx.to.INT_LITERAL(), this.errorCollector);
            if (from.compareTo(BigInteger.ZERO) < 0) {
                this.errorCollector.addSemanticError(ctx.from.start, String.format("The 'from' parameter must be positive, but wasn't: %s.", from));
            }

            if (to.compareTo(BigInteger.ZERO) < 0) {
                this.errorCollector.addSemanticError(ctx.to.start, String.format("The 'to' parameter must be positive, but wasn't: %s.", to));
            }
            
            if (from != null && to != null && from.compareTo(to) > 0) {
                this.errorCollector.addSemanticError(ctx.from.start, String.format("The 'to' parameter must not be greater than 'to' paremeter, but was: %s > %s.", from, to));
            }
        }

        if (!this.isBlockNumberSpecified(ctx.from) || !this.isBlockNumberSpecified(ctx.to)) {
            assert false;
            this.errorCollector.addSemanticError(ctx.start, "The 'from' and 'to' block numbers must be specified");
        }

        this.addScopeToStack(BLOCK_SCOPE);
    }

    private boolean isBlockNumberSpecified(BlockNumberContext ctx) {
        return    ctx.INT_LITERAL() != null 
               || ctx.KEY_CURRENT() != null 
               || ctx.KEY_EARLIEST() != null 
               || ctx.KEY_PENDING() != null;
    }

    @Override
    public void enterTransactionFilter(TransactionFilterContext ctx) {
        this.verifyFilterContexts(
            ctx, 
            FilterAnalyzer::isBlockFilter,
            "A smart contract scope must be embedded in a block scope."
        );

        this.verifyAddressList(ctx.recipients);
        this.verifyAddressList(ctx.senders);

        this.addScopeToStack(TRANSACTION_SCOPE);
    }

    @Override
    public void enterSmartContractsFilter(SmartContractsFilterContext ctx) {
        this.verifyFilterContexts(
            ctx, 
            FilterAnalyzer::isBlockFilter,
            "A smart contract scope must be embedded in a block scope."
        );

        this.verifyAddressList(ctx.addressList());

        if (!this.containsVariables(ctx)) {
            this.errorCollector.addSemanticError(ctx.start, "There must be at least one variable in a smart contract signature.");
        }

        if (!this.areVariableNamesDifferent(ctx)) {
            this.errorCollector.addSemanticError(ctx.start, "The variables in a smart contract signature must have distinct names.");
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
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.verifyFilterContexts(
            ctx, 
            enclosingFilter -> isBlockFilter(enclosingFilter) || isTransactionFilter(enclosingFilter), 
            "A log entry scope must be embedded in a block or transaction scope."
        );
        
        this.verifyAddressList(ctx.addressList());

        if (!this.containsVariables(ctx)) {
            this.errorCollector.addSemanticError(ctx.start, "A log entry signature must at least contain one variable.");
        }

        if (!this.areVariableNamesDifferent(ctx)) {
            this.errorCollector.addSemanticError(ctx.start, "The variables in a log entry signature must have distinct names.");
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
        // TODO: add and verify contracts variable if it exists
        if (ctx.KEY_ANY() == null) {
            if (!ctx.BYTE_AND_ADDRESS_LITERAL().isEmpty()) {
                AnalyzerUtils.verifyAddressLiterals(ctx.BYTE_AND_ADDRESS_LITERAL(), this.errorCollector);
            }
            else {
                final String message = "The use of this address list option is not supported!";
                this.errorCollector.addSemanticError(ctx.start, message);
                LOGGER.severe(message);
            }
        }
    }

    private void verifyFilterContexts(ParserRuleContext ctx, 
        Predicate<String> enclosingFilterPredicate, 
        String errorMessage) {
    
        final String scope = this.enclosingScopes.isEmpty() ? null : this.enclosingScopes.peek();

        if (!enclosingFilterPredicate.test(scope)) {
            this.errorCollector.addSemanticError(ctx.start, errorMessage);
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
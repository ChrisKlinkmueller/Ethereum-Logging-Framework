package au.csiro.data61.aap.parser;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ParserRuleContext;

import au.csiro.data61.aap.parser.XbelParser.AddressListContext;
import au.csiro.data61.aap.parser.XbelParser.BlockFilterContext;
import au.csiro.data61.aap.parser.XbelParser.LogEntryFilterContext;
import au.csiro.data61.aap.parser.XbelParser.ScopeContext;
import au.csiro.data61.aap.parser.XbelParser.SmartContractsFilterContext;
import au.csiro.data61.aap.parser.XbelParser.TransactionFilterContext;

/**
 * FilterVerifier
 */
class FilterVerifier extends SemanticAnalyzer {
    private static final Logger LOGGER = Logger.getLogger(FilterVerifier.class.getName());
    private static final String BLOCK_SCOPE = "block";
    private static final String TRANSACTION_SCOPE = "transaction";
    private static final String SMART_CONTRACT_SCOPE = "smartContract";
    private static final String LOG_ENTRY_SCOPE = "logEntry";

    private final Stack<String> enclosingScopes;

    public FilterVerifier(ErrorCollector errorCollector) {
        super(errorCollector);
        this.enclosingScopes = new Stack<>();
    }

    @Override
    public void clear() {
        this.enclosingScopes.clear();
    }

    @Override
    public void enterBlockFilter(BlockFilterContext ctx) {
        this.verifyFilterContexts(
            ctx, 
            Objects::isNull, 
            "A block scope cannot be embedded in any scope."
        );

        if (ctx.from.KEY_PENDING() != null) {
            this.errorCollector.addSemanticError(new SpecificationParserError(ctx.from.start, "The 'from' parameter cannot be set to 'PENDING'"));
        }

        if (ctx.to.KEY_EARLIEST() != null) {
            this.errorCollector.addSemanticError(new SpecificationParserError(ctx.to.start, "The 'to' parameter cannot be set to 'EARLIEST'"));
        }

        if (ctx.from.INT_LITERAL() != null && ctx.to.INT_LITERAL() != null) {
            final BigInteger from = LiteralVerifier.verifyIntegerLiteral(ctx.from.INT_LITERAL(), this.errorCollector);
            final BigInteger to = LiteralVerifier.verifyIntegerLiteral(ctx.to.INT_LITERAL(), this.errorCollector);
            if (from != null && to != null && from.compareTo(to) > 0) {
                this.errorCollector.addSemanticError(new SpecificationParserError(
                    ctx.from.start, 
                    String.format("The 'to' parameter must not be freater than 'to' paremeter, but was: %s > %s.", from, to))
                );
            }
        }

        this.addScopeToStack(BLOCK_SCOPE);
    } 

    @Override
    public void enterTransactionFilter(TransactionFilterContext ctx) {
        this.verifyFilterContexts(
            ctx, 
            FilterVerifier::isBlockFilter,
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
            FilterVerifier::isBlockFilter,
            "A smart contract scope must be embedded in a block scope."
        );

        this.verifyAddressList(ctx.addressList());

        this.addScopeToStack(SMART_CONTRACT_SCOPE);
    }
    
    @Override
    public void enterLogEntryFilter(LogEntryFilterContext ctx) {
        this.verifyFilterContexts(
            ctx, 
            enclosingFilter -> isBlockFilter(enclosingFilter) || isTransactionFilter(enclosingFilter), 
            "A log entry scope must be embedded in a block or transaction scope."
        );
        
        this.verifyAddressList(ctx.addressList());

        this.addScopeToStack(LOG_ENTRY_SCOPE);
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.removeScopeFromStack();
    }

    private void verifyAddressList(AddressListContext ctx) {
        // TODO: add and verify contracts variable if it exists
        if (ctx.KEY_ANY() == null) {
            if (!ctx.BYTE_AND_ADDRESS_LITERAL().isEmpty()) {
                LiteralVerifier.verifyAddressLiterals(ctx.BYTE_AND_ADDRESS_LITERAL(), this.errorCollector);
            }
            else {
                final String message = "The use of this address list option is not supported!";
                this.errorCollector.addSemanticError(new SpecificationParserError(ctx.start, message));
                LOGGER.severe(message);
            }
        }
    }

    private void verifyFilterContexts(ParserRuleContext ctx, 
        Predicate<String> enclosingFilterPredicate, 
        String errorMessage) {
    
        final String scope = this.enclosingScopes.isEmpty() ? null : this.enclosingScopes.peek();

        System.out.println("Scope: " + scope);

        if (!enclosingFilterPredicate.test(scope)) {
            this.errorCollector.addSemanticError(new SpecificationParserError(ctx.start, errorMessage));
        }

    }

    private void addScopeToStack(String scope) {
        System.out.println("Push: " + scope);
        this.enclosingScopes.push(scope);
    }

    private void removeScopeFromStack() {
        final String scope = this.enclosingScopes.pop();
        System.out.println("Pop: " + scope);
    }

    private static boolean isBlockFilter(String scope) {
        return scope == BLOCK_SCOPE;
    }

    private static boolean isTransactionFilter(String scope) {
        return scope == TRANSACTION_SCOPE;
    }
}
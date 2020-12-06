package au.csiro.data61.aap.elf.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.antlr.v4.runtime.Token;

import au.csiro.data61.aap.elf.parsing.BcqlParser.DocumentContext;
import au.csiro.data61.aap.elf.parsing.BcqlParser.FilterContext;
import au.csiro.data61.aap.elf.parsing.BcqlParser.ScopeContext;

/**
 * FilterCombinationAnalyzer
 */
public class FilterNestingAnalyzer extends SemanticAnalyzer {
    private final Stack<String> filterStack;

    public FilterNestingAnalyzer(ErrorCollector errorCollector) {
        super(errorCollector);
        this.filterStack = new Stack<String>();

    }

    @Override
    public void clear() {
        this.filterStack.clear();
    }

    @Override
    public void enterDocument(DocumentContext ctx) {
        this.filterStack.push(PROGRAM);
    }

    @Override
    public void exitDocument(DocumentContext ctx) {
        this.filterStack.pop();
    }

    @Override
    public void enterScope(ScopeContext ctx) {
        final String filterType = this.getFilterName(ctx.filter());
        this.checkFilter(ctx.filter().start, filterType);
        this.filterStack.add(filterType);
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        this.filterStack.pop();
    }

    private void checkFilter(Token token, String filterType) {
        if (this.verifyEnclosingType(filterType)) {
            return;
        }

        this.addError(token, String.format("Invalid nesting of filters."));
    }

    private boolean verifyEnclosingType(String filterType) {
        return VALID_ENCLOSING_FILTERS.get(filterType).test(this.filterStack);
    }

    private String getFilterName(FilterContext ctx) {
        if (ctx.blockFilter() != null) {
            return BLOCK_FILTER;
        } else if (ctx.transactionFilter() != null) {
            return TRANSACTION_FILTER;
        } else if (ctx.logEntryFilter() != null) {
            return LOG_ENRY_FILTER;
        } else if (ctx.genericFilter() != null) {
            return GENERIC_FILTER;
        } else if (ctx.smartContractFilter() != null) {
            return SMART_CONTRACT_FILTER;
        } else {
            throw new UnsupportedOperationException("This filter type is not supported");
        }
    }

    private static final String PROGRAM = "program";
    private static final String BLOCK_FILTER = "block";
    private static final String GENERIC_FILTER = "generic";
    private static final String TRANSACTION_FILTER = "transaction";
    private static final String LOG_ENRY_FILTER = "log entry";
    private static final String SMART_CONTRACT_FILTER = "smart contract";
    private static final Map<String, Predicate<Stack<String>>> VALID_ENCLOSING_FILTERS;

    static {
        VALID_ENCLOSING_FILTERS = new HashMap<>();
        VALID_ENCLOSING_FILTERS.put(BLOCK_FILTER, FilterNestingAnalyzer::areBlockFilterParentsValid);
        VALID_ENCLOSING_FILTERS.put(TRANSACTION_FILTER, FilterNestingAnalyzer::areTransactionFilterParentsValid);
        VALID_ENCLOSING_FILTERS.put(LOG_ENRY_FILTER, FilterNestingAnalyzer::areLogEntryFilterParentsValid);
        VALID_ENCLOSING_FILTERS.put(GENERIC_FILTER, FilterNestingAnalyzer::areGenericFilterParentsValid);
        VALID_ENCLOSING_FILTERS.put(SMART_CONTRACT_FILTER, FilterNestingAnalyzer::areSmartContractFilterParentsValid);
    }

    private static boolean areBlockFilterParentsValid(Stack<String> stack) {
        return !stack.isEmpty()
            && stack.get(0).equals(PROGRAM)
            && IntStream.range(1, stack.size()).allMatch(i -> stack.get(i).equals(GENERIC_FILTER));
    }

    private static boolean areTransactionFilterParentsValid(Stack<String> stack) {
        return !stack.isEmpty() && stack.get(0).equals(PROGRAM) && countFilters(stack, BLOCK_FILTER) == 1;
    }

    private static boolean areLogEntryFilterParentsValid(Stack<String> stack) {
        return !stack.isEmpty()
            && stack.get(0).equals(PROGRAM)
            && countFilters(stack, BLOCK_FILTER) == 1
            && countFilters(stack, TRANSACTION_FILTER) <= 1;
    }

    private static long countFilters(Stack<String> stack, String filter) {
        return stack.stream().filter(f -> f.equals(filter)).count();
    }

    private static boolean areGenericFilterParentsValid(Stack<String> stack) {
        return !stack.isEmpty() && stack.get(0).equals(PROGRAM);
    }

    private static boolean areSmartContractFilterParentsValid(Stack<String> stack) {
        return !stack.isEmpty() && stack.peek().equals(BLOCK_FILTER);
    }

}

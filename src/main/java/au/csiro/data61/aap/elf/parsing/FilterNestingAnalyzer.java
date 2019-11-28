package au.csiro.data61.aap.elf.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.antlr.v4.runtime.Token;

import au.csiro.data61.aap.elf.parsing.EthqlParser.FilterContext;
import au.csiro.data61.aap.elf.parsing.EthqlParser.ScopeContext;

/**
 * FilterCombinationAnalyzer
 */
public class FilterNestingAnalyzer extends SemanticAnalyzer {
    private static final String BLOCK_FILTER = "block";
    private static final String GENERIC_FILTER = "generic";
    private static final String TRANSACTION_FILTER = "transaction";
    private static final String LOG_ENRY_FILTER = "log entry";
    private static final Map<String, List<List<String>>> VALID_FILTER_NESTINGS;

    static {
        VALID_FILTER_NESTINGS = new HashMap<>();
        addValidFilterNestings(BLOCK_FILTER, new String[0]);
        addValidFilterNestings(TRANSACTION_FILTER, BLOCK_FILTER);
        addValidFilterNestings(LOG_ENRY_FILTER, BLOCK_FILTER);
        addValidFilterNestings(LOG_ENRY_FILTER, BLOCK_FILTER, TRANSACTION_FILTER);
    }

    private static void addValidFilterNestings(String filter, String... nesting) {
        VALID_FILTER_NESTINGS.putIfAbsent(filter, new ArrayList<>());
        VALID_FILTER_NESTINGS.get(filter).add(Arrays.asList(nesting));
    }

    private boolean conductCheck;
    private final List<String> filterStack;
    public FilterNestingAnalyzer(ErrorCollector errorCollector) {
        super(errorCollector);
        this.filterStack = new ArrayList<String>();
        this.conductCheck = true;

    }
    @Override
    public void clear() {
        this.filterStack.clear();
        this.conductCheck = true;
    }

    @Override
    public void enterScope(ScopeContext ctx) {
        if (!this.conductCheck) {
            return;
        }

        final String filter = this.getFilterName(ctx.filter());
        if (filter.equals(GENERIC_FILTER)) {
            return;
        }

        this.checkFilter(ctx.filter().start, filter);
    }

    private void checkFilter(Token token, String filter) {
        for (List<String> nesting : VALID_FILTER_NESTINGS.get(filter)) {
            if (nestingEqualsFilterStack(nesting)) {
                return;
            }
        }

        this.conductCheck = false;
        this.addError(token, String.format("Invalid nesting of filters."));
    }    

    private boolean nestingEqualsFilterStack(List<String> nesting) {
        if (nesting.size() !=  this.filterStack.size()) {
            return false;
        }
        return IntStream.range(0, nesting.size())
            .allMatch(i -> nesting.get(i).equals(this.filterStack.get(i)));
    }

    private String getFilterName(FilterContext ctx) {
        if (ctx.blockFilter() != null) {
            return BLOCK_FILTER;
        }
        else if (ctx.transactionFilter() != null) {
            return TRANSACTION_FILTER;
        }
        else if (ctx.logEntryFilter() != null) {
            return LOG_ENRY_FILTER;
        }
        else if (ctx.genericFilter() != null) {
            return GENERIC_FILTER;
        }
        else {
            throw new UnsupportedOperationException("This filter type is not supported");
        }
    }  
}
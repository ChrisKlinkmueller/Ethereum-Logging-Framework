package au.csiro.data61.aap.specification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * ScopeDefinition
 */
public class ScopeDefinition {
    public static String BLOCK_FROM = "from";
    public static String BLOCK_TO = "to";    
    public static String TRANSACTION_SENDERS = "senders";
    public static String TRANSACTION_RECIPIENTS = "recipients";
    public static String SMART_CONTRACTS_ADDRESSES = "addresses";
    public static String LOG_ENTRY_SCHEMA = "schema";
    public static String EMIT_CONDITION = "condition";
    
    private final ScopeType type;
    private final HashMap<String, ValueSource> valueSources;
    
    @SuppressWarnings("unchecked")
    public ScopeDefinition(ScopeType type) {
        this(type, new Entry[0]);
    }

    public ScopeDefinition(ScopeType type, Entry<String, ValueSource>[] valueSources) {
        assert type != null;
        assert valueSources != null && Arrays.stream(valueSources).allMatch(source -> source != null);
        this.type = type;
        this.valueSources =  new HashMap<>();        
        Arrays.stream(valueSources).forEach(source -> this.valueSources.put(source.getKey(), source.getValue()));
    }

    public ScopeType getType() {
        return this.type;
    }
    
    public boolean containsValueSource(String name) {
        return this.valueSources.containsKey(name);
    }

    public ValueSource getValueSource(String name) {
        return this.valueSources.get(name);
    }
}
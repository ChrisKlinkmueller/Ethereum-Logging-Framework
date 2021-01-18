package blf.core.values;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ValueStore
 */
public class ValueStore {
    private final Map<String, Object> values;

    public ValueStore() {
        this.values = new HashMap<>();
    }

    public void setValue(String name, Object value) {
        this.values.put(name, value);
    }

    public Object getValue(String name) {
        return this.values.get(name);
    }

    public void removeValue(String name) {
        this.values.remove(name);
    }

    @SuppressWarnings("unused")
    public void removeValues(List<String> names) {
        names.forEach(this::removeValue);
    }

    public boolean containsName(String name) {
        return this.values.containsKey(name);
    }
}

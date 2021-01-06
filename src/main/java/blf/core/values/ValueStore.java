package blf.core.values;

import io.reactivex.annotations.NonNull;

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

    public void setValue(@NonNull String name, Object value) {
        this.values.put(name, value);
    }

    public Object getValue(@NonNull String name) {
        return this.values.get(name);
    }

    public void removeValue(@NonNull String name) {
        this.values.remove(name);
    }

    public void removeValues(@NonNull List<String> names) {
        names.stream().forEach(this::removeValue);
    }

    public boolean containsName(String name) {
        return this.values.containsKey(name);
    }
}

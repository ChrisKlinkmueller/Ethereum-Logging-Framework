package au.csiro.data61.aap.etl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ValueStore
 */
public class ValueStore {
    private Map<String, Object> values;

    public ValueStore() {
        this.values = new HashMap<>();
    }
    
    public void setValue(String name, Object value) {
        assert name != null;
        this.values.put(name, value);
    }

    public Object getValue(String name) {
        assert name != null;
        return this.values.get(name);
    }

    public void removeValue(String name) {
        assert name != null;
        this.values.remove(name);
    }

    public void removeValues(List<String> names) {
        assert names != null;
        names.stream().forEach(name -> this.removeValue(name));
    }
}
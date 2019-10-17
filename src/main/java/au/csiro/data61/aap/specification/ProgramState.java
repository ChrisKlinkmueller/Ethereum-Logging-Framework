package au.csiro.data61.aap.specification;

import java.util.HashMap;
import java.util.Map;

/**
 * ProgramState
 */
public class ProgramState {
    private final Map<String, ValueContainer> valueContainers;
    
    public ProgramState() {
        this.valueContainers = new HashMap<>();
    }

    public void addValueContainer(ValueContainer container) {
        assert container != null && !this.valueContainers.containsKey(container.getName());
        this.valueContainers.put(container.getName(), container);        
    }

    public void removeValueContainer(ValueContainer container) {
        assert container != null && this.valueContainers.containsKey(container.getName());
        this.valueContainers.remove(container.getName());
    }

    public ValueContainer getValueContainer(String name) {
        return this.valueContainers.get(name);
    }
    
    public boolean exitsValueContainer(String name) {
        return this.valueContainers.containsKey(name);
    }

	public boolean exitsVariable(String name) {
		return this.valueContainers.get(name) != null && this.getValueContainer(name) instanceof Variable;
	}

	public boolean exitsConstant(String name) {
		return this.valueContainers.get(name) != null && this.getValueContainer(name) instanceof Constant;
	}

    public void clearValueContainers() {
        this.valueContainers.clear();
    }
}
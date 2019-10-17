package au.csiro.data61.aap.specification;

import java.util.HashMap;
import java.util.Map;

import au.csiro.data61.aap.specification.Variable;

/**
 * ProgramState
 */
public class ProgramState {
    private final Map<String, Variable> variables;
    
    public ProgramState() {
        this.variables = new HashMap<>();
    }

    public void addVariable(Variable variable) {
        assert variable != null && !this.variables.containsKey(variable.getName());
        this.variables.put(variable.getName(), variable);        
    }

    public void removeVariable(Variable variable) {
        assert variable != null && this.variables.containsKey(variable.getName());
        this.variables.remove(variable.getName());
    }

    public Variable getVariable(String name) {
        return this.variables.get(name);
    }
    
    public boolean exitsVariable(String name) {
        return this.variables.containsKey(name);
    }

    public void clearVariables() {
        this.variables.clear();
    }
}
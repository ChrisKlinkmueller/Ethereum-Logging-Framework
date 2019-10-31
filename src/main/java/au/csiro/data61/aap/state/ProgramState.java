package au.csiro.data61.aap.state;

import java.util.HashMap;
import java.util.Map;

import au.csiro.data61.aap.rpc.EthereumBlock;
import au.csiro.data61.aap.spec.Variable;

/**
 * ProgramState
 */
public class ProgramState {
    private final Map<String, Variable> variables;
    private EthereumBlock block;

    public ProgramState() {
        this.variables = new HashMap<>();
    }

    public Variable getVariable(String varName) {
        assert varName != null && varName.isBlank() && this.variables.containsKey(varName);
        return this.variables.get(varName);
    }

    public void addVariable(Variable variable) {
        assert variable != null && !this.variables.containsKey(variable.getName());
        this.variables.put(variable.getName(), variable);
    }

    public void removeVariable(Variable variable) {
        assert variable != null && this.variables.containsKey(variable.getName());
        this.variables.remove(variable.getName());
    }

    public void setCurrentBlock(EthereumBlock block) {
        assert block != null;
        this.block = block;
    }

    public EthereumBlock getCurrentBlock() {
        return this.block;
    }
}
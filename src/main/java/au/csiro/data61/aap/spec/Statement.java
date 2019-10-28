package au.csiro.data61.aap.spec;

import java.util.Optional;

/**
 * Statement
 */
public class Statement extends Instruction {
    private Optional<Variable> variable;
    private ValueSource source;

    public Statement(CodeBlock enclosingBlock, ValueSource source) {
        this(enclosingBlock, null, source);
    }

    public Statement(CodeBlock enclosingBlock, Variable variable, ValueSource source) {
        super(enclosingBlock);
        assert source != null;
        this.variable = variable == null ? Optional.empty() : Optional.of(variable);
        this.source = source;
    } 

    public Optional<Variable> getVariable() {
        return this.variable;
    }

    public ValueSource getSource() {
        return this.source;
    }
}
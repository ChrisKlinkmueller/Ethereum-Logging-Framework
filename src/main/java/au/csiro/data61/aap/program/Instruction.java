package au.csiro.data61.aap.program;

import java.util.stream.Stream;

/**
 * Instruction
 */
public abstract class Instruction implements Executable {
    private Scope enclosingScope;

    protected void setEnclosingScope(Scope enclosingScope) {
        this.enclosingScope = enclosingScope;
    }

    public Scope getEnclosingScope() {
        return this.enclosingScope;
    }

    public abstract Stream<Variable> variableStream();
    
}
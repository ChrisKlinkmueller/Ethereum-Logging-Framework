package au.csiro.data61.aap.program;

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
    
}
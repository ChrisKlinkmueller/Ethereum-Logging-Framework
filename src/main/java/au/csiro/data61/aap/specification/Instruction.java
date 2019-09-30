package au.csiro.data61.aap.specification;

/**
 * Instruction
 */
public abstract class Instruction {
    private final Scope parentScope;
    
    public Instruction() {
        this(null);
    }

    public Instruction(Scope parentScope) {
        this.parentScope = parentScope;
    }

    public Scope getParentScope() {
        return this.parentScope;
    }
}
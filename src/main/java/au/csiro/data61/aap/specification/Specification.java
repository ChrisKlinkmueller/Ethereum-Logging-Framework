package au.csiro.data61.aap.specification;

/**
 * TransformSpecification
 */
public class Specification {
    private final Configuration config;
    private final Scope globalScope;
    
    public Specification(Configuration config, Scope globalScope) {
        assert config != null;
        assert globalScope != null && globalScope.getDefinition().getType() == ScopeType.GLOBAL_SCOPE;
        this.config = config;
        this.globalScope = globalScope;
    }

    public Configuration getConfig() {
        return this.config;
    }

    public Scope getGlobalScope() {
        return this.globalScope;
    }
}
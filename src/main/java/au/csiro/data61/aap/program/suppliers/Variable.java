package au.csiro.data61.aap.program.suppliers;

/**
 * ValueContainer
 */
public interface Variable extends ValueSupplier {
    public String getName();
    
    public default boolean hasSameName(Variable variable) {
        assert variable != null;
        return this.hasName(variable.getName());
    }

    public default boolean hasName(String name) {
        assert name != null;
        return this.getName().equals(name);
    }
}
package au.csiro.data61.aap.program.suppliers;

import au.csiro.data61.aap.program.types.SolidityType;

/**
 * Literal
 */
public class Literal implements ValueSupplier {
    private final Object value;
    private final SolidityType type;

    public Literal(SolidityType type, Object value) {
        assert type != null;
        assert value != null;
        this.value = value;
        this.type = type;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public SolidityType getType() {
        return this.type;
    }

    
}
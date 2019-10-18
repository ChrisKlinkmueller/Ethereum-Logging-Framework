package au.csiro.data61.aap.spec.types;

/**
 * ArrayType
 */
public class ArrayType extends SolidityType {
    private static final String SUFFIX = "[]";

    private final SolidityType baseType;
    
    public ArrayType(SolidityType baseType) {
        assert baseType != null;
        this.baseType = baseType;
    }

    public SolidityType getBaseType() {
        return this.baseType;
    }

    @Override
    public String getName() {
        return String.format("%s%s", this.baseType.getName(), SUFFIX);
    }

    @Override
    public boolean castableFrom(SolidityType type) {
        return this.baseType.castableFrom(type);
    }
}
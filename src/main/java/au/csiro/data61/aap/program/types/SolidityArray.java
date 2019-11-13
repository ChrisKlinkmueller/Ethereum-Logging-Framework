package au.csiro.data61.aap.program.types;

import java.util.Objects;

/**
 * ArrayType
 */
public class SolidityArray extends SolidityType {
    private static final String SUFFIX = "[]";

    private final SolidityType baseType;
    
    public SolidityArray(SolidityType baseType) {
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
    
    @Override
    public boolean conceptuallyEquals(SolidityType type) {
        return    type != null 
               && type instanceof SolidityArray
               && this.baseType.conceptuallyEquals(((SolidityArray)type).baseType);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SolidityArray)) {
            return false;
        }

        return this.baseType.equals(((SolidityArray)o).getBaseType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(SUFFIX, this.baseType);
    }
}
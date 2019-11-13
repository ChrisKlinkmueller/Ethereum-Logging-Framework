package au.csiro.data61.aap.program.types;

import java.util.Objects;

/**
 * BoolType
 */
public class SolidityBool extends SolidityType {
    private static final String NAME = "bool";
    
    public static final SolidityBool DEFAULT_INSTANCE = new SolidityBool();

    @Override
    public String getName() {
        return NAME;
    }    

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof SolidityBool;
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME);
    }
}
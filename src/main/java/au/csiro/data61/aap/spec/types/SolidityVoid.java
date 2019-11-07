package au.csiro.data61.aap.spec.types;

import java.util.Objects;

/**
 * VoidType
 */
public class SolidityVoid extends SolidityType {
    private static final String NAME = "void";
    public static final SolidityVoid DEFAULT_INSTANCE = new SolidityVoid();

    public SolidityVoid() {
        super();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof SolidityVoid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME);
    }
}
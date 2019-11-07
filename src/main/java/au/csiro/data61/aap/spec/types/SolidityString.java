package au.csiro.data61.aap.spec.types;

import java.util.Objects;

/**
 * StringType
 */
public class SolidityString extends SolidityType {
    private static final String NAME = "string";

    public static final SolidityString DEFAULT_INSTANCE = new SolidityString();

    public SolidityString() {
        super(SolidityAddress.class, SolidityBool.class, SolidityBytes.class, SolidityFixed.class, SolidityInteger.class, SolidityString.class);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof SolidityString;
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME);
    }
}
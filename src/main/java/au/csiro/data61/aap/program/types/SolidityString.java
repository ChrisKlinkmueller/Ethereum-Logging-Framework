package au.csiro.data61.aap.program.types;

import java.util.Objects;

/**
 * StringType
 */
public class SolidityString extends SolidityType {
    private static final String NAME = "string";

    public static final SolidityString DEFAULT_INSTANCE = new SolidityString();

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
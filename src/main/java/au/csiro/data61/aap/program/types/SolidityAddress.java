package au.csiro.data61.aap.program.types;

import java.util.Objects;

/**
 * ArrayType
 */
public class SolidityAddress extends SolidityType {
    private static final String NAME = "address";
    
    public static final SolidityAddress DEFAULT_INSTANCE = new SolidityAddress();

    public SolidityAddress() {
        super(SolidityAddress.class);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof SolidityAddress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME);
    }
}
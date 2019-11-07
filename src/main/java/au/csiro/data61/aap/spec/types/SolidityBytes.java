package au.csiro.data61.aap.spec.types;

import java.util.Objects;

/**
 * BytesType
 */
public class SolidityBytes extends SolidityType {
    private final static String NAME = "bytes";    
    private final static int MIN_STATIC_LENGTH = 1;
    private final static int MAX_STATIC_LENGTH = 32;
    
    public static final SolidityBytes DEFAULT_INSTANCE = new SolidityBytes(1);

    private final int length;
    public SolidityBytes(int length) {
        super(SolidityBytes.class, SolidityAddress.class);
        assert isValidLength(length);
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String getName() {
        return String.format("%s%s", NAME, this.length);
    }

    public static final boolean isValidLength(int length) {
        return MIN_STATIC_LENGTH <= length && length <= MAX_STATIC_LENGTH;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SolidityBytes)) {
            return false;
        }

        return this.length == ((SolidityBytes)o).length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME, this.length);
    }

}
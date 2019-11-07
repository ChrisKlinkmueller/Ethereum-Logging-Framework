package au.csiro.data61.aap.spec.types;

import java.util.Objects;

/**
 * IntegerType
 */
public class SolidityInteger extends SolidityType {
    private static final String NAME = "int";
    private static final int DEFAULT_LENGTH = 256;

    public static final SolidityInteger DEFAULT_INSTANCE = new SolidityInteger(true);
    
    private final boolean signed;
    private final int bitLength;

    public SolidityInteger(boolean signed) {
        this(signed, DEFAULT_LENGTH);
    }

    public SolidityInteger(boolean signed, int bitLength) {
        super(SolidityInteger.class);
        this.bitLength = bitLength;
        this.signed = signed;
    }

    public boolean isUnsigned() {
        return !this.signed;
    }

    public int getLength() {
        return this.bitLength;
    }

    @Override
    public String getName() {
        final String unsignedPrefix = this.signed ? "" : "u";
        return String.format("%s%s%s", unsignedPrefix, NAME, this.bitLength);
    }

    public static boolean isValidLength(int length) {
        return 8 <= length && length <= 256 && length % 8 == 0;
    }    

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof SolidityInteger)) {
            return false;
        }

        final SolidityInteger type = (SolidityInteger)o;
        return this.signed == type.signed && this.bitLength == type.bitLength;
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME, this.signed, this.bitLength);
    }
}
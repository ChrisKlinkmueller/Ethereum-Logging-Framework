package au.csiro.data61.aap.spec.types;

/**
 * IntegerType
 */
public class IntegerType extends SolidityType {
    private static final String NAME = "int";
    private static final int DEFAULT_LENGTH = 256;

    public static final IntegerType DEFAULT_INSTANCE = new IntegerType(true);
    
    private final boolean signed;
    private final int bitLength;

    public IntegerType(boolean signed) {
        this(signed, DEFAULT_LENGTH);
    }

    public IntegerType(boolean signed, int bitLength) {
        super(IntegerType.class);
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
}
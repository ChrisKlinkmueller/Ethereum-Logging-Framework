package au.csiro.data61.aap.spec.types;

/**
 * BytesType
 */
public class BytesType extends SolidityType {
    private final static String NAME = "bytes";    
    private final static int DYNAMIC_LENGTH = -1;
    private final static int MIN_STATIC_LENGTH = 1;
    private final static int MAX_STATIC_LENGTH = 32;
    
    public static final BytesType DEFAULT_INSTANCE = new BytesType(DYNAMIC_LENGTH);

    private final int length;
    public BytesType(int length) {
        super(BytesType.class, AddressType.class);
        assert isValidLength(length);
        this.length = length;
    }

    public boolean isDynamic() {
        return this.length == DYNAMIC_LENGTH;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String getName() {
        final String lengthSuffix = this.length == DYNAMIC_LENGTH ? "" : Integer.toString(this.length);
        return String.format("%s%s", NAME, lengthSuffix);
    }

    public static final boolean isValidLength(int length) {
        return length == DYNAMIC_LENGTH || (MIN_STATIC_LENGTH <= length && length <= MAX_STATIC_LENGTH);
    }

}
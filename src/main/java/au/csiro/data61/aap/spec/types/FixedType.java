package au.csiro.data61.aap.spec.types;

/**
 * FixedType
 */
public class FixedType extends SolidityType {
    private static final String NAME = "fixed";
    private static final int MIN_M = 8;
    private static final int MAX_M = 256;
    private static final int MIN_N = 0;
    private static final int MAX_N = 80;
    private static final int FIXED_DEFAULT_M = 128;
    private static final int FIXED_DEFAULT_N = 18;    

    public static final FixedType DEFAULT_INSTANCE = new FixedType(true);    
    
    private final int m;
    private final int n;
    private final boolean signed;
    
    public FixedType(boolean signed) {
        this(signed, FIXED_DEFAULT_M, FIXED_DEFAULT_N);
    }

    public FixedType(boolean signed, int m, int n) {
        super(FixedType.class, IntegerType.class);
        assert isValidMValue(m);
        assert isValidNValue(n);
        this.signed = signed;
        this.m = m;
        this.n = n;
    }

    public boolean isUnsigned() {
        return !this.signed;
    }

    public int getM() {
        return this.m;
    }

    public int getN() {
        return this.n;
    }

    @Override
    public String getName() {
        final String unsignedPrefix = this.signed ? "" : "u";
        return String.format("%s%s%sx%s", unsignedPrefix, NAME, this.m, this.n); 
    }

    public static boolean isValidMValue(int value) {
        return value % 8 == 0 && MIN_M <= value && value <= MAX_M;
    }

    public static boolean isValidNValue(int value) {
        return MIN_N <= value && value <= MAX_N;
    }
}
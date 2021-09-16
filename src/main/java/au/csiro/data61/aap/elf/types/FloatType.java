package au.csiro.data61.aap.elf.types;

public final class FloatType extends PrimitiveType {
    private static final String NAME = "float";
    public static final FloatType INSTANCE = new FloatType();

    public FloatType() {
        super(NAME);
    }
}

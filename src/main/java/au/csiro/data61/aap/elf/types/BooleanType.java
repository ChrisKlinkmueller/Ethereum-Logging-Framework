package au.csiro.data61.aap.elf.types;

public final class BooleanType extends PrimitiveType {
    private static final String NAME = "boolean";
    public static final BooleanType INSTANCE = new BooleanType();

    private BooleanType() {
        super(NAME);
    }
}

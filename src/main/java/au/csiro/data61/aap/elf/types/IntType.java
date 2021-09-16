package au.csiro.data61.aap.elf.types;

public final class IntType extends PrimitiveType {
    private static final String NAME = "int";
    public static final IntType INSTANCE = new IntType();

    private IntType() {
        super(NAME);
    }
}

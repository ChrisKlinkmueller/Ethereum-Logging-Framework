package au.csiro.data61.aap.elf.types;

public final class VoidType extends PrimitiveType {
    private static final String NAME = "void";
    public static final VoidType INSTANCE = new VoidType();

    private VoidType() {
        super(NAME);
    }
}

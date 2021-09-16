package au.csiro.data61.aap.elf.types;

public final class StringType extends PrimitiveType {
    private static final String NAME = "string";
    public static final StringType INSTANCE = new StringType();

    private StringType() {
        super(NAME);
    }
    
}

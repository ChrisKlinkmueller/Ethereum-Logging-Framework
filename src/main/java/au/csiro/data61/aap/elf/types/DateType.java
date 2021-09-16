package au.csiro.data61.aap.elf.types;

public final class DateType extends PrimitiveType {
    private static final String NAME = "date";
    public static final DateType INSTANCE = new DateType();

    private DateType() {
        super(NAME);
    }
}

package au.csiro.data61.aap.library.types;

import java.util.Objects;

public class StringType extends BytesType {
    private static final String NAME = "string";
    private static final StringType INSTANCE = new StringType();

    StringType() {
        super();
    }

    @Override
    public String getTypeName() {
        return NAME;
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        return obj instanceof StringType;
    }

    static SolidityType<?> createStringType(String keyword) {
        return keyword.equals(NAME) ? INSTANCE : null;
    }
        
}
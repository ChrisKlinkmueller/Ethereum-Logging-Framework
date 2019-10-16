package au.csiro.data61.aap.specification.types;

import java.util.Objects;

public class StringType extends BytesType {
    private static final String BASE_NAME = "string";
    
    public StringType() {
        super(DYNAMIC_LENGTH);
    }

    @Override
    public String getTypeName() {
        return BASE_NAME;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(BASE_NAME);
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
}
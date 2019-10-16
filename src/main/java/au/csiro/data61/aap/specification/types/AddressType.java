package au.csiro.data61.aap.specification.types;

import java.util.Objects;

/**
 * AddressType
 */
public class AddressType extends BytesType {
    private static final int ARRAY_LENGTH = 20;
    private static final String BASE_NAME = "address";
    
    public AddressType() {
        super(ARRAY_LENGTH);
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

        return obj instanceof AddressType;
    }
}
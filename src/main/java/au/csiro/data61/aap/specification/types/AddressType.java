package au.csiro.data61.aap.specification.types;

import java.util.Objects;

/**
 * AddressType
 */
public class AddressType extends BytesType {
    private static final int ARRAY_LENGTH = 20;
    private static final String NAME = "address";
    private static final AddressType INSTANCE = new AddressType();

    AddressType() {
        super(ARRAY_LENGTH);
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

        return obj instanceof AddressType;
    }

    static SolidityType<?> createAddressType(String keyWord) {
        if (keyWord.equals(NAME)) {
            return INSTANCE;
        }
        return null;
    }
}
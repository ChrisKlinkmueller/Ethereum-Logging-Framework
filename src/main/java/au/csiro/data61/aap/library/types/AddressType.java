package au.csiro.data61.aap.library.types;

/**
 * AddressType
 */
public class AddressType extends BytesType {
    private static final int ARRAY_LENGTH = 20;
    private static final String NAME = "address";

    public AddressType() {
        super(ARRAY_LENGTH);
    }

    @Override
    public String getTypeName() {
        return NAME;
    }

    @Override
    public int hashCode() {
        final int prime = 53;
        final int hash = 59;
        return hash + prime * hash + NAME.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        return obj instanceof AddressType;
    }
}
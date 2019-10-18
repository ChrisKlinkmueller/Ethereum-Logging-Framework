package au.csiro.data61.aap.spec.types;

/**
 * ArrayType
 */
public class AddressType extends SolidityType {
    private static final String NAME = "address";
    
    public static final AddressType DEFAULT_INSTANCE = new AddressType();

    public AddressType() {
        super(AddressType.class);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
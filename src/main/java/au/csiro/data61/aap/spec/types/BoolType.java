package au.csiro.data61.aap.spec.types;

/**
 * BoolType
 */
public class BoolType extends SolidityType {
    private static final String NAME = "bool";
    
    public static final BoolType DEFAULT_INSTANCE = new BoolType();

    @Override
    public String getName() {
        return NAME;
    }    
}
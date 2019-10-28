package au.csiro.data61.aap.spec.types;

/**
 * VoidType
 */
public class VoidType extends SolidityType {
    private static final String NAME = "void";
    public static final VoidType DEFAULT_INSTANCE = new VoidType();

    public VoidType() {
        super();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
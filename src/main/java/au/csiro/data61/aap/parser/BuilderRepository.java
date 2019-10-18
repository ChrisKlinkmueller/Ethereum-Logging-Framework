package au.csiro.data61.aap.parser;

/**
 * VisitorRepository
 */
public class BuilderRepository {
    public static final SolidityTypeBuilder SOLIDITY_TYPE_BUILDER;
    
    static {
        SOLIDITY_TYPE_BUILDER = new SolidityTypeBuilder();
    }
}
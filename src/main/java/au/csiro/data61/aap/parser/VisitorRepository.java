package au.csiro.data61.aap.parser;

/**
 * VisitorRepository
 */
public class VisitorRepository {
    public static final SolidityTypeVisitor SOLIDITY_TYPE_VISITOR;
    public static final VariableVisitor VARIABLE_VISITOR;
    
    static {
        SOLIDITY_TYPE_VISITOR = new SolidityTypeVisitor();
        VARIABLE_VISITOR = new VariableVisitor();
    }
}
package au.csiro.data61.aap.parser;

/**
 * VisitorRepository
 */
class VisitorRepository {
    public static final SolidityTypeVisitor SOLIDITY_TYPE_VISITOR;
    public static final VariableVisitor VARIABLE_VISITOR;
    public static final ScopeBuilderVisitor SCOPE_VISITOR;
    
    static {
        SOLIDITY_TYPE_VISITOR = new SolidityTypeVisitor();
        VARIABLE_VISITOR = new VariableVisitor();
        SCOPE_VISITOR = new ScopeBuilderVisitor();
    }
}
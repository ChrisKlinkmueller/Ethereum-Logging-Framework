package au.csiro.data61.aap.parser;

/**
 * VisitorRepository
 */
class VisitorRepository {
    private static final VariableDefinitionVisitor varDefVisitor;
    private static final SolidityTypeVisitor solTypeVisitor;
    static {
        varDefVisitor = new VariableDefinitionVisitor();
        solTypeVisitor = new SolidityTypeVisitor();
    }

    public static VariableDefinitionVisitor getVariableDefinitionVisitor() {
        return varDefVisitor;
    }

    public static SolidityTypeVisitor getSolidityTypeVisitor() {
        return solTypeVisitor;
    }
    
}
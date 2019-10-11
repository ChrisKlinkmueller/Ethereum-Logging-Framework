package au.csiro.data61.aap.parser;

/**
 * VisitorRepository
 */
class VisitorRepository {
    private static final VariableDefinitionVisitor varDefVisitor;
    private static final SolidityTypeVisitor solTypeVisitor;
    private static final BlockVisitor blockVisitor;

    static {
        varDefVisitor = new VariableDefinitionVisitor();
        solTypeVisitor = new SolidityTypeVisitor();
        blockVisitor = new BlockVisitor();
    }

    public static VariableDefinitionVisitor getVariableDefinitionVisitor() {
        return varDefVisitor;
    }

    public static SolidityTypeVisitor getSolidityTypeVisitor() {
        return solTypeVisitor;
    }

    public static BlockVisitor getBlockVisitor() {
        return blockVisitor;
    }
    
}
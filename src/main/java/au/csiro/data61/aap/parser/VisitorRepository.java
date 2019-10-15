package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.state.ProgramState;

/**
 * VisitorRepository
 */
class VisitorRepository {
    private static final VariableDefinitionVisitor varDefVisitor;
    private static final SolidityTypeVisitor solTypeVisitor;
    private static final BlockVisitor blockVisitor;

    static {
        final ProgramState state = new ProgramState();
        varDefVisitor = new VariableDefinitionVisitor(state);
        solTypeVisitor = new SolidityTypeVisitor(state);
        blockVisitor = new BlockVisitor(state);
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
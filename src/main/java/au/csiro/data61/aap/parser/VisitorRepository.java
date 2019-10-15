package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.state.ProgramState;

/**
 * VisitorRepository
 */
class VisitorRepository {
    private static final ProgramState programState;
    private static final VariableDefinitionVisitor varDefVisitor;
    private static final SolidityTypeVisitor solTypeVisitor;
    private static final BlockVisitor blockVisitor;

    static {
        programState = new ProgramState();
        varDefVisitor = new VariableDefinitionVisitor(programState);
        solTypeVisitor = new SolidityTypeVisitor(programState);
        blockVisitor = new BlockVisitor(programState);
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

    public static void clearProgramState() {
        programState.clearVariables();
    }
    
}
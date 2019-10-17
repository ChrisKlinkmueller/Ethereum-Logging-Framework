package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.specification.ProgramState;

/**
 * VisitorRepository
 */
class VisitorRepository {
    private static final BlockVisitor blockVisitor;
    private static final ConstantVisitor constantVisitor;
    private static final SolidityTypeVisitor solTypeVisitor;
    private static final VariableDefinitionVisitor varDefVisitor;
    private static final ProgramState programState;

    static {
        programState = new ProgramState();
        varDefVisitor = new VariableDefinitionVisitor(programState);
        solTypeVisitor = new SolidityTypeVisitor(programState);
        blockVisitor = new BlockVisitor(programState);
        constantVisitor = new ConstantVisitor(programState);
    }

    public static ConstantVisitor getConstantVisitor() {
        return constantVisitor;
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
        programState.clearValueContainers();
    }
    
}
package au.csiro.data61.aap.spec;

/**
 * Instruction
 */
public abstract class Instruction {
    private final Block enclosingBlock;

    protected Instruction(Block enclosingBlock) {
        this.enclosingBlock = enclosingBlock;
    }

    public Block getEnclosingBlock() {
        return this.enclosingBlock;
    }
    
    
}
package au.csiro.data61.aap.spec;

/**
 * Block
 */
public class Block extends Instruction {

    public Block(Block enclosingBlock) {
        super(enclosingBlock);
    }
    
}
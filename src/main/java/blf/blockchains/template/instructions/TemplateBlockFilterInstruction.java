package blf.blockchains.template.instructions;

import blf.blockchains.template.TemplateProgramState;
import blf.core.exceptions.ExceptionHandler;
import blf.core.instructions.BlockInstruction;
import blf.core.instructions.Instruction;
import blf.core.state.ProgramState;
import blf.core.values.ValueStore;
import blf.grammar.BcqlParser;
import org.antlr.v4.runtime.misc.Pair;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * This class handles the 'BLOCKS (fromBlock) (toBlock)' filter of the .bcql file.
 */
public class TemplateBlockFilterInstruction extends BlockInstruction {

    private final BcqlParser.BlockFilterContext blockCtx;

    private final Logger logger;

    public TemplateBlockFilterInstruction(BcqlParser.BlockFilterContext blockCtx, List<Instruction> nestedInstructions) {
        // here the list of nested instructions is created
        super(nestedInstructions);

        this.blockCtx = blockCtx;
        this.logger = Logger.getLogger(TemplateBlockFilterInstruction.class.getName());
    }

    @Override
    public void execute(final ProgramState state) {

        final TemplateProgramState templateProgramState = (TemplateProgramState) state;

        // parse blocks from the blockCtx replace the next line with your extraction logic
        this.logger.info("TODO: extract from and to block from the block context.");
        Pair<BigInteger, BigInteger> pairOfBlockNumbers = new Pair<BigInteger, BigInteger>(BigInteger.valueOf(0), BigInteger.valueOf(100));

        final BigInteger fromBlockNumber = pairOfBlockNumbers.a;
        final BigInteger toBlockNumber = pairOfBlockNumbers.b;

        BigInteger currentBlock = fromBlockNumber;

        while (currentBlock.compareTo(toBlockNumber) < 1) {
            this.logger.info(String.format("Current block: %s", currentBlock.toString()));
            this.logger.info("TODO: extract the block with the current block number and store it in the program state.");
            // to something with the current block
            templateProgramState.setCurrentBlockNumber(currentBlock);
            this.logger.info("Execute nested block instructions.");
            this.executeNestedInstructions(templateProgramState);
            currentBlock = currentBlock.add(BigInteger.valueOf(1));
        }
    }
}

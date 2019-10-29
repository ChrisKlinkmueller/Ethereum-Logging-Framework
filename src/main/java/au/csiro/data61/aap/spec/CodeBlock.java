package au.csiro.data61.aap.spec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Block
 */
public class CodeBlock extends Instruction {
    private final List<Instruction> instructions;
    private final Filter filter;

    public CodeBlock() {
        super(null);
        this.filter = null;
        this.instructions = new ArrayList<>();
    }
    
    public CodeBlock(CodeBlock enclosingBlock, Filter filter) {
        super(enclosingBlock);
        assert enclosingBlock != null;
        assert filter != null;
        this.instructions = new ArrayList<>();
        this.filter = filter;
    }

    public void addInstruction(Instruction instruction) {
        this.instructions.add(instruction);
    }

    public int instructionCount() {
        return this.instructions.size();
    }

    public Instruction getInstruction(int index) {
        assert 0 <= index && index < this.instructionCount();
        return this.instructions.get(index);
    }

    public Stream<Instruction> instructionStream() {
        return this.instructions.stream();
    }

    public Filter getFilter() {
        return this.filter;
    }
    
    public boolean isVariableNameReserved(String variableName) {
        assert variableName != null;
        // TODO: in case of a logentry block, the log entry variables must be included!
        
        return RESERVED_VARIABLE_NAMES.contains(variableName);


    }

    private static final Set<String> RESERVED_VARIABLE_NAMES;
    
    static {
        RESERVED_VARIABLE_NAMES = new HashSet<>();

        // BLOCK VARIABLES
        RESERVED_VARIABLE_NAMES.add("block.number");
        RESERVED_VARIABLE_NAMES.add("block.hash");
        RESERVED_VARIABLE_NAMES.add("block.parentHash");
        RESERVED_VARIABLE_NAMES.add("block.nonce");
        RESERVED_VARIABLE_NAMES.add("block.sha3Uncles");
        RESERVED_VARIABLE_NAMES.add("block.logsBloom");
        RESERVED_VARIABLE_NAMES.add("block.transactionsRoot");
        RESERVED_VARIABLE_NAMES.add("block.stateRoot");
        RESERVED_VARIABLE_NAMES.add("block.receiptsRoot");
        RESERVED_VARIABLE_NAMES.add("block.miner");
        RESERVED_VARIABLE_NAMES.add("block.difficulty");
        RESERVED_VARIABLE_NAMES.add("block.totalDifficulty");
        RESERVED_VARIABLE_NAMES.add("block.extraData");
        RESERVED_VARIABLE_NAMES.add("block.size");
        RESERVED_VARIABLE_NAMES.add("block.gasLimit");
        RESERVED_VARIABLE_NAMES.add("block.gasUsed");
        RESERVED_VARIABLE_NAMES.add("block.imestamp");
        RESERVED_VARIABLE_NAMES.add("block.transactions");
        RESERVED_VARIABLE_NAMES.add("block.uncles");
        
        // TRANSACTION VARIABLES
        RESERVED_VARIABLE_NAMES.add("tx.blockHash");
        RESERVED_VARIABLE_NAMES.add("tx.blockNumber");
        RESERVED_VARIABLE_NAMES.add("tx.from");
        RESERVED_VARIABLE_NAMES.add("tx.gas");
        RESERVED_VARIABLE_NAMES.add("tx.gasPrice");
        RESERVED_VARIABLE_NAMES.add("tx.hash");
        RESERVED_VARIABLE_NAMES.add("tx.input");
        RESERVED_VARIABLE_NAMES.add("tx.nonce");
        RESERVED_VARIABLE_NAMES.add("tx.to");
        RESERVED_VARIABLE_NAMES.add("tx.transactionIndex");
        RESERVED_VARIABLE_NAMES.add("tx.value");
        RESERVED_VARIABLE_NAMES.add("tx.v");
        RESERVED_VARIABLE_NAMES.add("tx.r");
        RESERVED_VARIABLE_NAMES.add("tx.s");

        // LOG ENTRY VARIABLES
        RESERVED_VARIABLE_NAMES.add("log.removed");
        RESERVED_VARIABLE_NAMES.add("log.logIndex");
        RESERVED_VARIABLE_NAMES.add("log.transactionIndex");
        RESERVED_VARIABLE_NAMES.add("log.transactionHash");
        RESERVED_VARIABLE_NAMES.add("log.blockHash");
        RESERVED_VARIABLE_NAMES.add("log.blockNumber");
        RESERVED_VARIABLE_NAMES.add("log.address");
    }
}
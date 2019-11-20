package au.csiro.data61.aap.etl.configuration;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Function;

import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.Method;
import au.csiro.data61.aap.etl.core.MethodCall;
import au.csiro.data61.aap.etl.core.ValueAccessor;
import au.csiro.data61.aap.etl.core.ValueMutator;
import au.csiro.data61.aap.etl.core.VariableAssignment;
import au.csiro.data61.aap.etl.core.filters.BlockRangeFilter;
import au.csiro.data61.aap.etl.core.filters.Program;
import au.csiro.data61.aap.etl.core.filters.TransactionFilter;
import au.csiro.data61.aap.etl.core.variables.EthereumVariables;
import au.csiro.data61.aap.etl.core.variables.Literal;
import au.csiro.data61.aap.etl.core.variables.UserVariables;

/**
 * ProgramFactory
 */
public class ProgramBuilder {
    private final Stack<FactoryState> states;
    private final Stack<List<Instruction>> instructions;

    public ProgramBuilder() {
        this.instructions = new Stack<>();
        this.states = new Stack<>();
    }

    public void prepareProgramBuild() throws BuildException  {
        if (!this.states.isEmpty()) {
            throw new BuildException("Cannot build a program, while another program is still being build.");
        }
        
        this.addScope(FactoryState.PROGRAM);
    }

    public void prepareBlockRangeBuild() throws BuildException {
        if (this.states.peek() != FactoryState.PROGRAM) {
            throw new BuildException("A block range filter can only be added to a program.");
        }
        this.addScope(FactoryState.BLOCK_RANGE_FILTER);        
    }

    public void prepareTransactionFilterBuild() throws BuildException {
        if (this.states.peek() != FactoryState.BLOCK_RANGE_FILTER) {
            throw new BuildException("A block range filter can only be added to a block filter.");
        }
        this.addScope(FactoryState.TRANSACTION_FILTER);  
    }

    private void addScope(FactoryState state) {
        this.states.push(state);
        this.instructions.add(new LinkedList<>());
    }

    public Instruction buildProgram() throws BuildException {
        if (this.states.peek() != FactoryState.PROGRAM) {
            throw new BuildException(String.format("Cannot build a program, when construction of %s has not been finished.", this.states.peek()));
        }
        
        final Program program = new Program(this.instructions.pop());
        this.states.pop();
        return program;
    }

    public void buildBlockRange(ValueAccessor fromBlock, ValueAccessor toBlock) throws BuildException {
        assert fromBlock != null;
        assert toBlock != null;

        if (this.states.peek() != FactoryState.BLOCK_RANGE_FILTER) {
            throw new BuildException(String.format("Cannot build a block filter, when construction of %s has not been finished.", this.states.peek()));
        }

        final List<Instruction> instructions = this.instructions.pop();
        final BlockRangeFilter blockRange = new BlockRangeFilter(fromBlock, toBlock, instructions);
        this.instructions.peek().add(blockRange);
        this.states.pop();
    }

    public void buildTransactionFilter(List<String> senders, List<String> recipients) throws BuildException {
        if (this.states.peek() != FactoryState.TRANSACTION_FILTER) {
            throw new BuildException(String.format("Cannot build a transaction filter, when construction of %s has not been finished.", this.states.peek()));
        }

        final List<Instruction> instructions = this.instructions.pop();
        final TransactionFilter filter = new TransactionFilter(
            senders == null ? null : Literal.addressLiteral(senders), 
            recipients == null ? null : Literal.addressLiteral(recipients), 
            instructions
        );

        this.instructions.peek().add(filter);        
        this.states.pop();
    }

    public void addInstruction(Instruction instruction) throws BuildException {
        if (instruction == null) {
            throw new BuildException(String.format("Parameter instruction is null."));
        }
        this.instructions.peek().add(instruction);
    }

    public void addMethodCall(Method method, List<ValueAccessor> parameterAccessors, ValueMutator resultStorer) throws BuildException {
        if (method == null || parameterAccessors == null || parameterAccessors.stream().anyMatch(Objects::isNull)) {
            throw new BuildException(String.format("Null parameters detected: method = %s, parameterAccessors = %s.", method, parameterAccessors));
        }

        final Instruction methodCall = new MethodCall(method, parameterAccessors, resultStorer);
        this.instructions.peek().add(methodCall);
    }

    public void addDataSourceVariableCreationInstruction(String name) throws BuildException {
        this.addDataSourceInstruction(name, EthereumVariables::createValueCreationInstruction);        
    }

    public void addDataSourceVariableRemovalInstruction(String name) throws BuildException {
        this.addDataSourceInstruction(name, EthereumVariables::createValueRemovalInstruction);
    }

    private void addDataSourceInstruction(String name, Function<String, Instruction> instructionRetriever) throws BuildException {
        final Instruction instruction = instructionRetriever.apply(name);
        if (instruction == null) {
            throw new BuildException(String.format("'%s' is not a valid Ethereum variable.", name));
        }
        this.instructions.peek().add(instruction);
    }

	public void addVariableAssignmentWithIntegerValue(String name, long value) {
        final Instruction varAssignment = new VariableAssignment(UserVariables.createValueMutator(name), Literal.integerLiteral(value));
        this.instructions.peek().add(varAssignment);
	}

    private static enum FactoryState {
        PROGRAM,
        BLOCK_RANGE_FILTER,
        TRANSACTION_FILTER,
        LOG_ENTRY_FILTER,
        SMART_CONTRACT_FILTER,
        INSTRUCTION;

        @Override
        public String toString() {
            switch (this) {
                case PROGRAM : return "program";
                case BLOCK_RANGE_FILTER : return "block range filter";
                case TRANSACTION_FILTER : return "transaction filter";
                case LOG_ENTRY_FILTER : return "log entry filter";
                case SMART_CONTRACT_FILTER : return "smart contract filter";
                case INSTRUCTION : return "instruction";
                default : throw new IllegalArgumentException(String.format("FactoryState constant '%s' unknown.", this));
            }
        }
    }
    
    

}
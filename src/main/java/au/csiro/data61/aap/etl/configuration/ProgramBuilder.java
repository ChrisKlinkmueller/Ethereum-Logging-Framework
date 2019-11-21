package au.csiro.data61.aap.etl.configuration;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;

import au.csiro.data61.aap.etl.configuration.BlockNumberSpecification.Type;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.Method;
import au.csiro.data61.aap.etl.core.MethodCall;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;
import au.csiro.data61.aap.etl.core.values.ValueMutator;
import au.csiro.data61.aap.etl.core.VariableAssignment;
import au.csiro.data61.aap.etl.core.filters.BlockFilter;
import au.csiro.data61.aap.etl.core.filters.Program;
import au.csiro.data61.aap.etl.core.filters.TransactionFilter;
import au.csiro.data61.aap.etl.core.values.Literal;
import au.csiro.data61.aap.etl.core.values.UserVariables;

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
        this.prepareBuild(FactoryState.PROGRAM);
    }

    public void prepareBlockRangeBuild() throws BuildException {
        this.prepareBuild(FactoryState.BLOCK_RANGE_FILTER, FactoryState.PROGRAM);     
    }

    public void prepareTransactionFilterBuild() throws BuildException {
        this.prepareBuild(FactoryState.TRANSACTION_FILTER, FactoryState.BLOCK_RANGE_FILTER);
    }

    private void prepareBuild(FactoryState newState, FactoryState... possibleCurrentStates) throws BuildException  {
        final boolean areStatesEmpty = this.states.isEmpty() && possibleCurrentStates.length == 0;
        final boolean currentStatesMatches = !this.states.isEmpty()
            && Arrays.stream(possibleCurrentStates).anyMatch(s -> this.states.peek() == s);
        
        if (!(areStatesEmpty || currentStatesMatches)) {
            throw new BuildException(
                possibleCurrentStates == null 
                ? String.format("A %s can only be build when no other filter is being build.", newState)
                : String.format(
                    "A %s cannot be added to %s, but only to: %s.", 
                    newState, 
                    this.states.peek(), 
                    Arrays.stream(possibleCurrentStates).map(state -> state.toString()).collect(Collectors.joining(", "))
                )
            );
        }
       
        this.states.push(newState);
        this.instructions.add(new LinkedList<>());
    }

    public Instruction buildProgram() throws BuildException {
        if (this.states.peek() != FactoryState.PROGRAM) {
            throw new BuildException(String.format("Cannot build a program, when construction of %s has not been finished.", this.states.peek()));
        }
        
        final Program program = new Program(this.instructions.peek());
        this.closeScope(program);
        return program;
    }

    public void buildBlockRange(BlockNumberSpecification fromBlock, BlockNumberSpecification toBlock) throws BuildException {
        assert fromBlock != null && fromBlock.getType() != Type.CONTINUOUS;
        assert toBlock != null && toBlock.getType() != Type.EARLIEST;

        if (this.states.peek() != FactoryState.BLOCK_RANGE_FILTER) {
            throw new BuildException(String.format("Cannot build a block filter, when construction of %s has not been finished.", this.states.peek()));
        }

        final BlockFilter blockRange = new BlockFilter(
            fromBlock.getValueAccessor(), 
            toBlock.getStopCriterion(), 
            this.instructions.peek()
        );

        this.closeScope(blockRange);
    }

    public void buildTransactionFilter(AddressListSpecification senders, AddressListSpecification recipients) throws BuildException {
        assert senders != null;
        assert recipients != null;

        if (this.states.peek() != FactoryState.TRANSACTION_FILTER) {
            throw new BuildException(String.format("Cannot build a transaction filter, when construction of %s has not been finished.", this.states.peek()));
        }

        final TransactionFilter filter = new TransactionFilter(
            senders.getAddressCheck(), 
            recipients.getAddressCheck(), 
            this.instructions.peek()
        );

        this.closeScope(filter);        
    }

    private void closeScope(Instruction instruction) {
        this.instructions.pop();
        if (!this.instructions.isEmpty()) {
            this.instructions.peek().add(instruction);        
        }
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
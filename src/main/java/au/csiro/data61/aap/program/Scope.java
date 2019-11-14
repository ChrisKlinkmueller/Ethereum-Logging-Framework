package au.csiro.data61.aap.program;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.suppliers.BlockchainVariable;
import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.program.types.SolidityType;
import au.csiro.data61.aap.program.types.ValueCasts.ValueCast;

/**
 * Block
 */
public abstract class Scope extends Instruction {
    private final List<Instruction> instructions;

    protected Scope() {
        this.instructions = new ArrayList<>();
    }

    protected static final <T> void addVariable(Set<Variable> variables, SolidityType type, String name, Function<T, Object> valueAccessor, ValueCast valueCast) {
        assert variables != null && type != null && name != null;
        variables.add(new BlockchainVariable<T>(type, name, valueAccessor, valueCast));
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

    public Stream<Variable> findVariablesWithinScope(Predicate<Variable> selectionCriterion) {
        assert selectionCriterion != null;
        return Stream.concat(this.variableStream(), this.instructionVariableStream())
            .filter(selectionCriterion)
            .distinct();
    }

    private Stream<Variable> instructionVariableStream() {
        return this.instructionStream().flatMap(Instruction::variableStream);
    }
}
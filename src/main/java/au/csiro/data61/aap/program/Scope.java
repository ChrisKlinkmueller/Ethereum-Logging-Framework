package au.csiro.data61.aap.program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.types.SolidityType;

/**
 * Block
 */
public abstract class Scope extends Instruction {
    private final List<Instruction> instructions;
    private final Map<String, Variable> variables;

    protected Scope(Set<Variable> variables) {
        assert variables != null && variables.stream().allMatch(Objects::nonNull);
        this.instructions = new ArrayList<>();
        this.variables = new HashMap<>();
        variables.stream()
            .forEach(variable -> this.variables.put(variable.getName(), new Variable(variable)));            
    }

    protected static final void addVariable(Set<Variable> variables, SolidityType type, String name) {
        assert variables != null && type != null && name != null;
        variables.add(new Variable(type, name, VariableCategory.SCOPE_VARIABLE, null));
    }
    
    protected Variable getVariable(String name) {
        assert name != null && this.variables.containsKey(name);
        return this.variables.get(name);
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

    @Override
    public Stream<Variable> variableStream() {
        return this.variables.values().stream();
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

    /*
    TODO: remove
    public Stream<Variable> variableStream() {
        return Stream.concat(
            this.defaultVariableStream(), 
            this.instructionStream().flatMap(instr -> {
                if (instr instanceof Statement) {
                    Statement stmt = (Statement)instr;
                    return stmt.getVariable().isEmpty() ? Stream.of() : Stream.of(stmt.getVariable().get());
                }
                else if (instr instanceof Scope) {
                    return ((Scope)instr).defaultVariableStream();
                }
                else {
                    // TODO: once emit blocks are introduced, check here
                    throw new UnsupportedOperationException();
                }
            })
            .filter(this.filterByName())
        );
    }

    public Stream<Variable> variableStream(Predicate<Variable> selectionCriterion) {
        assert selectionCriterion != null;
        return this.variableStream().filter(selectionCriterion);
    }

    private Predicate<Variable> filterByName() {
        final Set<String> knownNames = new HashSet<>();
        return variable -> {
            final boolean isKnown = knownNames.contains(variable.getName());
            knownNames.add(variable.getName());
            return !isKnown;
        };
    }*/
}
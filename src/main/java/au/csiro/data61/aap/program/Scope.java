package au.csiro.data61.aap.program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.types.SolidityType;
import au.csiro.data61.aap.program.types.ValueCasts.ValueCast;
import au.csiro.data61.aap.program.types.ValueCasts.ValueCastException;

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

    protected static <T> void extractValues(List<ValueExtractor<T>> extractors, T object, Scope scope) throws ValueCastException {
        assert extractors != null && extractors.stream().allMatch(Objects::nonNull);
        assert object != null;

        for (ValueExtractor<T> extractor : extractors) {
            extractor.setVariableValue(scope, object);
        }
    }
    
    protected static class ValueExtractor<T> {
        private final String variableName;
        private final Function<T, Object> attribute;
        private final ValueCast cast;
        
        public ValueExtractor(String variableName, Function<T, Object> blockAttribute) {
            this(variableName, blockAttribute, null);
        }
        
        public ValueExtractor(String variableName, Function<T, Object> attribute, ValueCast cast) {
            assert variableName != null;
            assert attribute != null;
            this.variableName = variableName;
            this.attribute = attribute;
            this.cast = cast;
        }

        public void setVariableValue(Scope scope, T object) throws ValueCastException {
            assert scope != null;
            assert object != null;

            final Variable variable = scope.getVariable(this.variableName);
            assert variable != null;

            final Object value = this.attribute.apply(object);
            assert value != null;

            variable.setValue(this.cast == null ? value : this.cast.cast(value));
        }
    }
}
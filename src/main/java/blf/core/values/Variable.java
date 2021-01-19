package blf.core.values;

import io.reactivex.annotations.NonNull;

import java.util.Set;

/**
 * DataSourceVariable
 */
public class Variable {
    private final String name;
    private final String type;
    private final ValueAccessor valueAccessor;

    public Variable(final String name, final String type, final ValueAccessor valueAccessor) {
        this.name = name;
        this.type = type;
        this.valueAccessor = valueAccessor;
    }

    public boolean hasName(String name) {
        return this.name.equals(name);
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public ValueAccessor getAccessor() {
        return this.valueAccessor;
    }

    public static void addVariable(Set<Variable> variables, String name, String type, ValueAccessor valueAccessor) {
        try {
            variables.add(new Variable(name, type, valueAccessor));
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}

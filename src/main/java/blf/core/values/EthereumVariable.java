package blf.core.values;

import io.reactivex.annotations.NonNull;

import java.util.Set;

/**
 * DataSourceVariable
 */
class EthereumVariable {
    private final String name;
    private final String type;
    private final ValueAccessor valueAccessor;

    public EthereumVariable(@NonNull final String name, @NonNull final String type, @NonNull final ValueAccessor valueAccessor) {
        this.name = name;
        this.type = type;
        this.valueAccessor = valueAccessor;
    }

    boolean hasName(String name) {
        return name != null && this.name.equals(name);
    }

    String getName() {
        return this.name;
    }

    String getType() {
        return this.type;
    }

    ValueAccessor getAccessor() {
        return this.valueAccessor;
    }

    static void addVariable(Set<EthereumVariable> variables, String name, String type, ValueAccessor valueAccessor) {
        try {
            variables.add(new EthereumVariable(name, type, valueAccessor));
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}

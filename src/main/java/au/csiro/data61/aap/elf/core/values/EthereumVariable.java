package au.csiro.data61.aap.elf.core.values;

import java.util.Set;

/**
 * DataSourceVariable
 */
class EthereumVariable {
    private final String name;
    private final String type;
    private final ValueAccessor valueAccessor;

    public EthereumVariable(final String name, final String type,
            final ValueAccessor valueAccessor) {
        assert name != null;
        assert type != null;
        assert valueAccessor != null;
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

    static <T> void addVariable(Set<EthereumVariable> variables, String name, String type,
            ValueAccessor valueAccessor) {
        try {
            variables.add(new EthereumVariable(name, type, valueAccessor));
        } catch (Throwable error) {
            error.printStackTrace();
        }
    }
}

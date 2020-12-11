package blf.core.writers;

import blf.core.values.ValueAccessor;

/**
 * CsvColumn
 */
public class CsvColumn {
    private final String name;
    private final ValueAccessor accessor;

    public CsvColumn(String name, ValueAccessor accessor) {
        assert name != null;
        assert accessor != null;
        this.name = name;
        this.accessor = accessor;
    }

    public String getName() {
        return this.name;
    }

    public ValueAccessor getAccessor() {
        return this.accessor;
    }

}

package au.csiro.data61.aap.elf.core.writers;

import au.csiro.data61.aap.elf.core.values.ValueAccessor;

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
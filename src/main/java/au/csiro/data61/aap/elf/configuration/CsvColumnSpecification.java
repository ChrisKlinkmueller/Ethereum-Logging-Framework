package au.csiro.data61.aap.elf.configuration;

import au.csiro.data61.aap.elf.core.writers.CsvColumn;

/**
 * CsvCellSpecification
 */
public class CsvColumnSpecification {
    private final CsvColumn column;

    private CsvColumnSpecification(CsvColumn column) {
        this.column = column;
    }

    public CsvColumn getColumn() {
        return this.column;
    }

    public static CsvColumnSpecification of(String name, ValueAccessorSpecification accessor) {
        assert name != null;
        assert accessor != null;
        return new CsvColumnSpecification(new CsvColumn(name, accessor.getValueAccessor()));
    }
}

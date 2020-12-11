package blf.configuration;

import blf.core.writers.CsvColumn;

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

package blf.configuration;

import blf.core.writers.CsvColumn;
import io.reactivex.annotations.NonNull;

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

    public static CsvColumnSpecification of(@NonNull String name, @NonNull ValueAccessorSpecification accessor) {
        return new CsvColumnSpecification(new CsvColumn(name, accessor.getValueAccessor()));
    }
}

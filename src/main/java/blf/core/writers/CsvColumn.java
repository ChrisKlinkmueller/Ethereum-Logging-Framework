package blf.core.writers;

import blf.core.values.ValueAccessor;
import io.reactivex.annotations.NonNull;

/**
 * CsvColumn
 */
public class CsvColumn {
    private final String name;
    private final ValueAccessor accessor;

    public CsvColumn(@NonNull String name, @NonNull ValueAccessor accessor) {
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

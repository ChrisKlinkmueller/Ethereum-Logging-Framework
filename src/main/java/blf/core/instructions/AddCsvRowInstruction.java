package blf.core.instructions;

import blf.core.state.ProgramState;
import blf.core.values.ValueAccessor;
import blf.core.writers.CsvColumn;
import blf.core.writers.CsvWriter;
import io.reactivex.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * CsvRowCreator
 */
public class AddCsvRowInstruction extends Instruction {
    private final List<CsvColumn> columns;
    private final ValueAccessor tableName;

    public AddCsvRowInstruction(@NonNull ValueAccessor tableName, @NonNull List<CsvColumn> columns) {
        this.columns = new ArrayList<>(columns);
        this.tableName = tableName;
    }

    @Override
    public void execute(ProgramState state) {
        final CsvWriter writer = state.getWriters().getCsvWriter();
        final String tbName;
        tbName = (String) this.tableName.getValue(state);

        writer.beginRow(tbName);

        for (final CsvColumn column : columns) {
            final Object value = column.getAccessor().getValue(state);
            writer.addCell(tbName, column.getName(), value);
        }

        writer.endRow(tbName);
    }

}

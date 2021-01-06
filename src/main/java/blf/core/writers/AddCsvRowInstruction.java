package blf.core.writers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import blf.core.exceptions.ProgramException;
import blf.core.values.ValueAccessor;
import blf.core.Instruction;
import blf.core.ProgramState;
import io.reactivex.annotations.NonNull;

/**
 * CsvRowCreator
 */
public class AddCsvRowInstruction implements Instruction {
    private final List<CsvColumn> columns;
    private final ValueAccessor tableName;

    public AddCsvRowInstruction(@NonNull ValueAccessor tableName, @NonNull List<CsvColumn> columns) {
        assert columns.stream().allMatch(Objects::nonNull);
        this.columns = new ArrayList<>(columns);
        this.tableName = tableName;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        final CsvWriter writer = state.getWriters().getCsvWriter();
        final String tbName = (String) this.tableName.getValue(state);
        writer.beginRow(tbName);

        for (final CsvColumn column : columns) {
            final Object value = column.getAccessor().getValue(state);
            writer.addCell(tbName, column.getName(), value);
        }

        writer.endRow(tbName);
    }

}

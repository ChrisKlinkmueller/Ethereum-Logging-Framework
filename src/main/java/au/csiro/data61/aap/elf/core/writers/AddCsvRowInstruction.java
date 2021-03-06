package au.csiro.data61.aap.elf.core.writers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import au.csiro.data61.aap.elf.core.exceptions.ProgramException;
import au.csiro.data61.aap.elf.core.values.ValueAccessor;
import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;

/**
 * CsvRowCreator
 */
public class AddCsvRowInstruction implements Instruction {
    private final List<CsvColumn> columns;
    private final ValueAccessor tableName;

    public AddCsvRowInstruction(ValueAccessor tableName, List<CsvColumn> columns) {
        assert columns != null && columns.stream().allMatch(Objects::nonNull);
        assert tableName != null;
        this.columns = new ArrayList<>(columns);
        this.tableName = tableName;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        final CsvWriter writer = state.getWriters().getCsvWriter();
        final String tableName = (String) this.tableName.getValue(state);
        writer.beginRow(tableName);

        for (int i = 0; i < columns.size(); i++) {
            final CsvColumn column = columns.get(i);
            final Object value = column.getAccessor().getValue(state);
            writer.addCell(tableName, column.getName(), value);
        }

        writer.endRow(tableName);
    }

}

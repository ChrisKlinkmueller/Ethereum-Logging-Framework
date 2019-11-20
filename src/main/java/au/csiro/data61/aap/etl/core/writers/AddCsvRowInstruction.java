package au.csiro.data61.aap.etl.core.writers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;

/**
 * CsvRowCreator
 */
public class AddCsvRowInstruction implements Instruction {
    private final List<String> names;
    private final List<ValueAccessor> valueAccessors;
    private final String tableName;

    public AddCsvRowInstruction(String tableName, List<String> names, List<ValueAccessor> valueAccessors) {
        assert names != null && names.stream().allMatch(Objects::nonNull);
        assert valueAccessors != null && valueAccessors.stream().allMatch(Objects::nonNull);
        assert names.size() == valueAccessors.size();
        assert tableName != null;
        this.names = new ArrayList<>(names);
        this.valueAccessors = new ArrayList<>(valueAccessors);
        this.tableName = tableName;
    }

    @Override
    public void execute(ProgramState state) throws ProgramException {
        final CsvWriter writer = state.getWriters().getCsvWriter();
        writer.beginRow(tableName);

        for (int i = 0; i < names.size(); i++) {
            final String name = names.get(i);
            final ValueAccessor valueAccessor = this.valueAccessors.get(i);
            final Object value = valueAccessor.getValue(state);
            writer.addCell(tableName, name, value);
        }

        writer.endRow(tableName);
    }
        
}
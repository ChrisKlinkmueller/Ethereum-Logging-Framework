package au.csiro.data61.aap.etl.library.output;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import au.csiro.data61.aap.etl.core.CsvSink;
import au.csiro.data61.aap.etl.core.EtlException;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.ValueAccessor;

/**
 * CsvRowCreator
 */
public class CsvRowCreator implements Instruction {
    private final List<String> names;
    private final List<ValueAccessor> valueAccessors;
    private final String tableName;

    public CsvRowCreator(String tableName, List<String> names, List<ValueAccessor> valueAccessors) {
        assert names != null && names.stream().allMatch(Objects::nonNull);
        assert valueAccessors != null && valueAccessors.stream().allMatch(Objects::nonNull);
        assert names.size() == valueAccessors.size();
        assert tableName != null;
        this.names = new ArrayList<>(names);
        this.valueAccessors = new ArrayList<>(valueAccessors);
        this.tableName = tableName;
    }

    @Override
    public void execute(ProgramState state) throws EtlException {
        final CsvSink sink = state.getCsvSink();
        sink.beginRow(tableName);

        for (int i = 0; i < names.size(); i++) {
            final String name = names.get(i);
            final ValueAccessor valueAccessor = this.valueAccessors.get(i);
            final Object value = valueAccessor.getValue(state);
            sink.addCell(tableName, name, value);
        }

        sink.endRow(tableName);
    }
        
}
package au.csiro.data61.aap.etl.configuration;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import au.csiro.data61.aap.etl.core.writers.AddCsvRowInstruction;

/**
 * CsvExportSpecification
 */
public class CsvExportSpecification extends InstructionSpecification {
    
    private CsvExportSpecification(AddCsvRowInstruction instruction) {
        super(instruction);
    }

    public static CsvExportSpecification of(String tableName, List<String> columnNames, List<ValueAccessorSpecification> valueAccessors) {
        assert tableName != null;
        assert columnNames != null && columnNames.stream().allMatch(Objects::nonNull);
        assert valueAccessors != null && valueAccessors.stream().allMatch(Objects::nonNull);
        assert columnNames.size() == valueAccessors.size();
        
        return new CsvExportSpecification(
            new AddCsvRowInstruction(
                tableName, 
                columnNames, 
                valueAccessors.stream()
                    .map(acc -> acc.getValueAccessor())
                    .collect(Collectors.toList())
            )
        );
    }
}
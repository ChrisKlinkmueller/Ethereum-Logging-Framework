package au.csiro.data61.aap.elf.configuration;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.core.writers.AddCsvRowInstruction;

/**
 * CsvExportSpecification
 */
public class CsvExportSpecification extends InstructionSpecification<AddCsvRowInstruction> {

    private CsvExportSpecification(AddCsvRowInstruction instruction) {
        super(instruction);
    }

    public static CsvExportSpecification of(ValueAccessorSpecification tableName, List<CsvColumnSpecification> columns) {
        assert tableName != null;
        assert columns != null && columns.stream().allMatch(Objects::nonNull);

        return new CsvExportSpecification(
            new AddCsvRowInstruction(tableName.getValueAccessor(), columns.stream().map(c -> c.getColumn()).collect(Collectors.toList()))
        );
    }
}

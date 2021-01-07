package blf.configuration;

import java.util.List;
import java.util.stream.Collectors;

import blf.core.writers.AddCsvRowInstruction;
import io.reactivex.annotations.NonNull;

/**
 * CsvExportSpecification
 */
public class CsvExportSpecification extends InstructionSpecification<AddCsvRowInstruction> {

    private CsvExportSpecification(AddCsvRowInstruction instruction) {
        super(instruction);
    }

    public static CsvExportSpecification of(@NonNull ValueAccessorSpecification tableName, @NonNull List<CsvColumnSpecification> columns) {

        return new CsvExportSpecification(
            new AddCsvRowInstruction(
                tableName.getValueAccessor(),
                columns.stream().map(CsvColumnSpecification::getColumn).collect(Collectors.toList())
            )
        );
    }
}

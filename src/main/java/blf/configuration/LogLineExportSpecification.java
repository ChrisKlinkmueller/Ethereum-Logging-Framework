package blf.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import blf.core.writers.AddLogLineInstruction;
import io.reactivex.annotations.NonNull;

/**
 * LogLineExportSpecification
 */
public class LogLineExportSpecification extends InstructionSpecification<AddLogLineInstruction> {

    private LogLineExportSpecification(AddLogLineInstruction instruction) {
        super(instruction);
    }

    public static LogLineExportSpecification ofValues(ValueAccessorSpecification... accessors) {
        return ofValues(Arrays.asList(accessors));
    }

    public static LogLineExportSpecification ofValues(@NonNull List<ValueAccessorSpecification> accessors) {
        return new LogLineExportSpecification(
            new AddLogLineInstruction(accessors.stream().map(ValueAccessorSpecification::getValueAccessor).collect(Collectors.toList()))
        );
    }

}

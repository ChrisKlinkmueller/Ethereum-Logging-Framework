package au.csiro.data61.aap.elf.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.core.writers.AddLogLineInstruction;

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

    public static LogLineExportSpecification ofValues(List<ValueAccessorSpecification> accessors) {
        assert accessors != null && accessors.stream().allMatch(Objects::nonNull);
        return new LogLineExportSpecification(new AddLogLineInstruction(accessors.stream()
                .map(ValueAccessorSpecification::getValueAccessor).collect(Collectors.toList())));
    }

}

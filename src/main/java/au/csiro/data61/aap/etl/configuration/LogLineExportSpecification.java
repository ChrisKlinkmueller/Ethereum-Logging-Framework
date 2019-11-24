package au.csiro.data61.aap.etl.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.writers.AddLogLineInstruction;

/**
 * LogLineExportSpecification
 */
public class LogLineExportSpecification extends InstructionSpecification {

    private LogLineExportSpecification(Instruction instruction) {
        super(instruction);
    }
    
    public static LogLineExportSpecification ofValues(ValueAccessorSpecification... accessors) {
        return ofValues(Arrays.asList(accessors));
    }

    public static LogLineExportSpecification ofValues(List<ValueAccessorSpecification> accessors) {
        assert accessors != null && accessors.stream().allMatch(Objects::nonNull);
        return new LogLineExportSpecification(
            new AddLogLineInstruction(
                accessors.stream()
                    .map(ValueAccessorSpecification::getValueAccessor)
                    .collect(Collectors.toList())
            )
        );
    }
    
}
package au.csiro.data61.aap.elf.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.core.writers.AddXesElementInstruction;
import au.csiro.data61.aap.elf.core.writers.AddXesEventInstruction;
import au.csiro.data61.aap.elf.core.writers.AddXesTraceInstruction;

/**
 * XesExportSpecification
 */
public class XesExportSpecification extends InstructionSpecification<AddXesElementInstruction> {
    
    private XesExportSpecification(AddXesElementInstruction instruction) {
        super(instruction);
    }

    public static XesExportSpecification ofEventExport(
        ValueAccessorSpecification pid, 
        ValueAccessorSpecification piid, 
        ValueAccessorSpecification eid, 
        XesParameterSpecification... parameters) {
        return ofEventExport(pid, piid, eid, Arrays.asList(parameters));
    }

    public static XesExportSpecification ofEventExport(
        ValueAccessorSpecification pid, 
        ValueAccessorSpecification piid, 
        ValueAccessorSpecification eid, 
        List<XesParameterSpecification> parameters) {
        assert parameters != null && parameters.stream().allMatch(Objects::nonNull);
        final AddXesEventInstruction instruction = new AddXesEventInstruction(
            pid == null ? null : pid.getValueAccessor(), 
            piid == null ? null : piid.getValueAccessor(), 
            eid == null ? null : eid.getValueAccessor(), 
            parameters.stream().map(XesParameterSpecification::getParameter).collect(Collectors.toList())
        );
        return new XesExportSpecification(instruction);
    }

    public static XesExportSpecification ofTraceExport(
        ValueAccessorSpecification pid, 
        ValueAccessorSpecification piid, 
        XesParameterSpecification... parameters) {
        return ofTraceExport(pid, piid, Arrays.asList(parameters));
    }

    public static XesExportSpecification ofTraceExport(
        ValueAccessorSpecification pid, 
        ValueAccessorSpecification piid, 
        List<XesParameterSpecification> parameters
    ) {
        assert parameters != null && parameters.stream().allMatch(Objects::nonNull);
        final AddXesTraceInstruction instruction = new AddXesTraceInstruction(
            pid == null ? null : pid.getValueAccessor(), 
            piid == null ? null : piid.getValueAccessor(),
            parameters.stream().map(XesParameterSpecification::getParameter).collect(Collectors.toList())
        );
        return new XesExportSpecification(instruction);
    }

}
package blf.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import blf.core.writers.AddXesElementInstruction;
import blf.core.writers.AddXesEventInstruction;
import blf.core.writers.AddXesTraceInstruction;
import io.reactivex.annotations.NonNull;

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
        XesParameterSpecification... parameters
    ) {
        return ofEventExport(pid, piid, eid, Arrays.asList(parameters));
    }

    public static XesExportSpecification ofEventExport(
        ValueAccessorSpecification pid,
        ValueAccessorSpecification piid,
        ValueAccessorSpecification eid,
        @NonNull List<XesParameterSpecification> parameters
    ) {
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
        XesParameterSpecification... parameters
    ) {
        return ofTraceExport(pid, piid, Arrays.asList(parameters));
    }

    public static XesExportSpecification ofTraceExport(
        ValueAccessorSpecification pid,
        ValueAccessorSpecification piid,
        @NonNull List<XesParameterSpecification> parameters
    ) {
        final AddXesTraceInstruction instruction = new AddXesTraceInstruction(
            pid == null ? null : pid.getValueAccessor(),
            piid == null ? null : piid.getValueAccessor(),
            parameters.stream().map(XesParameterSpecification::getParameter).collect(Collectors.toList())
        );
        return new XesExportSpecification(instruction);
    }

}

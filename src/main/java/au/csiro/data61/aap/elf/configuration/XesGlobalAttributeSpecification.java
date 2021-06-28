package au.csiro.data61.aap.elf.configuration;

import au.csiro.data61.aap.elf.core.values.ValueAccessor;
import au.csiro.data61.aap.elf.core.writers.AddXesGlobalTimestampInstruction;

public class XesGlobalAttributeSpecification extends InstructionSpecification<AddXesGlobalTimestampInstruction> {

    protected XesGlobalAttributeSpecification(AddXesGlobalTimestampInstruction instruction) {
        super(instruction);
    }

    public static XesGlobalAttributeSpecification of(ValueAccessorSpecification pid) {
        assert pid != null;
        final ValueAccessor accessor = pid == null ? null : pid.getValueAccessor();
        return new XesGlobalAttributeSpecification(new AddXesGlobalTimestampInstruction(accessor));
    }

}

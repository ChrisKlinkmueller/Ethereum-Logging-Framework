/*
package au.csiro.data61.aap.elf.configuration;

import au.csiro.data61.aap.elf.core.values.ValueAccessor;
import au.csiro.data61.aap.elf.core.writers.AddXesGlobalValueInstruction;

public class XesGlobalAttributeSpecification extends InstructionSpecification<AddXesGlobalValueInstruction> {

    protected XesGlobalAttributeSpecification(AddXesGlobalValueInstruction instruction) {
        super(instruction);
    }

    public static XesGlobalAttributeSpecification of(ValueAccessorSpecification pid, String attribute) {
        assert pid != null;
        assert attribute != null && !attribute.isBlank();
        final ValueAccessor accessor = pid == null ? null : pid.getValueAccessor();
        return new XesGlobalAttributeSpecification(new AddXesGlobalValueInstruction(accessor, attribute));
    }

}
*/
package au.csiro.data61.aap.elf.configuration;

import org.deckfour.xes.extension.XExtensionManager;

import au.csiro.data61.aap.elf.core.values.ValueAccessor;
import au.csiro.data61.aap.elf.core.writers.AddXesExtensionInstruction;

public class XesExtensionSpecification extends InstructionSpecification<AddXesExtensionInstruction> {

    protected XesExtensionSpecification(AddXesExtensionInstruction instruction) {
        super(instruction);
    }

    public static XesExtensionSpecification of(String prefix, ValueAccessorSpecification pid) {
        assert pid != null;
        assert prefix != null && XExtensionManager.instance().getByPrefix(prefix) != null;
        final ValueAccessor accessor = pid == null ? null : pid.getValueAccessor();
        return new XesExtensionSpecification(new AddXesExtensionInstruction(prefix, accessor));
    }

}

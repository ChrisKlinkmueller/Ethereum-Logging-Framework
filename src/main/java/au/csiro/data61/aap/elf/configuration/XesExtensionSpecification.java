package au.csiro.data61.aap.elf.configuration;

import au.csiro.data61.aap.elf.core.writers.AddXesExtensionInstruction;

public class XesExtensionSpecification extends InstructionSpecification<AddXesExtensionInstruction> {

    private XesExtensionSpecification(String prefix) {
        super(new AddXesExtensionInstruction(prefix));
    }

    public static XesExtensionSpecification of(String prefix) {
        assert prefix != null;
        return new XesExtensionSpecification(prefix);
    }
}

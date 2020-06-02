package au.csiro.data61.aap.elf.core.filters;

import au.csiro.data61.aap.elf.core.values.ValueAccessor;

/**
 * SmartContractParameter
 */
public class SmartContractParameter extends Parameter {
    private final ValueAccessor accessor;

    public SmartContractParameter(String solType, String name, ValueAccessor accessor) {
        super(solType, name);
        assert accessor != null;
        this.accessor = accessor;
    }

    public ValueAccessor getAccessor() {
        return this.accessor;
    }
}

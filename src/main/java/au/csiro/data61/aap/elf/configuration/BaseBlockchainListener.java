package au.csiro.data61.aap.elf.configuration;

import au.csiro.data61.aap.elf.core.filters.Program;
import au.csiro.data61.aap.elf.parsing.BcqlBaseListener;

/**
 *
 * BaseBlockchainListener
 *
 */
public abstract class BaseBlockchainListener extends BcqlBaseListener {

    protected Program program;

    public Program getProgram() {
        return this.program;
    }

}

package au.csiro.data61.aap.elf.generation;

import au.csiro.data61.aap.elf.parsing.BcqlBaseListener;

/**
 * BaseGenerator
 */
public class BaseGenerator extends BcqlBaseListener {
    protected final CodeCollector codeCollector;

    public BaseGenerator(CodeCollector codeCollector) {
        assert codeCollector != null;
        this.codeCollector = codeCollector;
    }
}

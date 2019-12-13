package au.csiro.data61.aap.elf.generation;

import au.csiro.data61.aap.elf.parsing.EthqlBaseListener;

/**
 * BaseGenerator
 */
public class BaseGenerator extends EthqlBaseListener {
    protected final CodeCollector codeCollector;
    
    public BaseGenerator(CodeCollector codeCollector) {
        assert codeCollector != null;
        this.codeCollector = codeCollector;
    }
}
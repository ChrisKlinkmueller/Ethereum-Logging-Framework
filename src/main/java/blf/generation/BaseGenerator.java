package blf.generation;

import blf.grammar.BcqlBaseListener;

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

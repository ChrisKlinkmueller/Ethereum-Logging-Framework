package au.csiro.data61.aap.elf.parsing;

import org.antlr.v4.runtime.Token;

/**
 * XbelAnalyzer
 */
public abstract class SemanticAnalyzer extends EthqlBaseListener {
    protected final ErrorCollector errorCollector;

    public SemanticAnalyzer(ErrorCollector errorCollector) {
        assert errorCollector != null;
        this.errorCollector = errorCollector;
    }

    public abstract void clear();

    protected void addError(Token token, String message) {
        this.errorCollector.addSemanticError(token, message);
    }
}

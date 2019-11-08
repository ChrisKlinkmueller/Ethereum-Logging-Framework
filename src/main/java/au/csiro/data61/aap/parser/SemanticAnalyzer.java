package au.csiro.data61.aap.parser;

import org.antlr.v4.runtime.Token;

/**
 * XbelAnalyzer
 */
abstract class SemanticAnalyzer extends XbelBaseListener {
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
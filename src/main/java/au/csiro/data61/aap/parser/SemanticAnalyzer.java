package au.csiro.data61.aap.parser;

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
    
}
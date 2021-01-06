package blf.parsing;

import blf.grammar.BcqlBaseListener;
import io.reactivex.annotations.NonNull;
import org.antlr.v4.runtime.Token;

/**
 * XbelAnalyzer
 */
public abstract class SemanticAnalyzer extends BcqlBaseListener {
    protected final ErrorCollector errorCollector;

    protected SemanticAnalyzer(@NonNull ErrorCollector errorCollector) {
        this.errorCollector = errorCollector;
    }

    public abstract void clear();

    protected void addError(Token token, String message) {
        this.errorCollector.addSemanticError(token, message);
    }
}

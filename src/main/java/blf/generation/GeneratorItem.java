package blf.generation;

import org.antlr.v4.runtime.Token;

/**
 * GeneratorItem
 */
class GeneratorItem {
    private final int line;
    private final int column;
    private final String specification;

    GeneratorItem(Token token, String specification) {
        assert token != null;
        assert specification != null;
        this.line = token.getLine();
        this.column = token.getCharPositionInLine();
        this.specification = specification;
    }

    int getColumn() {
        return this.column;
    }

    int getLine() {
        return this.line;
    }

    String getSpecification() {
        return this.specification;
    }
}

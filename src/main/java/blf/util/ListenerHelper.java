package blf.util;

import blf.core.exceptions.ExceptionHandler;
import blf.grammar.BcqlParser;

public interface ListenerHelper {
    static String getOutputFolderLiteral(BcqlParser.OutputFolderContext ctx) {
        final BcqlParser.LiteralContext literal = ctx.literal();
        final String literalText = literal.getText();

        if (literal.STRING_LITERAL() == null) {
            ExceptionHandler.getInstance().handleException("SET OUTPUT FOLDER parameter should be a String.", new NullPointerException());

            return null;
        }

        return literalText;
    }
}

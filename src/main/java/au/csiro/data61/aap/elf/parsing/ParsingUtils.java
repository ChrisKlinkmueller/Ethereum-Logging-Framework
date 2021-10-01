package au.csiro.data61.aap.elf.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import au.csiro.data61.aap.elf.parsing.InterpretationEvent.Type;

public final class ParsingUtils {
    
    private ParsingUtils() {}

    public static InterpretationResult<CharStream> charStreamfromInputStream(InputStream is) {
        try {
            final CharStream charStream = CharStreams.fromStream(is);
            return InterpretationResult.of(charStream);
        }
        catch (IOException ex) {
            final String msg = "Error parsing the input stream.";
            Logger.getGlobal().log(Level.SEVERE, msg, ex);
            return InterpretationResult.failure(new InterpretationEvent(Type.ERROR, msg, ex));
        }
    }
}

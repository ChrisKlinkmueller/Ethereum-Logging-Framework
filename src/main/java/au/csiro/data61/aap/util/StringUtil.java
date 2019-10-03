package au.csiro.data61.aap.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * StringUtil
 */
public class StringUtil {

    public static InputStream toStream(String string) {
        assert string != null;
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }

    public static MethodResult<Integer> parseInt(String string) {
        if (string == null) {
            return MethodResult.ofError("Parameter 'string' is null");
        }

        try {
            final int value = Integer.parseInt(string);
            return MethodResult.ofResult(value);
        }
        catch (NumberFormatException ex) {
            return MethodResult.ofError(String.format("'%s' isn't a valid integer value.", string), ex);
        }
        
    }
}
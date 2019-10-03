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
}
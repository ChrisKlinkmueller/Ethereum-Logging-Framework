package au.csiro.data61.aap.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * CollectionUtil
 */
public class CollectionUtil {

    @SafeVarargs
    public static <T> Set<T> arrayToSet(T... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
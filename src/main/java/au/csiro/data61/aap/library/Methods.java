package au.csiro.data61.aap.library;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import au.csiro.data61.aap.specification.MethodSignature;

/**
 * Methods
 */
public class Methods {
    private static final Map<String, Set<MethodSignature>> METHOD_SIGNATURES = new HashMap<String, Set<MethodSignature>>();

    static {
        // TODO: register methods here
    }

    public static void registerMethod(MethodSignature method) {
        if (method == null) {
            throw new IllegalArgumentException("Parameter 'method' is null.");
        }

        Set<MethodSignature> methods = METHOD_SIGNATURES.get(method.getName());
        if (methods == null) {
            methods = new HashSet<>();
            METHOD_SIGNATURES.put(method.getName(), methods);
        }

        if (!methods.contains(method)) {
            methods.add(method);
        }
    }

    public static void deregisterMethod(MethodSignature method) {
        if (method == null) {
            throw new IllegalArgumentException("Parameter 'method' must not be null.");
        }

        if (!METHOD_SIGNATURES.containsKey(method.getName())) {
            return;
        }

        final Set<MethodSignature> signatures = METHOD_SIGNATURES.get(method.getName());
        if (!signatures.contains(method)) {
            return;
        }

        signatures.remove(method);
        if (signatures.isEmpty()) {
            METHOD_SIGNATURES.remove(method.getName());
        }
    }

}
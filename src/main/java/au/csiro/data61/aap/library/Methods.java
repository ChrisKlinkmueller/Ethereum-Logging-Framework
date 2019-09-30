package au.csiro.data61.aap.library;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import au.csiro.data61.aap.specification.MethodSignature;
import au.csiro.data61.aap.specification.ScopeType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Methods
 */
public class Methods {
    private static final Map<String, Set<Method>> METHODS = new HashMap<String, Set<Method>>();

    static {
        // TODO: register methods here
    }

    public static MethodResult<Void> registerMethod(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("Parameter 'method' is null.");
        }

        final String methodName = method.getSignature().getName();
        Set<Method> methods = METHODS.get(methodName);
        if (methods == null) {
            methods = new HashSet<>();
            METHODS.put(methodName, methods);
        }

        if (!containsMethod(method.getSignature())) {
            methods.add(method);
            return MethodResult.ofResult();
        }
        else {
            final String errorMessage = String.format("Method with signature '%s' exists already.", method.getSignature().toString());
            return MethodResult.ofError(errorMessage);
        }
    }

    public static boolean existsAndIsVisible(MethodSignature signature, ScopeType scopeType) {
        final MethodResult<Method> methodLookupResult = getMethod(signature);
        
        if (!methodLookupResult.isSuccessful()) {
            return false;
        }
        
        final Method method = methodLookupResult.getResult();
        return method.isVisibleIn(scopeType);
    }

    private static boolean containsMethod(MethodSignature signature) {
        return getMethod(signature) != null;
    }

    public static MethodResult<Method> getMethod(MethodSignature signature) {
        final String methodName = signature.getName();
        final Set<Method> methods = METHODS.get(methodName);
        if (methods == null) {
            final String errorMessage = String.format("no such method: %s", signature);
            return MethodResult.ofError(errorMessage);
        }

        final Method registeredMethod = methods
            .stream()
            .filter(method -> method.getSignature().equals(signature))
            .findAny()
            .orElse(null);
        
        if (registeredMethod == null) {
            final String errorMessage = String.format("no such method: %s", signature);
            return MethodResult.ofError(errorMessage);
        }
        else {
            return MethodResult.ofResult(registeredMethod);
        }
    }

    public static MethodResult<Void> deregisterMethod(Method method) {
        if (method == null) {
            final String errorMessage = "Parameter 'method' must not be null.";
            return MethodResult.ofError(errorMessage);
        }

        return deregisterMethod(method.getSignature());
    }

    public static MethodResult<Void> deregisterMethod(MethodSignature signature) {
        if (signature == null) {
            final String errorMessage = "Parameter 'signature' must not be null.";
            return MethodResult.ofError(errorMessage);
        }

        final MethodResult<Method> methodLookupResult = getMethod(signature);
        if (!methodLookupResult.isSuccessful()) {
            return MethodResult.ofError(methodLookupResult);
        }

        final String methodName = signature.getName();
        final Set<Method> methods = METHODS.get(methodName);
        final Method registeredMethod = methodLookupResult.getResult();
        methods.remove(registeredMethod);
        if (methods.isEmpty()) {
            METHODS.remove(methodName);
        }
        
        return MethodResult.ofResult();
    }

}
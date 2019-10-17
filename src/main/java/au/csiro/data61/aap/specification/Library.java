package au.csiro.data61.aap.specification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.csiro.data61.aap.util.MethodResult;

/**
 * Library
 */
public class Library {
    private Map<String, List<Method>> methodRegister;
    
    public static final Library INSTANCE = new Library();

    private Library() {
        this.methodRegister = new HashMap<>();
    }

    public MethodResult<Void> addMethod(Method method) {
        if (method == null) {
            return MethodResult.ofError("Method parameter must reference an object, but was null.");
        }

        List<Method> methods = this.methodRegister.get(method.getSignature().getName());
        if (methods == null) {
            methods = new ArrayList<>();
            this.methodRegister.put(method.getSignature().getName(), methods);
        }

        if (this.containsMethod(method.getSignature(), methods)) {
            return MethodResult.ofError(String.format("A method with signature '%s' already exists.", method.getSignature().toString()));
        }

        methods.add(method);
        return MethodResult.ofResult();
    }

    public MethodResult<Void> removeMethod(Method method) {
        final List<Method> methods = this.methodRegister.get(method.getSignature().getName());
        if (methods == null) {
            return noSuchMethodError(method.getSignature());
        }

        final Method registeredMethod = this.retrieveMethod(method.getSignature(), methods);
        if (registeredMethod != null) {
            return noSuchMethodError(method.getSignature());
        }

        methods.remove(registeredMethod);
        if (methods.isEmpty()) {
            this.methodRegister.remove(method.getSignature().getName());
        }
        return MethodResult.ofResult();
    }

    public MethodResult<Method> lookupMethod(MethodSignature signature) {
        final List<Method> methods = this.methodRegister.get(signature.getName());
        if (methods == null) {
            return noSuchMethodError(signature);
        }

        final Method registeredMethod = this.retrieveMethod(signature, methods);
        if (registeredMethod != null) {
            return noSuchMethodError(signature);
        }

        return MethodResult.ofResult(registeredMethod);
    }

    private <T> MethodResult<T> noSuchMethodError(MethodSignature signature) {
        return MethodResult.ofError(String.format("No method with signature '%s' registered", signature));
    }

    private boolean containsMethod(MethodSignature signature, List<Method> methods) {
        return this.retrieveMethod(signature, methods) != null;
    }

    private Method retrieveMethod(MethodSignature signature, List<Method> methods) {
        return methods.stream()
            .filter(existingMethod -> existingMethod.getSignature().parameterListEquals(signature))
            .findFirst()
            .orElse(null);
    }
}
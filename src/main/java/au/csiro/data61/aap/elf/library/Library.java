package au.csiro.data61.aap.elf.library;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.vavr.control.Try;

public class Library {
    private final Map<String, List<Method>> methods;

    public Library() {
        this.methods = new HashMap<>();
    }

    public void addMethod(Method method) {
        checkNotNull(method);

        final List<Method> nameMethods = this.methods.computeIfAbsent(method.getSignature().getName(), n -> new LinkedList<>());
        checkArgument(!this.containsMethod(method, nameMethods));

        nameMethods.add(method);
    }

    public Try<Method> getMethod(MethodSignature signature) {
        checkNotNull(signature);

        final List<Method> nameMethods = this.methods.get(signature.getName());
        if (nameMethods.isEmpty()) {
            return this.noSuchMethod(signature);
        }

        final Method method = nameMethods.stream()
            .filter(m -> m.getSignature().equals(signature))
            .findAny().orElse(null);

        if (method != null) {
            return Try.success(method);
        }

        final List<Method> compatibleMethods = nameMethods.stream()
            .filter(m -> m.getSignature().isAssignableFrom(signature))
            .collect(Collectors.toList());

        if (compatibleMethods.size() == 1) {
            return Try.success(compatibleMethods.get(0));
        }
        else {
            return compatibleMethods.isEmpty() ? this.noSuchMethod(signature) : this.multipleMethods(signature);
        }
    }

    private Try<Method> noSuchMethod(MethodSignature signature) {
        final String msg = String.format("No compatible method with signature '%s' found.", signature);
        return Try.failure(new NoSuchMethodError(msg));
    }

    private Try<Method> multipleMethods(MethodSignature signature) {
        final String msg = String.format("Multiple compatible methods with signature '%s' found.", signature);
        return Try.failure(new NoSuchMethodError(msg));
    }

    private boolean containsMethod(Method method, List<Method> methods) {
        return methods.stream()
            .anyMatch(m -> m.getSignature().equals(method.getSignature()));
    }
}

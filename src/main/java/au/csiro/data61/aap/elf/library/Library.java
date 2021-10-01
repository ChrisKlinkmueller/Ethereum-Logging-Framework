package au.csiro.data61.aap.elf.library;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.library.plugins.Plugin;
import io.vavr.control.Try;

public class Library {
    private final Map<String, List<Method>> methods;
    private final Map<String, Plugin> plugins;

    public Library() {
        this.methods = new HashMap<>();
        this.plugins = new HashMap<>();
    }

    public void addMethod(Method method) {
        checkNotNull(method);

        final List<Method> nameMethods = this.methods.computeIfAbsent(method.getSignature().getName(), n -> new LinkedList<>());
        checkArgument(!this.containsMethod(method, nameMethods));

        nameMethods.add(method);
    }

    public void addPlugin(Plugin plugin) {
        checkNotNull(plugin);

        checkArgument(!this.plugins.containsKey(plugin.getName()));
        this.plugins.put(plugin.getName(), plugin);
    }

    public Try<Method> findMethod(MethodSignature signature) {
        checkNotNull(signature);

        final List<Method> nameMethods = this.methods.getOrDefault(signature.getName(), Collections.emptyList());
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

    public Try<Plugin> findPlugin(String pluginName) {
        checkNotNull(pluginName);
        final Plugin plugin = this.plugins.get(pluginName);
        return plugin == null ? this.noSuchPlugin(pluginName) : Try.success(plugin);
    }

    public Collection<Plugin> getPlugins() {
        return this.plugins.values();
    }

    private Try<Method> noSuchMethod(MethodSignature signature) {
        final String msg = String.format("No compatible method with signature '%s' found.", signature);
        return Try.failure(new UnsupportedOperationException(msg));
    }

    private Try<Plugin> noSuchPlugin(String pluginName) {
        final String msg = String.format("No plugin with name '%s' found.", pluginName);
        return Try.failure(new UnsupportedOperationException(msg));
    }

    private Try<Method> multipleMethods(MethodSignature signature) {
        final String msg = String.format("Multiple compatible methods with signature '%s' found.", signature);
        return Try.failure(new UnsupportedOperationException(msg));
    }

    private boolean containsMethod(Method method, List<Method> methods) {
        return methods.stream()
            .anyMatch(m -> m.getSignature().equals(method.getSignature()));
    }
}

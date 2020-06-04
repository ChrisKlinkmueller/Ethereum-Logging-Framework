package au.csiro.data61.aap.elf.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.core.Method;
import au.csiro.data61.aap.elf.library.Library;

/**
 * MethodSpecification
 */
public class MethodSpecification {
    private final Method method;

    private MethodSpecification(Method method) {
        this.method = method;
    }

    Method getMethod() {
        return this.method;
    }

    public static MethodSpecification of(String name, String... parameterTypes) throws BuildException {
        return of(name, Arrays.asList(parameterTypes));
    }

    public static MethodSpecification of(String name, List<String> parameterTypes) throws BuildException {
        final Method method = Library.INSTANCE.findMethod(name, parameterTypes);
        if (method == null) {
            final String message = String.format("%s(%s)", name, parameterTypes.stream().collect(Collectors.joining(",")));
            throw new BuildException(message);
        }
        return new MethodSpecification(method);
    }

}

package blf.configuration;

import blf.core.exceptions.ExceptionHandler;
import blf.core.interfaces.Method;
import blf.library.Library;

import java.util.Arrays;
import java.util.List;

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

    public static MethodSpecification of(Method method) {
        return new MethodSpecification(method);
    }

    public static MethodSpecification of(String name, String... parameterTypes) throws BuildException {
        return of(name, Arrays.asList(parameterTypes));
    }

    public static MethodSpecification of(String name, List<String> parameterTypes) {
        final Method method = Library.INSTANCE.findMethod(name, parameterTypes);
        final ExceptionHandler exceptionHandler = new ExceptionHandler();

        if (method == null) {
            final String errorMsg = String.format("%s(%s)", name, String.join(",", parameterTypes));
            exceptionHandler.handleException(errorMsg, new NullPointerException());

            return null;
        }

        return new MethodSpecification(method);
    }

}

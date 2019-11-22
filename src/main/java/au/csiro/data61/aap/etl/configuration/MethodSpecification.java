package au.csiro.data61.aap.etl.configuration;

import java.util.Arrays;
import java.util.List;

import au.csiro.data61.aap.etl.core.Method;
import au.csiro.data61.aap.etl.library.Library;
import au.csiro.data61.aap.etl.library.MethodSignature;

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
        final MethodSignature signature = new MethodSignature(name, parameterTypes);
        final Method method = Library.INSTANCE.getMethod(signature);
        if (method == null) {
            throw new BuildException("No such method: " + signature.getSignature());
        }
        return new MethodSpecification(method);
    }

}
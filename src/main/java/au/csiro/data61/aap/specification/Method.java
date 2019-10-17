package au.csiro.data61.aap.specification;

import java.util.function.Function;

/**
 * Method
 */
public class Method {
    private final MethodSignature signature;
    private final Function<Object[], Object> implementation;
    
    public Method(MethodSignature signature, Function<Object[], Object> implementation) {
        assert signature != null;
        assert implementation != null;
        this.signature = signature;
        this.implementation = implementation;
    }

    public MethodSignature getSignature() {
        return this.signature;
    }

    public Function<Object[], Object> getImplementation() {
        return this.implementation;
    }

    @Override
    public String toString() {
        return String.format("Method '%s'", this.signature.toString());
    }
}
package au.csiro.data61.aap.library;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import au.csiro.data61.aap.specification.MethodSignature;
import au.csiro.data61.aap.specification.ScopeType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * Method
 */
public class Method {
    private MethodSignature signature;
    private Function<Object[], MethodResult<Object>> implementation;
    private Set<ScopeType> visibility;
    
    public Method(MethodSignature signature, Function<Object[], MethodResult<Object>> implementation, ScopeType... visibility) {
        assert signature != null;
        assert implementation != null;
        assert visibility != null && Arrays.stream(visibility).allMatch(v -> v != null);
        this.signature = signature;
        this.implementation = implementation;
        this.visibility = Arrays.stream(visibility).collect(Collectors.toSet());
    }

    public MethodSignature getSignature() {
        return this.signature;
    }

    public Function<Object[], MethodResult<Object>> getImplementation() {
        return this.implementation;
    }

    public boolean isVisibleIn(ScopeType type) {
        assert type != null;
        return this.visibility.contains(type);
    }
    
}
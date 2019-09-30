package au.csiro.data61.aap.specification;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.csiro.data61.aap.library.types.SolidityType;

/**
 * Method
 */
public class MethodSignature {
    private final SolidityType<?> returnType;
    private final String name;
    private final SolidityType<?>[] parameterTypes;    

    public MethodSignature(String name, SolidityType<?> returnType, SolidityType<?>[] parameterTypes) {
        assert name != null && !name.trim().isEmpty();
        assert returnType != null;
        assert parameterTypes != null && Arrays.stream(parameterTypes).allMatch(t -> t != null);
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public String getName() {
        return this.name;
    }

    public SolidityType<?> getReturnType() {
        return this.returnType;
    }

    public int parameterTypeCount() {
        return this.parameterTypes.length;
    }

    public SolidityType<?> getParameterType(int index) {
        assert 0 <= index && index < this.parameterTypes.length;
        return this.parameterTypes[index];
    }

    public Stream<SolidityType<?>> paramaterTypeStream() {
        return Arrays.stream(this.parameterTypes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MethodSignature)) {
            return false;
        }

        final MethodSignature method = (MethodSignature)obj;
        if (!method.name.equals(this.name) || 
             method.parameterTypeCount() != this.parameterTypeCount() ||
            !method.returnType.equals(this.returnType)) {
            return false;
        }

        for (int i = 0; i < this.parameterTypeCount(); i++) {
            if (!this.getParameterType(i).equals(method.getParameterType(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 41;
        int hash = 43;
        hash += prime * hash + this.name.hashCode();
        hash += prime * hash + this.returnType.hashCode();
        for (SolidityType<?> param : this.parameterTypes) {
            hash += prime * hash + param.hashCode();
        }

        return super.hashCode();
    }

    @Override
    public String toString() {
        final String parameterList = Arrays.stream(this.parameterTypes).map(param -> param.getTypeName()).collect(Collectors.joining(", "));
        return String.format("%s %s(%s))", this.returnType, this.name, parameterList);
    }
}
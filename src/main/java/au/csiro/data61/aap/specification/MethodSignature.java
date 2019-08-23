package au.csiro.data61.aap.specification;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Method
 */
public class MethodSignature {
    private final String returnType;
    private final String name;
    private final String[] parameterTypes;    

    public MethodSignature(String name, String returnType, String[] parameterTypes) {
        assert name != null && !name.trim().isEmpty();
        assert returnType != null && !returnType.trim().isEmpty();
        assert parameterTypes != null && Arrays.stream(parameterTypes).allMatch(t -> t != null);
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public String getName() {
        return this.name;
    }

    public String getReturnType() {
        return this.returnType;
    }

    public int parameterTypeCount() {
        return this.parameterTypes.length;
    }

    public String getParameterType(int index) {
        assert 0 <= index && index < this.parameterTypes.length;
        return this.parameterTypes[index];
    }

    public Stream<String> paramaterTypeStream() {
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
        for (String param : this.parameterTypes) {
            hash += prime * hash + param.hashCode();
        }

        return super.hashCode();
    }
}
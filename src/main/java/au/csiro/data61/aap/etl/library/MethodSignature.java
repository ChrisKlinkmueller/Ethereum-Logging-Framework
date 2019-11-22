package au.csiro.data61.aap.etl.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.web3j.abi.TypeReference;

/**
 * MethodSignature
 */
public class MethodSignature {
    private final String methodName;
    private final List<String> parameterTypes;
    
    public MethodSignature(String methodName, String... parameterTypes) {
        this(methodName, Arrays.asList(parameterTypes));
    }

    public MethodSignature(String methodName, List<String> parameterTypes) {
        assert methodName != null;
        assert parameterTypes != null && parameterTypes.stream().allMatch(this::isValidType);
        this.methodName = methodName;
        this.parameterTypes = new ArrayList<String>(parameterTypes);
    }
    
    private boolean isValidType(String type) {
        if (type == null) {
            return false;
        }

        try {
            return TypeReference.makeTypeReference(type) != null;
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public String getMethodName() {
        return this.methodName;
    }

    public Stream<String> parameterTypeStream() {
        return this.parameterTypes.stream();
    }

    public int parameterTypeCount() {
        return this.parameterTypes.size();
    }

    public String getParameterType(int index) {
        assert 0 <= index && index <= this.parameterTypeCount();
        return this.parameterTypes.get(index);
    }
    
    public String getSignature() {
        return String.format("%s(%s)", this.methodName, this.parameterTypeStream().collect(Collectors.joining(",")));
    }

    public boolean isCompatibleWith(MethodSignature signature) {
        if (signature == null || !signature.getMethodName().equals(this.getMethodName())) {
            return false;
        }
        
        if (signature.parameterTypeCount() != this.parameterTypeCount()) {
            return false;
        }

        for (int i = 0; i < signature.parameterTypeCount(); i++) {
            if (!areCompatible(signature.getParameterType(i), this.getParameterType(i))) {
                return false;
            }
        }
        return true;
    }

    private static final String ARRAY_PATTERN = "[a-zA-Z0-9\\[\\]]+\\[\\]";
    private static boolean areCompatible(String type, String expectedType) {
        if (type.equals(expectedType)) {
            return true;
        }

        if (type.matches(ARRAY_PATTERN) && expectedType.matches(ARRAY_PATTERN)) {
            return areCompatible(type.substring(0, type.length() - 2), expectedType.substring(0, expectedType.length() - 2));
        }

        return (type.contains("int") && expectedType.contains("int"))
               || (type.contains("fixed") && expectedType.contains("fixed"))
               || (type.contains("byte") && expectedType.contains("byte"));
    }
}
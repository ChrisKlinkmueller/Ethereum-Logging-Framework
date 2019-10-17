package au.csiro.data61.aap.specification;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import au.csiro.data61.aap.specification.types.SolidityType;

/**
 * MethodCall
 */
public class MethodCall implements ValueSource {
    private Method method;
    private ValueSource[] inputParameters;

    public MethodCall(Method method, ValueSource... inputParameters) {
        assert method != null;
        assert Arrays.stream(inputParameters).allMatch(Objects::nonNull);
        assert method.getSignature().parameterTypeCount() == inputParameters.length;
        // TODO: verify if the following assertion can be relaxed by requiring type compatibility instead of type equality
        assert IntStream
            .range(0, inputParameters.length)
            .allMatch(i -> inputParameters[i].getType().getClass().equals(method.getSignature().getParameterType(i).getClass()));

        this.method = method;
        this.inputParameters = Arrays.copyOf(inputParameters, inputParameters.length);
    }    

    public int inputParameterCount() {
        return this.inputParameters.length;
    }

    public ValueSource getInputParameter(int index) {
        assert 0 <= index && index <= this.inputParameterCount();
        return this.inputParameters[index];
    }

    public Stream<ValueSource> inputParameterStream() {
        return Arrays.stream(this.inputParameters);
    }

    public Method getMethod() {
        return this.method;
    }

    @Override
    public Object getValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SolidityType<?> getType() {
        return this.method.getSignature().getReturnType();
    }

}
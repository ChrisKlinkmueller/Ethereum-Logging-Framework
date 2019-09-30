package au.csiro.data61.aap.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import au.csiro.data61.aap.util.MethodResult;

/**
 * SpecificationParserResult
 */
public class SpecificationParserResult<T> {
    private final T result;
    private final SpecificationParserError[] errors;

    private SpecificationParserResult(T result, SpecificationParserError[] errors) {
        assert result == null ? errors != null && 0 <= errors.length && Arrays.stream(errors).allMatch(error -> error != null) : errors == null;
        this.result = result;
        this.errors = errors == null ? new SpecificationParserError[0] : errors;
    }

    public boolean isSuccessful() {
        return this.result != null;
    }

    public int errorCount() {
        return this.errors.length;
    }

    public SpecificationParserError getError(int index) {
        assert 0 <= index && index < this.errorCount();
        return this.errors[index];
    }

    public Stream<SpecificationParserError> errorStream() {
        return Arrays.stream(this.errors);
    }
    
    static <T> SpecificationParserResult<T> ofSingleError(String message) {
        return ofSingleError(message, null);
    }

    static <T> SpecificationParserResult<T> ofSingleError(String message, Throwable cause) {
        assert message != null && !message.trim().isEmpty();
        final SpecificationParserError[] errors = new SpecificationParserError[1];
        errors[0] = new SpecificationParserError(0, 0, message, cause);
        return new SpecificationParserResult<T>(null, errors);
    }

    static <T> SpecificationParserResult<T> ofErrors(Collection<SpecificationParserError> errors) {
        assert errors != null && errors.stream().allMatch(error -> error != null);
        return new SpecificationParserResult<T>(null, errors.toArray(new SpecificationParserError[0]));
    }

    static <T> SpecificationParserResult<T> ofUnsuccessfulMethodResult(MethodResult<?> result) {
        assert result != null && !result.isSuccessful();
        return ofSingleError(result.getErrorMessage(), result.getErrorCause());
    }

    static <T> SpecificationParserResult<T> ofErrorReporter(AntlrErrorReporter reporter) {
        assert reporter != null && reporter.hasErrors();
        return new SpecificationParserResult<T>(null, reporter.errorStream().toArray(SpecificationParserError[]::new));
    } 

    static <T> SpecificationParserResult<T> ofGlobalScope(T result) {
        assert result != null;
        return new SpecificationParserResult<T>(result, null);
    }
}
package au.csiro.data61.aap.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.Token;

import au.csiro.data61.aap.util.MethodResult;

/**
 * SpecificationParserResult
 */
public class SpecificationParserResult<T> {
    private static final String ERROR_MESSAGE_JOIN = ", ";
    
    private final T result;
    private final SpecificationParserError[] errors;

    private SpecificationParserResult(T result, SpecificationParserError[] errors) {
        assert result == null ? errors != null && 0 <= errors.length && Arrays.stream(errors).allMatch(error -> error != null) : errors == null;
        this.result = result;
        this.errors = errors == null ? new SpecificationParserError[0] : Arrays.copyOf(errors, errors.length);
    }

    public boolean isSuccessful() {
        return this.result != null;
    }

    public T getResult() {
        return this.result;
    }

    public int errorCount() {
        return this.errors.length;
    }

    public String getErrorMessage() {
        return this.errorStream()
                   .map(error -> error.getErrorMessage())
                   .collect(Collectors.joining(ERROR_MESSAGE_JOIN));
    }

    public SpecificationParserError getError(int index) {
        assert 0 <= index && index < this.errorCount();
        return this.errors[index];
    }

    public Stream<SpecificationParserError> errorStream() {
        return Arrays.stream(this.errors);
    }
    
    static <T> SpecificationParserResult<T> ofError(String message) {
        return ofError(message, null);
    }

    static <T> SpecificationParserResult<T> ofError(String message, Throwable cause) {
        assert message != null && !message.trim().isEmpty();
        final SpecificationParserError[] errors = new SpecificationParserError[1];
        errors[0] = new SpecificationParserError(0, 0, message, cause);
        return new SpecificationParserResult<T>(null, errors);
    }    

    static <T> SpecificationParserResult<T> ofError(Token token, String message) {
        assert token != null;
        assert message != null && !message.trim().isEmpty();
        final SpecificationParserError[] errors = new SpecificationParserError[1];
        errors[0] = new SpecificationParserError(token, message);
        return new SpecificationParserResult<T>(null, errors);
    }

    static <T> SpecificationParserResult<T> ofErrors(Collection<SpecificationParserError> errors) {
        assert errors != null && errors.stream().allMatch(error -> error != null);
        return new SpecificationParserResult<T>(null, errors.toArray(new SpecificationParserError[0]));
    }

    static <T> SpecificationParserResult<T> ofErrors(SpecificationParserError... errors) {
        return ofErrors(Arrays.asList(errors));
    }

    static <T> SpecificationParserResult<T> ofUnsuccessfulParserResult(SpecificationParserResult<?> result) {
        assert result != null;
        return new SpecificationParserResult<T>(null, result.errors);
    }

    static <T> SpecificationParserResult<T> ofUnsuccessfulMethodResult(MethodResult<?> result) {
        assert result != null && !result.isSuccessful();
        return ofError(result.getErrorMessage(), result.getErrorCause());
    }

    static <T> SpecificationParserResult<T> ofErrorReporter(AntlrErrorReporter reporter) {
        assert reporter != null && reporter.hasErrors();
        return new SpecificationParserResult<T>(null, reporter.errorStream().toArray(SpecificationParserError[]::new));
    } 

    static <T> SpecificationParserResult<T> ofResult(T result) {
        assert result != null;
        return new SpecificationParserResult<T>(result, null);
    }
}
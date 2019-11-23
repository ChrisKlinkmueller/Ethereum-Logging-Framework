package au.csiro.data61.aap.etl.parsing;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.csiro.data61.aap.etl.MethodResult;

/**
 * SpecificationParserResult
 */
public class SpecificationParserResult<T> {
    private static final String ERROR_MESSAGE_JOIN = ", ";
    
    private final T result;
    private final SpecificationParserError[] errors;

    private SpecificationParserResult(T result, SpecificationParserError[] errors) {
        assert errors == null ? true : 0 <= errors.length && Arrays.stream(errors).allMatch(error -> error != null);
        this.result = result;
        this.errors = errors == null ? new SpecificationParserError[0] : Arrays.copyOf(errors, errors.length);
    }

    public boolean isSuccessful() {
        return this.errors.length == 0;
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

    static <T> SpecificationParserResult<T> ofUnsuccessfulMethodResult(MethodResult<?> result) {
        assert result != null && !result.isSuccessful();
        return ofError(result.getErrorMessage(), result.getErrorCause());
    } 

    static <T> SpecificationParserResult<T> ofError(String message, Throwable cause) {
        assert message != null && !message.trim().isEmpty();
        final SpecificationParserError[] errors = new SpecificationParserError[1];
        errors[0] = new SpecificationParserError(0, 0, message, cause);
        return new SpecificationParserResult<T>(null, errors);
    }   

    /*static <T> SpecificationParserResult<T> ofError(Token token, String message) {
        return ofError(token, message, null);
    }

    static <T> SpecificationParserResult<T> ofError(Token token, String message, Throwable cause) {
        assert token != null;
        assert message != null && !message.trim().isEmpty();
        final SpecificationParserError[] errors = new SpecificationParserError[1];
        errors[0] = new SpecificationParserError(token, message, cause);
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
        assert result != null && !result.isSuccessful();
        return new SpecificationParserResult<T>(null, result.errors);
    }

	static <T> SpecificationParserResult<SolidityType> ofUnsuccessfulParserResult(Token token, SpecificationParserResult<SolidityType> result) {
        assert result != null && !result.isSuccessful();
        assert token != null;
        
        return new SpecificationParserResult<SolidityType>(
            null, 
            result.errorStream().map(e -> new SpecificationParserError(token, e.getErrorMessage(), e.getErrorCause())).toArray(SpecificationParserError[]::new)
        );
    }*/
    
    static <T> SpecificationParserResult<T> ofErrors(Stream<SpecificationParserError> errorStream) {
        assert errorStream != null;

        final SpecificationParserError[] errors = errorStream.toArray(SpecificationParserError[]::new);
        assert 0 < errors.length;

        return new SpecificationParserResult<T>(null, errors);
    }

    /*static <T> SpecificationParserResult<T> ofErrorReporter(AntlrErrorReporter reporter) {
        assert reporter != null && reporter.hasErrors();
        return new SpecificationParserResult<T>(null, reporter.errorStream().toArray(SpecificationParserError[]::new));
    } */

    static <T> SpecificationParserResult<T> ofResult(T result) {
        return new SpecificationParserResult<T>(result, null);
    }
}
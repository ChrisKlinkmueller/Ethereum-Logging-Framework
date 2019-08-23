package au.csiro.data61.aap.parser;

import java.util.Arrays;
import java.util.stream.Stream;

import au.csiro.data61.aap.specification.Specification;
import au.csiro.data61.aap.util.MethodResult;

/**
 * SpecificationParserResult
 */
public class SpecificationParserResult {
    private final Specification specification;
    private final SpecificationParserError[] errors;

    public SpecificationParserResult(Specification specification) {
        assert specification != null;
        this.specification = specification;
        this.errors = null;
    }

    public SpecificationParserResult(SpecificationParserError[] errors) {
        assert errors != null && 0 <= errors.length && Arrays.stream(errors).allMatch(error -> error != null);
        this.specification = null;
        this.errors = errors;
    }

    public boolean isSuccessful() {
        return this.specification != null;
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
    
    static SpecificationParserResult ofSingleError(String message) {
        return ofSingleError(message, null);
    }

    static SpecificationParserResult ofSingleError(String message, Throwable cause) {
        assert message != null && !message.trim().isEmpty();
        final SpecificationParserError[] errors = new SpecificationParserError[1];
        errors[0] = new SpecificationParserError(0, 0, message, cause);
        return new SpecificationParserResult(errors);
    }

    static <T> SpecificationParserResult ofUnsuccessfulMethodResult(MethodResult<T> result) {
        assert result != null && !result.isSuccessful();
        return ofSingleError(result.getErrorMessage(), result.getErrorCause());
    }

    static SpecificationParserResult ofErrorReporter(AntlrErrorReporter reporter) {
        assert reporter != null && reporter.hasErrors();
        return new SpecificationParserResult(reporter.errorStream().toArray(SpecificationParserError[]::new));
    } 
}
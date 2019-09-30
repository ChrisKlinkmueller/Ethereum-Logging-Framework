package au.csiro.data61.aap.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import au.csiro.data61.aap.specification.Scope;
import au.csiro.data61.aap.specification.ScopeType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * SpecificationParserResult
 */
public class SpecificationParserResult {
    private final Scope globalScope;
    private final SpecificationParserError[] errors;

    private SpecificationParserResult(Scope globalScope, SpecificationParserError[] errors) {
        assert globalScope != null ? errors == null : errors != null && 0 <= errors.length && Arrays.stream(errors).allMatch(error -> error != null);
        this.globalScope = globalScope;
        this.errors = errors == null ? new SpecificationParserError[0] : errors;
    }

    public boolean isSuccessful() {
        return this.globalScope != null;
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
        return new SpecificationParserResult(null, errors);
    }

    static SpecificationParserResult ofErrors(Collection<SpecificationParserError> errors) {
        assert errors != null && errors.stream().allMatch(error -> error != null);
        return new SpecificationParserResult(null, errors.toArray(new SpecificationParserError[0]));
    }

    static <T> SpecificationParserResult ofUnsuccessfulMethodResult(MethodResult<T> result) {
        assert result != null && !result.isSuccessful();
        return ofSingleError(result.getErrorMessage(), result.getErrorCause());
    }

    static SpecificationParserResult ofErrorReporter(AntlrErrorReporter reporter) {
        assert reporter != null && reporter.hasErrors();
        return new SpecificationParserResult(null, reporter.errorStream().toArray(SpecificationParserError[]::new));
    } 

    static SpecificationParserResult ofGlobalScope(Scope globalScope) {
        assert globalScope != null && globalScope.getDefinition().getType() == ScopeType.GLOBAL_SCOPE;
        return new SpecificationParserResult(globalScope, null);
    }
}
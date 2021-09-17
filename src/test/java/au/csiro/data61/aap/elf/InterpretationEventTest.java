package au.csiro.data61.aap.elf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;

import org.antlr.v4.runtime.Token;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import au.csiro.data61.aap.elf.InterpretationEvent.Type;

class InterpretationEventTest {
    
    @ParameterizedTest
    @MethodSource
    void type_toStringReturnsCorrectResult(Type type, String expectedString) {
        assertEquals(expectedString, type.toString());
    }

    private static Stream<Arguments> type_toStringReturnsCorrectResult() {
        return Stream.of(
            Arguments.of(Type.ERROR, InterpretationEvent.ERROR_STRING),
            Arguments.of(Type.INFO, InterpretationEvent.INFO_STRING),
            Arguments.of(Type.WARNING, InterpretationEvent.WARNING_STRING)
        );
    }

    @ParameterizedTest
    @MethodSource("constructorTestCases")
    void testTypeMessageConstructor(Type type, Token token, String message, Throwable cause) {
        final InterpretationEvent event = new InterpretationEvent(type, message);
        this.verifyEvent(event, type, InterpretationEvent.DEFAULT_LINE, InterpretationEvent.DEFAULT_COLUMN, message, null);
    }

    @ParameterizedTest
    @MethodSource("constructorTestCases")
    void testTypeMessageCauseConstructor(Type type, Token token, String message, Throwable cause) {
        final InterpretationEvent event = new InterpretationEvent(type, message, cause);
        this.verifyEvent(event, type, InterpretationEvent.DEFAULT_LINE, InterpretationEvent.DEFAULT_COLUMN, message, cause);
    }

    @ParameterizedTest
    @MethodSource("constructorTestCases")
    void testTypeTokenMessageConstructor(Type type, Token token, String message, Throwable cause) {
        final InterpretationEvent event = new InterpretationEvent(type, token, message);
        this.verifyEvent(event, type, token.getLine(), token.getCharPositionInLine(), message, null);
    }

    @ParameterizedTest
    @MethodSource("constructorTestCases")
    void testTypeTokenMessageCauseConstructor(Type type, Token token, String message, Throwable cause) {
        final InterpretationEvent event = new InterpretationEvent(type, token, message, cause);
        this.verifyEvent(event, type, token.getLine(), token.getCharPositionInLine(), message, cause);
    }

    @ParameterizedTest
    @MethodSource("constructorTestCases")
    void testTypePosMessageConstructor(Type type, Token token, String message, Throwable cause) {
        final InterpretationEvent event = new InterpretationEvent(type, token.getLine(), token.getCharPositionInLine(), message);
        this.verifyEvent(event, type, token.getLine(), token.getCharPositionInLine(), message, null);
    }

    @ParameterizedTest
    @MethodSource("constructorTestCases")
    void testTypePosMessageCauseConstructor(Type type, Token token, String message, Throwable cause) {
        final InterpretationEvent event = new InterpretationEvent(type, token.getLine(), token.getCharPositionInLine(), message, cause);
        this.verifyEvent(event, type, token.getLine(), token.getCharPositionInLine(), message, cause);
    }

    private void verifyEvent(InterpretationEvent event, Type type, int line, int col, String message, Throwable cause) {
        assertEquals(type, event.getType());
        assertEquals(line, event.getLine());
        assertEquals(col, event.getColumn());
        assertEquals(message, event.getMessage());

        if (cause == null) {
            assertNull(event.getCause());
        }
        else {
            assertEquals(cause, event.getCause());
        }
    }

    private static Stream<Arguments> constructorTestCases() {
        return Stream.of(
            Arguments.of(Type.ERROR, mock(Token.class), "An error that occurred during parsing.", new IllegalArgumentException()),
            Arguments.of(Type.INFO, mock(Token.class), "An info regarding the analysis.", new UnsupportedOperationException()),
            Arguments.of(Type.WARNING, mock(Token.class), "A warning that occurred during parsing.", new NullPointerException())
        );
    }

}

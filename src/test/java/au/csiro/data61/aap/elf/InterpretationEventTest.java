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

    static Stream<Arguments> type_toStringReturnsCorrectResult() {
        return Stream.of(
            Arguments.of(Type.ERROR, InterpretationEvent.ERROR_STRING),
            Arguments.of(Type.INFO, InterpretationEvent.INFO_STRING),
            Arguments.of(Type.WARNING, InterpretationEvent.WARNING_STRING)
        );
    }

    @ParameterizedTest
    @MethodSource
    void constructorsIntializeCorrectly(Type type, Token token, String message, Throwable cause) {
        InterpretationEvent event = new InterpretationEvent(type, message);
        assertEquals(type, event.getType());
        assertEquals(InterpretationEvent.DEFAULT_LINE, event.getLine());
        assertEquals(InterpretationEvent.DEFAULT_COLUMN, event.getColumn());
        assertEquals(message, event.getMessage());
        assertNull(event.getCause());

        event = new InterpretationEvent(type, message, cause);
        assertEquals(type, event.getType());
        assertEquals(InterpretationEvent.DEFAULT_LINE, event.getLine());
        assertEquals(InterpretationEvent.DEFAULT_COLUMN, event.getColumn());
        assertEquals(message, event.getMessage());
        assertEquals(cause, event.getCause());

        event = new InterpretationEvent(type, token, message);
        assertEquals(type, event.getType());
        assertEquals(token.getLine(), event.getLine());
        assertEquals(token.getCharPositionInLine(), event.getColumn());
        assertEquals(message, event.getMessage());
        assertNull(event.getCause());

        event = new InterpretationEvent(type, token, message, cause);
        assertEquals(type, event.getType());
        assertEquals(token.getLine(), event.getLine());
        assertEquals(token.getCharPositionInLine(), event.getColumn());
        assertEquals(message, event.getMessage());
        assertEquals(cause, event.getCause());

        event = new InterpretationEvent(type, token.getLine(), token.getCharPositionInLine(), message);
        assertEquals(type, event.getType());
        assertEquals(token.getLine(), event.getLine());
        assertEquals(token.getCharPositionInLine(), event.getColumn());
        assertEquals(message, event.getMessage());
        assertNull(event.getCause());

        event = new InterpretationEvent(type, token.getLine(), token.getCharPositionInLine(), message, cause);
        assertEquals(type, event.getType());
        assertEquals(token.getLine(), event.getLine());
        assertEquals(token.getCharPositionInLine(), event.getColumn());
        assertEquals(message, event.getMessage());
        assertEquals(cause, event.getCause());
    }

    static Stream<Arguments> constructorsIntializeCorrectly() {
        return Stream.of(
            Arguments.of(Type.ERROR, mock(Token.class), "An error that occurred during parsing.", new IllegalArgumentException()),
            Arguments.of(Type.INFO, mock(Token.class), "An info regarding the analysis.", new UnsupportedOperationException()),
            Arguments.of(Type.WARNING, mock(Token.class), "A warning that occurred during parsing.", new NullPointerException())
        );
    }

}

package au.csiro.data61.aap.elf.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.antlr.v4.runtime.Token;

/**
 * ErrorItem
 */
public class ErrorItem extends GeneratorItem {
    private List<String> messages;

    ErrorItem(Token token, String specification) {
        super(token, specification);
        this.messages = new ArrayList<>();
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public Stream<String> messageStream() {
        return this.messages.stream();
    }
}

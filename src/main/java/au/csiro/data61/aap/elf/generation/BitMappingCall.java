package au.csiro.data61.aap.elf.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BitMappingCall
 */
class BitMappingCall {
    private final String variableName;
    private final int from;
    private final int to;
    private final List<Object> values;

    BitMappingCall(String variableName, int from, int to, List<Object> values) {
        this.variableName = variableName;
        this.from = from;
        this.to = to;
        this.values = new ArrayList<>();
    } 

    public int getFrom() {
        return this.from;
    }

    public int getTo() {
        return this.to;
    }

    public List<Object> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public String getVariableName() {
        return this.variableName;
    }
}
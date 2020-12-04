package au.csiro.data61.aap.elf.generation;

import java.math.BigInteger;
import java.util.List;

import org.antlr.v4.runtime.Token;

/**
 * BitMappingItem
 */
class BitMappingItem<T> extends GeneratorItem {
    private BigInteger from;
    private BigInteger to;
    private List<T> values;
    private String encodedAttribute;
    private String targetVariable;

    BitMappingItem(Token token, String specification) {
        super(token, specification);
    }

    public BigInteger getFrom() {
        return this.from;
    }

    public void setFrom(BigInteger from) {
        if (from == null) {
            throw new IllegalArgumentException("BitMappingItem.setFrom: null not allowed!");
        }
        this.from = from;
    }

    public BigInteger getTo() {
        return this.to;
    }

    public void setTo(BigInteger to) {
        if (to == null) {
            throw new IllegalArgumentException("BitMappingItem.setTo: null not allowed!");
        }
        this.to = to;
    }

    public List<T> getValues() {
        return this.values;
    }

    public void setValues(List<T> values) {
        if (values == null) {
            throw new IllegalArgumentException("BitMappingItem.setValues: null not allowed!");
        }
        this.values = values;
    }

    public String getEncodedAttribute() {
        return this.encodedAttribute;
    }

    public void setEncodedAttribute(String encodedAttribute) {
        this.encodedAttribute = encodedAttribute;
    }

    public String getTargetVariable() {
        return this.targetVariable == null ? "unnamed_variable" : this.targetVariable;
    }

    public void setTargetVariable(String targetVariable) {
        this.targetVariable = targetVariable;
    }
}

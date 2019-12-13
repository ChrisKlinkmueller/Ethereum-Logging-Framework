package au.csiro.data61.aap.elf.generation;

import java.math.BigInteger;
import java.util.List;

import org.antlr.v4.runtime.Token;

/**
 * BitMapping
 */
class BitMappingItem extends GeneratorItem {
    private BigInteger from;
    private BigInteger to;
    private List<?> values;
    private String encodedAttribute;
    private String targetVariable;

    BitMappingItem(Token token, String specification) {
        super(token, specification);
    }

    public BigInteger getFrom() {
        return this.from;
    }

    public void setFrom(BigInteger from) {
        assert from != null;
        this.from = from;
    }

    public BigInteger getTo() {
        return this.to;
    }

    public void setTo(BigInteger to) {
        assert to != null;
        this.to = to;
    }

    public List<?> getValues() {
        return this.values;
    }

    public void setValues(List<?> values) {
        assert values != null;
        this.values = values;
    }

    public String getEncodedAttribute() {
        return this.encodedAttribute;
    }

    public void setEncodedAttribute(String encodedAttribute) {
        this.encodedAttribute = encodedAttribute;
    }

    public String getTargetVariable() {
        return this.targetVariable;
    }

    public void setTargetVariable(String targetVariable) {
        this.targetVariable = targetVariable;
    }
}
package au.csiro.data61.aap.elf.generation;

import java.util.List;

import org.antlr.v4.runtime.Token;

/**
 * ValueDictionaryItem
 */
class ValueDictionaryItem extends GeneratorItem {
    private final String targetVariable;
    private final String encodedAttribute;
    private final Object defaultValue;
    private final List<?> fromValues;
    private final List<?> toValues;

    ValueDictionaryItem(
        Token token, 
        String specification, 
        String targetVariable,
        String encodedAttribute,
        Object defaultValue,
        List<?> fromValues,
        List<?> toValues    
    ) {
        super(token, specification);
        this.fromValues = fromValues;
        this.toValues = toValues;
        this.targetVariable = targetVariable;
        this.encodedAttribute = encodedAttribute;
        this.defaultValue = defaultValue;
    }

    List<?> getFromValues() {
        return this.fromValues;
    }

    String getTargetVariable() {
        return this.targetVariable;
    }

    List<?> getToValues() {
        return this.toValues;
    }

    Object getDefaultValue() {
        return this.defaultValue;
    }

    String getEncodedAttribute() {
        return this.encodedAttribute;
    }
}
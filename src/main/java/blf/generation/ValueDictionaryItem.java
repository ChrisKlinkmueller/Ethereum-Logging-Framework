package blf.generation;

import java.util.List;

import org.antlr.v4.runtime.Token;

/**
 * ValueDictionaryItem
 */
class ValueDictionaryItem<S, T> extends GeneratorItem {
    private String targetVariable;
    private String targetType;
    private String encodedAttribute;
    private String encodedType;
    private Object defaultValue;
    private List<S> fromValues;
    private List<T> toValues;

    ValueDictionaryItem(Token token, String specification) {
        super(token, specification);
    }

    List<S> getFromValues() {
        return this.fromValues;
    }

    void setFromValues(List<S> fromValues) {
        this.fromValues = fromValues;
    }

    String getTargetVariable() {
        return this.targetVariable == null ? "unnamed_variable" : this.targetVariable;
    }

    void setTargetVariable(String targetVariable) {
        this.targetVariable = targetVariable;
    }

    String getTargetType() {
        return this.targetType;
    }

    void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    String getEncodedAttribute() {
        return this.encodedAttribute;
    }

    void setEncodedAttribute(String encodedAttribute) {
        this.encodedAttribute = encodedAttribute;
    }

    String getEncodedType() {
        return this.encodedType;
    }

    void setEncodedType(String encodedType) {
        this.encodedType = encodedType;
    }

    List<T> getToValues() {
        return this.toValues;
    }

    void setToValues(List<T> toValues) {
        this.toValues = toValues;
    }

    Object getDefaultValue() {
        return this.defaultValue;
    }

    void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}

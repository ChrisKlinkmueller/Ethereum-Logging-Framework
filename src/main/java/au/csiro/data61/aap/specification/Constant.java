package au.csiro.data61.aap.specification;

import java.util.Objects;

import au.csiro.data61.aap.specification.types.SolidityType;

/**
 * Constant
 */
public class Constant extends ValueContainer {

    public Constant(SolidityType<?> type, String name, Object value) {
        super(type, name);
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("const %s %s", this.getType(), this.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Constant)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        final Variable v = (Variable)obj;
        return v.getName().equals(this.getName()) && v.getType().equals(this.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getType(), this.getName(), Constant.class);
    }

    
}
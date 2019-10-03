package au.csiro.data61.aap.specification;

import java.util.Objects;

/**
 * Variable
 */
public class Variable {
    private final String type;
    private final String name;

    public Variable(String type, String name) {
        assert type != null;
        assert name != null;
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return String.format("%s %s", this.type, this.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Variable)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        final Variable v = (Variable)obj;
        return v.name.equals(this.name) && v.type.equals(this.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }
    
}
package au.csiro.data61.aap.elf.types;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

public final class ListType implements Type {
    private final Type baseType;

    public ListType(Type baseType) {
        checkNotNull(baseType);
        this.baseType = baseType;
    }

    public Type getBaseType() {
        return this.baseType;
    }

    @Override
    public String getName() {
        return String.format("%s[]", this.baseType.getName());
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        checkNotNull(type);
        return type instanceof ListType
            && this.baseType.isAssignableFrom(((ListType)type).baseType);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ListType && ((ListType)o).baseType.equals(this.baseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClass(), this.baseType);
    }

    @Override
    public String toString() {
        return this.getName();
    }

}

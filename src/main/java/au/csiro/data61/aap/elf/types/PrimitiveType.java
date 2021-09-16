package au.csiro.data61.aap.elf.types;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

public abstract class PrimitiveType implements Type {
    private final String name;

    protected PrimitiveType(String name) {
        checkNotNull(name);
        checkArgument(!name.isBlank());
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        checkNotNull(type);
        return this.getClass().equals(type.getClass());
    }

    @Override
    public boolean equals(Object o) {
        return this.getClass().isInstance(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClass(), this.name);
    }

    @Override
    public String toString() {
        return this.name;
    }

}

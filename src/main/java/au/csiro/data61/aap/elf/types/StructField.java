package au.csiro.data61.aap.elf.types;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.Map.Entry;

public final class StructField {
    private final String name;
    private final Type type;

    public StructField(String name, Type type) {
        checkNotNull(name);
        checkArgument(!name.isBlank());
        checkNotNull(type);
        this.name = name;
        this.type = type;
    }

    StructField(Entry<String, Type> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public Type getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.name, this.type);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StructField)) {
            return false;
        }

        final StructField field = (StructField)o;
        return field.name.equals(this.name) && field.type.equals(this.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClass(), this.name, this.type);
    }
}

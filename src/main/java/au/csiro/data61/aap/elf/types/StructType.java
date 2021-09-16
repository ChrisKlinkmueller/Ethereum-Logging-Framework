package au.csiro.data61.aap.elf.types;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StructType implements Type {
    private static final String NAME = "struct";
    private final Map<String, Type> fields;

    public StructType(Map<String, Type> fields) {
        checkNotNull(fields);
        this.fields = new HashMap<>();
        this.addFields(this.getFields(fields));
    }

    public StructType(StructField... fields) {
        this.fields = new HashMap<>();
        this.addFields(List.of(fields));
    }

    public StructType(List<StructField> fields) {
        checkNotNull(fields);
        this.fields = new HashMap<>();
        this.addFields(fields);
    }

    private void addFields(List<StructField> fields) {
        fields.forEach(this::addField);
    }

    private void addField(StructField field) {
        checkNotNull(field);
        checkArgument(!this.fields.containsKey(field.getName()));
        this.fields.put(field.getName(), field.getType());
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Stream<StructField> fieldStream() {
        return this.fieldStream(this.fields);
    }

    public List<StructField> getFields() {
        return this.getFields(this.fields);
    }

    private Stream<StructField> fieldStream(Map<String, Type> fields) {
        return fields.entrySet().stream()
            .map(StructField::new);
    }

    private List<StructField> getFields(Map<String, Type> fields) {
        return this.fieldStream(fields).collect(Collectors.toList());
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        checkNotNull(type);
        if (!(type instanceof StructType)) {
            return false;
        }

        final Predicate<Entry<String, Type>> assignable = assignabilityPredicate(type);
        return this.fields.entrySet().stream().allMatch(assignable);
    }

    private Predicate<Entry<String, Type>> assignabilityPredicate(Type type) {
        final StructType struct = (StructType)type;
        return e -> {
            final Type superType = e.getValue();
            final Type subType = struct.fields.get(e.getKey());
            return subType != null && superType.isAssignableFrom(subType);
        };
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StructType)) {
            return false;
        }

        final StructType type = (StructType)o;
        for (Entry<String, Type> fieldEntry : this.fields.entrySet()) {
            if (!fieldEntry.getValue().equals(type.fields.get(fieldEntry.getKey()))) {
                return false;
            }
        }

        return type.fields.keySet().stream().allMatch(this.fields::containsKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getClass(), this.fields.hashCode());
    }
    
}

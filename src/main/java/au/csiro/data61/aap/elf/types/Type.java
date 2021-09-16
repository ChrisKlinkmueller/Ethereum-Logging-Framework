package au.csiro.data61.aap.elf.types;

public interface Type {
    String getName();
    boolean isAssignableFrom(Type type);
}

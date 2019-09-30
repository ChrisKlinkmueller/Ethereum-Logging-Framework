package au.csiro.data61.aap.specification;

import au.csiro.data61.aap.library.types.SolidityType;

public interface ValueSource {
    public SolidityType<?> getReturnType();
}
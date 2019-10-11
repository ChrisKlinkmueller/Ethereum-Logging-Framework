package au.csiro.data61.aap.specification;

import au.csiro.data61.aap.specification.types.SolidityType;

/**
 * ValueSource
 */
public interface ValueSource {
    public Object getValue();
    public SolidityType<?> getType();
}
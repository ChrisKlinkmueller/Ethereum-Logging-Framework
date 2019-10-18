package au.csiro.data61.aap.spec;

import au.csiro.data61.aap.spec.types.SolidityType;

/**
 * ValueSource
 */
public interface ValueSource {
    public Object getValue();
    public SolidityType getType();    
}
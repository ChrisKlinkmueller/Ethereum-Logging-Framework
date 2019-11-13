package au.csiro.data61.aap.program.suppliers;

import au.csiro.data61.aap.program.types.SolidityType;

/**
 * ValueSource
 */
public interface ValueSource {
    public Object getValue();
    public SolidityType getType();    
}
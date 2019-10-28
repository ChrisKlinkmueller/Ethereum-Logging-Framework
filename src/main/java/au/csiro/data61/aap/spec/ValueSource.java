package au.csiro.data61.aap.spec;

import au.csiro.data61.aap.spec.types.SolidityType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * ValueSource
 */
public interface ValueSource {
    public MethodResult<Object> getValue();
    public SolidityType getType();    
}
package au.csiro.data61.aap.program;

import au.csiro.data61.aap.program.types.SolidityType;
import au.csiro.data61.aap.util.MethodResult;

/**
 * ValueSource
 */
public interface ValueSource {
    public MethodResult<Object> getValue();
    public SolidityType getType();    
}
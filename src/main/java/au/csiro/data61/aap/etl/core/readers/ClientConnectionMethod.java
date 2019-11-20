package au.csiro.data61.aap.etl.core.readers;

import au.csiro.data61.aap.etl.core.Method;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * ClientConnectionMethod
 */
public class ClientConnectionMethod implements Method {

    @Override
    public Object call(Object[] parameters, ProgramState state) throws ProgramException {
        assert parameters != null && parameters.length == 1;
        assert parameters[0] instanceof String;
        final String url = (String) parameters[0];
        state.getReader().connect(url);
        return null;
    }
}
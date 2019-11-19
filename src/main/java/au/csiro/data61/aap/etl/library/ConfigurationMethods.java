package au.csiro.data61.aap.etl.library;

import au.csiro.data61.aap.etl.core.EtlException;
import au.csiro.data61.aap.etl.core.ProgramState;

/**
 * Web3jMethods
 */
public class ConfigurationMethods {

    public static Object connectClient(Object[] parameters, ProgramState state) throws EtlException {
        assert parameters != null && parameters.length == 1;
        assert parameters[0] instanceof String;
        final String url = (String) parameters[0];
        state.getDataSource().connect(url);
        return null;
    }

    public static Object setOutputFolder(Object[] parameters, ProgramState state) throws EtlException {
        assert parameters != null && parameters.length == 1;
        assert parameters[0] instanceof String;
        final String outputFolder = (String) parameters[0];
        try {
            state.setOutputFolder(outputFolder);
        } catch (Throwable e) {
            throw new EtlException("Error when setting the output folder.", e);
        }
        return null;
    }
}
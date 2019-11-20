package au.csiro.data61.aap.etl.core;

import au.csiro.data61.aap.etl.core.exceptions.ProgramException;

/**
 * SetOutputFolderInstruction
 */
public class SetOutputFolderInstruction implements Method {

    @Override
    public Object call(Object[] parameters, ProgramState state) throws ProgramException {
        assert parameters != null && parameters.length == 1;
        assert parameters[0] instanceof String;
        final String outputFolder = (String) parameters[0];
        try {
            state.setOutputFolder(outputFolder);
        } catch (Throwable e) {
            throw new ProgramException("Error when setting the output folder.", e);
        }
        return null;
    }
    
}
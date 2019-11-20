package au.csiro.data61.aap.library;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import au.csiro.data61.aap.etl.core.readers.EthereumClient;
import au.csiro.data61.aap.etl.core.readers.Web3jClient;
import au.csiro.data61.aap.program.ExceptionHandlingStrategy;
import au.csiro.data61.aap.program.Method;
import au.csiro.data61.aap.program.ProgramState;
import au.csiro.data61.aap.program.types.SolidityString;
import au.csiro.data61.aap.program.types.SolidityVoid;

/**
 * Configurations
 */
class Configurations {
    private static final List<Method> CONFIGURATION_METHODS;
    
    public static Stream<Method> configurationMethods() {
        return CONFIGURATION_METHODS.stream();
    }
    
    static {
        CONFIGURATION_METHODS = new LinkedList<>();
        CONFIGURATION_METHODS.add(new Method(Configurations::connectToClient, SolidityVoid.DEFAULT_INSTANCE, "connect", SolidityString.DEFAULT_INSTANCE));
        CONFIGURATION_METHODS.add(new Method(Configurations::setExceptionHandlingStrategy, SolidityVoid.DEFAULT_INSTANCE, "setExceptionHandling", SolidityString.DEFAULT_INSTANCE));
    }

    private static final Void connectToClient(ProgramState state, Object[] parameters) throws Throwable {
        assert state != null;
        assert Library.isValidParameterList(parameters, String.class);

        final EthereumClient client = new Web3jClient(parameters[0].toString());
        state.setEthereumClient(client);

        state.addOnCloseListener(s -> {
            if (s.getEthereumClient() != null) {
                System.out.println("Closing down Ethereum client.");
                s.getEthereumClient().close();
            }
        });

        return null;
    }

    private static final Void setExceptionHandlingStrategy(ProgramState state, Object[] parameters) throws Throwable {
        assert state != null;
        assert Library.isValidParameterList(parameters, String.class);

        switch (parameters[0].toString()) {
            case "abort" : state.setExceptionHandlingStrategy(ExceptionHandlingStrategy.ABORT); break;
            case "continue" : state.setExceptionHandlingStrategy(ExceptionHandlingStrategy.CONTINUE); break;
            default : throw new IllegalArgumentException(
                String.format(
                    "The exception handling parameter must be set to 'abort' or 'pending', but was '%s'.",
                    parameters[0].toString()
                )
            );
        }

        return null;
    }
}
package au.csiro.data61.aap.library;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.Method;
import au.csiro.data61.aap.program.ProgramState;
import au.csiro.data61.aap.program.types.SolidityString;
import au.csiro.data61.aap.program.types.SolidityVoid;
import au.csiro.data61.aap.rpc.EthereumClient;

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
    }

    private static final Void connectToClient(ProgramState state, Object[] parameters) throws Throwable {
        assert state != null;
        assert Library.isValidParameterList(parameters, String.class);

        final EthereumClient client = new EthereumClient(parameters[0].toString());
        state.setEthereumClient(client);

        state.addOnCloseListener(s -> {
            if (s.getEthereumClient() != null) {
                System.out.println("Closing down Ethereum client.");
                s.getEthereumClient().close();
            }
        });

        return null;
    }
}
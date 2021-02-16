package blf.core.parameters;

import blf.core.exceptions.ExceptionHandler;
import org.web3j.abi.TypeReference;

/**
 * Parameter
 */
public class Parameter {
    private final String name;
    private final TypeReference<?> type;

    public Parameter(String solType, String name) {
        this(solType, name, false);
    }

    public Parameter(String solType, String name, boolean isIndexed) {

        TypeReference<?> typeOfParameter = this.createType(solType, isIndexed);
        assert typeOfParameter != null;

        this.type = typeOfParameter;
        this.name = name;
    }

    public boolean isIndexed() {
        return this.type.isIndexed();
    }

    public String getName() {
        return this.name;
    }

    public TypeReference<?> getType() {
        return this.type;
    }

    private TypeReference<?> createType(String solType, boolean isIndexed) {
        try {
            return TypeReference.makeTypeReference(solType, isIndexed, false);
        } catch (ClassNotFoundException e) {
            ExceptionHandler.getInstance().handleException(e.getMessage(), e);
            return null;
        }

    }
}

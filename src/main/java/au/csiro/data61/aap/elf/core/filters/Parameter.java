package au.csiro.data61.aap.elf.core.filters;

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
        assert name != null;

        TypeReference<?> type = this.createType(solType, isIndexed);
        assert type != null;
        
        this.type = type;
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
        }
        catch (ClassNotFoundException ex) {
            return null;
        }
        
    }
}
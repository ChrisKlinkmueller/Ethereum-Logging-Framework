package au.csiro.data61.aap.specification;

/**
 * Variable
 */
public class Variable implements ValueSource {
    private final String name;
    private final String type;

    public Variable(String type, String name) {
        assert type != null && !type.trim().isEmpty();
        assert name != null && !name.trim().isEmpty();
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getReturnType() {
        return this.type;
    }

    @Override
    public int hashCode() {
        final int prime = 43;
        int hash = 41;
        hash = prime * hash + this.type.hashCode();
        hash = prime * hash + this.name.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Variable)) {
            return false;
        }
        
        final Variable var = (Variable)obj;
        return var.name.equals(this.name) &&
               var.type.equals(this.type);
    }
    
}
package au.csiro.data61.aap.library.types;

public class StringType extends BytesType {
    private static final String NAME = "string";

    public StringType() {
        super();
    }

    @Override
    public String getTypeName() {
        return NAME;
    }

    @Override
    public int hashCode() {
        final int prime = 257;
        int hash = 283;
        return hash + prime * hash + NAME.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof StringType;
    }
        
}
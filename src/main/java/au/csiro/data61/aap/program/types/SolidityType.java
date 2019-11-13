package au.csiro.data61.aap.program.types;

/**
 * SolidityType
 */
public abstract class SolidityType {
    
    public abstract String getName();

    /*public boolean castableFrom(SolidityType type) {
        return type != null && this.compatibleTypes.contains(type.getClass());
    }*/

    public boolean conceptuallyEquals(SolidityType type) {
        return type != null && this.getClass().equals(type.getClass());
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
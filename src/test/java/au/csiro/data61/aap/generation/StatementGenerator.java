package au.csiro.data61.aap.generation;

import java.util.Random;

import au.csiro.data61.aap.spec.Scope;
import au.csiro.data61.aap.spec.Statement;

/**
 * StatementGenerator
 */
public class StatementGenerator {    
    private final Random random;
    public StatementGenerator(Random random) {
        assert random != null;
        this.random = random;
    }

    public Statement createStatement(Scope scope) {
        return null;
    }

    public static void main(String[] args) {
        
    }

}
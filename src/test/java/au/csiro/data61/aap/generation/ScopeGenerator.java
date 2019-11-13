package au.csiro.data61.aap.generation;

import java.util.Random;

/**
 * ScopeGenerator
 */
public class ScopeGenerator {
    private final Random random;
    public ScopeGenerator(Random random) {
        assert random != null;
        this.random = random;
    }
}
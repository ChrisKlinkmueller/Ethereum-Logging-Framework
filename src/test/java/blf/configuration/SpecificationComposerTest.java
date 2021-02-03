package blf.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecificationComposerTest {
    // TODO (Mykola Digtiar): No need of these tests?
    @Test
    void prepareProgramBuild() {
        SpecificationComposer composer = new SpecificationComposer();

        composer.prepareProgramBuild();

        assertTrue(true);
    }

    @Test
    void testSimpleComposer() {
        SpecificationComposer composer = new SpecificationComposer();

        composer.prepareProgramBuild();
        composer.prepareBlockRangeBuild();
        composer.prepareTransactionFilterBuild();

        assertTrue(true);
    }
}

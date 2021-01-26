package blf.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpecificationComposerTest {

    @Test
    void prepareProgramBuild() {
        SpecificationComposer composer = new SpecificationComposer();
        try {
            composer.prepareProgramBuild();
        } catch (BuildException e) {
            e.printStackTrace();
            fail();
        }

        assertThrows(BuildException.class, composer::prepareProgramBuild);
    }

    @Test
    void testSimpleComposer() {
        SpecificationComposer composer = new SpecificationComposer();
        try {
            composer.prepareProgramBuild();
        } catch (BuildException e) {
            e.printStackTrace();
            fail();
        }

        assertThrows(BuildException.class, composer::prepareTransactionFilterBuild);

        try {
            composer.prepareBlockRangeBuild();
            composer.prepareTransactionFilterBuild();
        } catch (BuildException e) {
            e.printStackTrace();
            fail();
        }
    }
}
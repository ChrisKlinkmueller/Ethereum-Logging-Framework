package au.csiro.data61.aap.elf

import au.csiro.data61.aap.samples.SampleUtils
import spock.lang.Specification
import spock.lang.Unroll

class ValidatorSpec extends Specification {
    @Unroll
    def "#url.getFile() should pass Validator"() {
        given:
        Validator validator = new Validator()

        expect:
        validator.analyzeScript(url.getFile()).size() == 0

        where:
        url << SampleUtils.getAllResources()
    }
}

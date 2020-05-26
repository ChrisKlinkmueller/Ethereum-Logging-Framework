package au.csiro.data61.aap.elf

import spock.lang.Specification

class ValidatorBaseSpec extends Specification {
    Validator validator = new Validator()

    static List<EthqlProcessingError> validate(String script, Validator validator) {
        validator.analyzeScript(new ByteArrayInputStream(script.getBytes()))
    }
}

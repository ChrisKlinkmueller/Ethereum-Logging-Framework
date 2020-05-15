package au.csiro.data61.aap.elf

import au.csiro.data61.aap.samples.SampleUtils
import spock.lang.Specification
import spock.lang.Unroll

class ValidatorSpec extends Specification {
    Validator validator = new Validator()

    @Unroll
    def "#url.getFile() should pass validation"() {
        when:
        List<EthqlProcessingError> errors = validator.analyzeScript(url.getFile())

        then:
        noExceptionThrown()
        errors.size() == 0

        where:
        url << SampleUtils.getAllResources()
    }

    def "Validator should throw exception when file not exists"() {
        when:
        validator.analyzeScript('notExist.ethql')

        then:
        EthqlProcessingException e = thrown()
        e.getMessage() == '''Invalid file path: 'notExist.ethql'.'''
    }

    @Unroll
    def "Validator should detect errors in #input"() {
        given:
        InputStream script = new ByteArrayInputStream(input.getBytes())

        when:
        List<EthqlProcessingError> errors = validator.analyzeScript(script)

        then:
        errors*.getErrorMessage() == error

        where:
        input | error
        '''setOutputFolder("./folder")''' // syntax error
              | ['''missing ';' at '<EOF>\'''']
        '''EMIT CSV ROW ("sha") (sha);''' // semantic error
              | ['''Variable 'sha' not defined.''']
        '''EMIT CSV ROW (5) (sha);''' // multiple semantic errors
              | ['''Variable 'sha' not defined.''', 'CSV table name must be a string.']
    }
}

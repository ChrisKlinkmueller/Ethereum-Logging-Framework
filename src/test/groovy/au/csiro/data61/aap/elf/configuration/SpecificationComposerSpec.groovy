package au.csiro.data61.aap.elf.configuration

import spock.lang.Specification

class SpecificationComposerSpec extends Specification {
    SpecificationComposer composer = new SpecificationComposer()

    def "prepareBuild should throw when states not empty"() {
        when: 'build a program with empty states'
        composer.prepareProgramBuild()
        then:
        notThrown(BuildException)

        when: 'build a program when another program is being build'
        composer.prepareProgramBuild()
        then:
        BuildException e = thrown()
        e.getMessage() == 'A program can only be build when no other filter is being build.'
    }

    def "prepareBuild should throw when current states not match"() {
        given:
        composer.prepareProgramBuild()

        when: 'build a transaction filter when current state is PROGRAM'
        composer.prepareTransactionFilterBuild()
        then:
        BuildException e = thrown()
        e.getMessage() == 'A transaction filter cannot be added to program, but only to: block range filter.'

        when: 'build a transaction filter when current state is BLOCK_RANGE_FILTER'
        composer.prepareBlockRangeBuild()
        composer.prepareTransactionFilterBuild()
        then:
        notThrown(BuildException)
    }
}

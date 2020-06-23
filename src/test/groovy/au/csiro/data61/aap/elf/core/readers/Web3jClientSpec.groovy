package au.csiro.data61.aap.elf.core.readers

import spock.lang.Specification

class Web3jClientSpec extends Specification {
    Web3jClient client = new Web3jClient()

    def "client should throw exception when block number doesn't exist yet"() {
        when:
        client.queryBlockData(1000000000000000 as BigInteger)

        then:
        thrown(IOException)
    }
}

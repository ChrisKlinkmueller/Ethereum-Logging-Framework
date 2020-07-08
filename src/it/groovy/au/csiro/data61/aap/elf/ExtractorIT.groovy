package au.csiro.data61.aap.elf

import au.csiro.data61.aap.elf.core.readers.Web3jClient
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

class ExtractorIT extends Specification {
    @Shared
    String webSocketUrl = "ws://localhost:8545"
    @Shared
    Integer blockTime = 5000 // milliseconds
    @Shared
    File testOutputFolder = new File("test_output")

    Web3jClient client = new Web3jClient(webSocketUrl)
    BigInteger blockNumberWhenTestStart = 0
    Extractor extractor = new Extractor()

    def setup() {
        assert testOutputFolder.mkdirs()
        blockNumberWhenTestStart = client.queryBlockNumber()
    }

    def cleanup() {
        assert testOutputFolder.deleteDir()
        client.close()
    }

    @Timeout(60)
    def "block filter should time out when 'to' hasn't been mined yet"() {
        when:
        Long time = timer.call({ extract("""
        | connect("$webSocketUrl");
        | setOutputFolder("${testOutputFolder.getPath()}");
        | BLOCKS ($blockNumberWhenTestStart) (${blockNumberWhenTestStart + 2}) {
        |   EMIT LOG LINE ("block number: ", block.number);
        | }
        """.stripMargin(), extractor) })
        then:
        checkOutputFile("all.log", """
        | block number: $blockNumberWhenTestStart
        | block number: ${blockNumberWhenTestStart + 1}
        | block number: ${blockNumberWhenTestStart + 2}
        """.stripMargin())
        // must have waited for one block time
        time > blockTime
    }

    @Timeout(60)
    def "block filter should time out when 'from' hasn't been mined yet"() {
        def blockToQuery = blockNumberWhenTestStart + 2
        when:
        Long time = timer.call({ extract("""
        | connect("$webSocketUrl");
        | setOutputFolder("${testOutputFolder.getPath()}");
        | BLOCKS ($blockToQuery) ($blockToQuery) {
        |   EMIT LOG LINE ("block number: ", block.number);
        | }
        """.stripMargin(), extractor) })
        then:
        checkOutputFile("all.log", """
        | block number: $blockToQuery
        """.stripMargin())
        // must have waited for one block time
        time > blockTime
    }

    static void extract(String script, Extractor extractor) {
        extractor.extractData(new ByteArrayInputStream(script.getBytes()))
    }

    void checkOutputFile(String fileName, String expectedContent) {
        File outputFile = new File(testOutputFolder.getPath(), fileName)
        assert outputFile.text.replaceAll("\\s+", "") == expectedContent.replaceAll("\\s+", "")
    }

    Closure timer = { Closure closure ->
        def start = System.currentTimeMillis()
        closure.call()
        System.currentTimeMillis() - start
    }
}

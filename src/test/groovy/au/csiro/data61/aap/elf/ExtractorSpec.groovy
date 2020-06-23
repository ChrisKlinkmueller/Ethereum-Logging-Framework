package au.csiro.data61.aap.elf

import au.csiro.data61.aap.elf.core.ProgramState
import au.csiro.data61.aap.elf.core.readers.DataReader
import au.csiro.data61.aap.elf.core.readers.EthereumClient
import au.csiro.data61.aap.elf.core.readers.RawBlock
import spock.lang.Shared
import spock.lang.Specification
import java.nio.file.Files
import java.nio.file.Paths

class ExtractorSpec extends Specification {
    @Shared
    File testOutputFolder = new File("test_output")

    def setup() {
        assert testOutputFolder.mkdirs()
    }

    def cleanup() {
        assert testOutputFolder.deleteDir()
    }

    def "log entry filter should return expected result"() {
        given:
        def extractor = extractorFor(readBlocksFromJson("blocks.json"))

        when:
        extract("""
        | setOutputFolder("${testOutputFolder.getPath()}");
        | BLOCKS (0) (0) {
        |   LOG ENTRIES (0x06012c8cf97BEaD5deAe237070F9587f8E7A266d)
        |               (Transfer(address from, address to, uint256 tokenId)) {
        |     EMIT LOG LINE ("block number: ", block.number);
        |     EMIT LOG LINE ("from address: ", from);
        |     EMIT LOG LINE ("to address: ", to);
        |     EMIT LOG LINE ("tokenId: ", tokenId);
        |   }
        | }
        """.stripMargin(), extractor)
        then:
        checkOutputFile("all.log", """
        | block number: 0
        | from address: 0x51c290e134ac179635ba5374591c81c1e2f8ab87
        | to address: 0x60ad418b78e5a211ab6fd53dc9a64e316a9146d3
        | tokenId: 1018019
        """.stripMargin())
    }

    static void extract(String script, Extractor extractor) {
        extractor.extractData(new ByteArrayInputStream(script.getBytes()))
    }

    Extractor extractorFor(RawBlock[] blocks) {
        ProgramState state = Spy(ProgramState) {
            getReader() >> Spy(DataReader) {
                getClient() >> Stub(EthereumClient) {
                    close() >> {}
                    queryBlockNumber() >> blocks.size()
                    queryBlockData(_ as BigInteger) >> { BigInteger i ->
                        {
                            if (i >= blocks.size())
                                throw new IOException("""block number $i doesn't exist.""")
                            else blocks[i.toInteger()]
                        }
                    }
                }
            }
        }
        new Extractor(state)
    }

    RawBlock[] readBlocksFromJson(String fileName) {
        String path = getClass().getClassLoader().getResource(fileName).getPath()
        String blocksJson = new String(Files.readAllBytes(Paths.get(path)))
        RawBlock.fromJsonString(blocksJson)
    }

    Boolean checkOutputFile(String fileName, String expectedContent) {
        File outputFile = new File(testOutputFolder.getPath(), fileName)
        outputFile.text.replaceAll("\\s+", "") == expectedContent.replaceAll("\\s+", "")
    }
}

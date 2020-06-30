package au.csiro.data61.aap.elf

import au.csiro.data61.aap.elf.core.ProgramState
import au.csiro.data61.aap.elf.core.exceptions.ExceptionHandler
import au.csiro.data61.aap.elf.core.readers.DataReader
import au.csiro.data61.aap.elf.core.readers.EthereumClient
import au.csiro.data61.aap.elf.core.readers.RawBlock
import au.csiro.data61.aap.elf.core.readers.RawLogEntry
import au.csiro.data61.aap.elf.core.readers.RawTransaction
import com.fasterxml.jackson.databind.ObjectMapper
import org.web3j.abi.TypeDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Type
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.nio.file.Files
import java.nio.file.Paths

class ExtractorSpec extends Specification {
    @Shared
    File testOutputFolder = new File("test_output")

    ExceptionHandler exceptionHandler = Mock(ExceptionHandler)

    def setup() {
        assert testOutputFolder.mkdirs()
    }

    def cleanup() {
        assert testOutputFolder.deleteDir()
    }

    @Ignore
    @Timeout(5)
    // This test case should fail because it should time out
    def "block filter should time out when 'to' hasn't been mined yet"() {
        given:
        def extractor = extractorFor(readBlocksFromJson("blocks.json"))

        when:
        extract("""
        | setOutputFolder("${testOutputFolder.getPath()}");
        | BLOCKS (2) (6) {
        |   EMIT LOG LINE ("block number: ", block.number);
        | }
        """.stripMargin(), extractor)
        then:
        notThrown()
    }

    @Ignore
    @Timeout(5)
    // This test case should fail because it should time out
    def "block filter should time out when 'from' hasn't been mined yet"() {
        given:
        def extractor = extractorFor(readBlocksFromJson("blocks.json"))

        when:
        extract("""
        | setOutputFolder("${testOutputFolder.getPath()}");
        | BLOCKS (5) (10) {
        |   EMIT LOG LINE ("block number: ", block.number);
        | }
        """.stripMargin(), extractor)
        then:
        notThrown()
    }

    def "log entry filter should return expected result"() {
        given:
        def extractor = extractorFor(readBlocksFromJson("blocks.json"))

        when:
        extract("""
        | setOutputFolder("${testOutputFolder.getPath()}");
        | BLOCKS (2) (4) {
        |   LOG ENTRIES (0x06012c8cf97BEaD5deAe237070F9587f8E7A266d)
        |               (Pregnant (address owner, uint256 matronId, uint256 sireId, uint256 cooldownEndBlock)) {
        |     EMIT LOG LINE ("block number: ", block.number);
        |     EMIT LOG LINE ("owner address: ", owner);
        |     EMIT LOG LINE ("matron id: ", matronId);
        |   }
        | }
        """.stripMargin(), extractor)
        then:
        checkOutputFile("all.log", """
        | block number: 3
        | owner address: 0x68b42e44079d1d0a4a037e8c6ecd62c48967e69f
        | matron id: 1689620
        | block number: 3
        | owner address: 0x68b42e44079d1d0a4a037e8c6ecd62c48967e69f
        | matron id: 790799
        | block number: 3
        | owner address: 0x68b42e44079d1d0a4a037e8c6ecd62c48967e69f
        | matron id: 1885100
        """.stripMargin())
    }

    def "smart contract filter should return expected result"() {
        given:
        def extractor = extractorFor(readBlocksFromJson("blocks.json"))

        when:
        extract("""
        | setOutputFolder("${testOutputFolder.getPath()}");
        | address plus100Contract = 0x1234512345123451234512345123451234512345;
        | BLOCKS (1) (4) {
        |   SMART CONTRACT (plus100Contract)
        |                  (uint plus100Block = plus100()) {
        |     EMIT LOG LINE ("current block number + 100: ", plus100Block);
        |   }
        | }
        """.stripMargin(), extractor)
        then:
        checkOutputFile("all.log", """
        | current block number + 100: 101
        | current block number + 100: 102
        | current block number + 100: 103
        | current block number + 100: 104
        """.stripMargin())
    }

    def "smart contract filter should log exception when contract not exist"() {
        given:
        def extractor = extractorFor(readBlocksFromJson("blocks.json"))

        when:
        extract("""
        | setOutputFolder("${testOutputFolder.getPath()}");
        | address plus100Contract = 0x1234512345123451234512345123451234512345;
        | BLOCKS (0) (1) {
        |   SMART CONTRACT (plus100Contract)
        |                  (uint plus100Block = plus100()) {
        |     EMIT LOG LINE ("current block number + 100: ", plus100Block);
        |   }
        | }
        """.stripMargin(), extractor)
        then:
        1 * exceptionHandler.handleExceptionAndDecideOnAbort(
                _ as String,
                { Throwable th ->
                    th.getMessage() == "Error querying members of smart contract 0x1234512345123451234512345123451234512345"
                    th.getCause().getMessage() == "Output parameters not compatible with return values."
                }
        )
        checkOutputFile("all.log", """
        | current block number + 100: 101
        """.stripMargin())
    }

    static void extract(String script, Extractor extractor) {
        extractor.extractData(new ByteArrayInputStream(script.getBytes()))
    }

    Extractor extractorFor(RawBlock[] blocks) {
        ProgramState state = Spy(ProgramState) {
            getExceptionHandler() >> exceptionHandler
            getReader() >> Spy(DataReader) {
                getClient() >> Stub(EthereumClient) {
                    close() >> {}
                    queryBlockNumber() >> blocks.size()
                    queryBlockData(_ as BigInteger) >> { BigInteger i -> blocks[i.toInteger()] }
                    // A smart contract with one function that
                    // returns current block number plus 100.
                    // the contract is deployed at block 1.
                    queryPublicMember(
                            _ as String,
                            _ as BigInteger,
                            _ as String,
                            _ as List<Type>,
                            _ as List<TypeReference<Type>>
                    ) >> {
                        String contract,
                        BigInteger block,
                        String memberName,
                        List<Type> inputs,
                        List<TypeReference<Type>> outputs ->
                            if (contract == "0x1234512345123451234512345123451234512345")
                                if (memberName == "plus100" && block >= 1)
                                    [TypeDecoder.instantiateType("uint", block + 100)]
                                else []
                            else []
                    }
                }
            }
        }
        new Extractor(state)
    }

    RawBlock[] readBlocksFromJson(String fileName) {
        String path = getClass().getClassLoader().getResource(fileName).getPath()
        String blocksJson = new String(Files.readAllBytes(Paths.get(path)))
        ObjectMapper objectMapper = new ObjectMapper()
        RawBlock[] blocks = objectMapper.readValue(blocksJson, RawBlock[].class)
        blocks.eachWithIndex { RawBlock block, int i ->
            {
                block.setNumber(i as BigInteger)
                block.transactionStream().eachWithIndex { RawTransaction transaction, int j ->
                    transaction.setTransactionIndex(j as BigInteger)
                    transaction.setBlock(block)
                    transaction.logStream().eachWithIndex { RawLogEntry log, int k ->
                        log.setLogIndex(k as BigInteger)
                        log.setTransaction(transaction)
                    }
                }
            }
        }
    }

    void checkOutputFile(String fileName, String expectedContent) {
        File outputFile = new File(testOutputFolder.getPath(), fileName)
        assert outputFile.text.replaceAll("\\s+", "") == expectedContent.replaceAll("\\s+", "")
    }
}

package au.csiro.data61.aap.elf

import spock.lang.Specification

class GeneratorSpec extends Specification {
    Generator generator = new Generator()

    def "generator should generate expected code"() {
        expect:
        checkGeneratedCode("""
        | address contract = 0x0000000000000000000000000000000000000000;
        |
        | BLOCKS (EARLIEST) (CURRENT) {
        |   LOG ENTRIES (contract) (GitCommit(uint authorId, bytes32 sha)) {
        |     string author = mapValue(authorId, "unknown", [0,1,2,3], ["first", "second", "third", "fourth"]);
        |     EMIT CSV ROW ("commits") (author, sha);
        |   }
        | }
        """.stripMargin(), """
        | // Generation result for event 'GitCommit(uintauthorId,bytes32sha)' (line: 5, column: 27)
        |
        | event GitCommit(uint authorId, bytes32 sha);
        | mapping (string => uint) authors;
        |
        | constructor() public {
        |   authors["first"] = 0;
        |   authors["second"] = 1;
        |   authors["third"] = 2;
        |   authors["fourth"] = 3;
        | }
        |
        | function logGitCommit(string memory author, bytes32 sha) internal {
        |   uint authorId = authors[author];
        |   emit GitCommit(authorId, sha);
        | }
        """.stripMargin())
    }

    void checkGeneratedCode(String script, String expectedCode) {
        String generatedCode = generator.generateLoggingFunctionality(new ByteArrayInputStream(script.getBytes()))
        assert generatedCode.replaceAll("\\s+", "") == expectedCode.replaceAll("\\s+", "")
    }
}

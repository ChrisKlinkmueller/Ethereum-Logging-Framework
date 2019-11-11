package au.csiro.data61.aap.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import au.csiro.data61.aap.spec.GlobalScope;
import au.csiro.data61.aap.util.StringUtil;

/**
 * ParseStatementTest
 */
public class ParseStatementTest {
    private final SpecificationParser parser = new SpecificationParser();

    @ParameterizedTest
    @MethodSource("validStatementSequence")
    public void testValidStatementSequences(String script) {
        SpecificationParserResult<GlobalScope> parserResult = this.parseScript(script);
        assertNotNull(parserResult, script);

        if (!parserResult.isSuccessful()) {
            parserResult.errorStream().forEach(System.out::println);
        }

        assertTrue(parserResult.isSuccessful(), script);
    }

    private SpecificationParserResult<GlobalScope> parseScript(String script) {
        final InputStream is = StringUtil.toStream(script);
        return this.parser.parseDocument(is);
    }

    private static Stream<Arguments> validStatementSequence() {
        return Stream.of(
            Arguments.of("bytes[] fKMbBaJ64H0 = {0xCadeb674DcFC8A65Dce3CF166cfD32F7e8dB1C84787F, 0xef816A460e66cbCA7FED0Be93227EaeAaB56f0ea01, 0xF0Ea49, 0x44Dcd17e64A86F1aAa8c6AbcaC9Da454};"),
			Arguments.of("bytes XXuSiXhgZS1 = 0x5f0c095b5be70F50cb1f49009f0cbEc7A5eaf7AA35dD5EE63dbA5e23;"),
			Arguments.of("address c0PtkBJwu22 = 0xA99BEAeedAeaff516C6cA546FeC386761A474f5f;"),
			Arguments.of("int[] hibHtHNZCK9:rgUGzqNP7Rm = {8432216569, 97447079969514, 597, 72, 946573079763477801, 105181268314813, 69923903690, 169748566};"),
			Arguments.of("bytes WAVHoj458WH = 0xE514EbebF1b3d4f394cd89a40b3Ce6;"),
			Arguments.of("int N3zWxJZuZHe:J1yrIbCnwQg = 7689097371;"),
			Arguments.of("fixed[] o8EA63GH9yu:NPItTfBtMLc = {631888440969200.73752, 8740.88487032, 84.3574395, 12191.8810269};"),
			Arguments.of("int wV4hGuwkgD3 = 13937981210647;"),
			Arguments.of("fixed EPZMthDm4OW = 137911.61726472;"),
			Arguments.of("fixed dTHT1G5sc7i = 4148.4826;"),
			Arguments.of("bytes WfXXiya6wR3 = 0x3eaAb715ACBEca747Be8B0AB8Ad2FEF230D6;"),
			Arguments.of("bool aQ1L02HVEA8:eFh994NMDT3 = false;"),
			Arguments.of("bytes IFqjnJ5JWIW:KwWapKBwxaj = 0xDb4FE212d99dA52414F29d4d524C4Ca2CB52Ad3Ab4a9;"),
			Arguments.of("fixed[] JnDRw43t2mO = {34026862384043791.91, 123.0119, 8758902137.5769116, 57819.8845514, 762720329669093397.7000203908, 9054.6, 65408454963345240.08};"),
			Arguments.of("bytes wAODjUppxuh:TxuHeI3kdvL = 0x0c2c1eFAbBCCa0b12EEFF49363E5eBa4B3;"),
			Arguments.of("bool G7tWZeViP3q = false;"),
			Arguments.of("string tCrMEh5DZv9 = \"ujj?Okz*U*9L#5(wOEf\";"),
			Arguments.of("fixed[] aFTPwXhgQxC = {87.16, 32379537882124471.49055458, 8315.450350564, 5.46650175, 923063854519381995.6119, 8345406.26092371, 81128902, 58209288724.973};"),
			Arguments.of("bytes[] l4tnxRxyx8f = {0x028eB86774Bf, 0x0E0bB4bDcEf1822dE10Be4, 0xea9494F6eFD5dA2feE, 0xbEebB5dF2bE2f182f3Fd9DeEbd94AeFfe4A1aB0dBd, 0x7e2cFA24BBCb, 0xEc769f34Fa34e81FdeC625a3D7eF317274e7ccC9CbAe2DfBF0, 0x5Cef8cA9AeC8bF14Cd4aADaf8CD2af8A4D0aAfc14caEFA8113Db};"),
			Arguments.of("address[] UBVxFuj2j1J:dj1YAsdXDgk = {0xcDaDD6F55D3Cd12DD22bBdACbFc4e8AABBed0bf6, 0x5A9EFc3998fBB2cEAa8e8dADd8855DdcB6BffaBb, 0xfdce3CEF8115195a29cE8c527d0a8cb9b2FeE4Cf, 0x4DfcbBEdAF81374EaddA00Bd3735e220eF6C9dc4, 0xCCE6ffF2fb6BDd52DCC97E2fBcaaE8e6D7C874c3, 0xAd7CC0cCa1D2a8Da3D2B732bd3CAC4DFc256EFe7, 0x6CF26C813B03Ea48dfBE4a9aDDbd4E744CeAcE1c};"),
			Arguments.of("string ix00ValAGvF = \"3JznDOzy^)e##P6p1IQbCDF\";"),
			Arguments.of("address[] msMHHoyvEY2:qccO8KaMLfe = {0x41C9DfaaD8eecF97E101bB6c38f88B4e3f9cd7DC, 0x9F4E67B1fdD424b5BfFDAbCcBfBe9A5fA70e031E, 0xd6D5b0d1FDA8a8BB43Cebeb17C3BDB42cE65f1df, 0xfE4CF1dCeb4Cda005C1a72bAd4457041B9AeB1d9, 0x9837ef5B0EfFe9b38c7F745E0e7dCE76f09c19FE, 0xAf5FdBdb4ebc4A47b73CFeAbab4fa652a9DbA548, 0x9caBBa840BE6BfCA2Cf57F8e4cfC6bb5DDCB6543, 0x1c091ae1BbC678d3a8e54b725B5D7a7b5f3Eb5D9};"),
			Arguments.of("address[] S0sFD19tFaJ = {0x3DAEefbD50280ED82edD1A8a104CeEFBCEc7B4b7};"),
			Arguments.of("bytes bxsmWyugTLO = 0xFBed9A517Adb21b7DbBacFDf388ba7BB8C7cE45d;"),
			Arguments.of("bytes[] isbnRMBb1vb = {0xdfD0a59AfeFE0CDA3477d5DbC54D5FB7A0f3650a4087bfE6E5, 0x8C2Ae92C5edaEd4F};"),
			Arguments.of("bool[] YBx9Zs0aCbw:STACqNE9gsM = {false, false, false, false, true, true, false, false, false, false};"),
			Arguments.of("int[] Pqlv10MmdiC:KI9C3kHcJHm = {9659741802, 0, 916268373486074847, 184296046};"),
			Arguments.of("string[] Je633BJiIY5:BAXm7XtS6Ay = {\"2*03APIo?(eOx#f(\", \"3M2mW*5^%yvZPYfVT\", \"g!BW1Bj:sRlLtGhMiC?wUdNk\"};"),
			Arguments.of("bytes rCunbuUc0Um = 0xddAbeFf2B7D7C1ac4AFa56B7AB;"),
			Arguments.of("address ORewAPYj2Ro = 0x4d27c7eFa8cF80b372DED971C58d4fd54e79e8DA;"),
			Arguments.of("address SoidTHbQkNJ = 0xDE590616eCb9e2beD6bbdDBC6ACF6602Da0Da9e9;"),
			Arguments.of("address dyGNjK0os2k:gLhvuSnbVcH = 0xdb6dAFef89c1F0BAEFfABF3EbeAfE362A7112eeC;"),
			Arguments.of("fixed mVBAHtG2ubW = 50840565471663744.91456990;"),
			Arguments.of("string[] uhXBxZZibwX:O1VPkhRfWmb = {\"PY7JXLp:tTonddG6K08%\", \"N8fjjYRo2eiOr:y\", \"Q@&Y2\", \"9g5v^xP6ppIIlEnH13\"};"),
			Arguments.of("bytes wDxfMMRBb1S:uwqAugTS4VJ = 0xE1fd7b23AA3D101AE6dd3fb6fEA6C2;"),
			Arguments.of("string[] GWqU2xtVsQv = {\"pgQ6snxsk)rMpNhUjtv$bi\"};"),
			Arguments.of("string U8rSFCKXLqu:VMQsYgOgRCd = \"ygMxJrEqS6Qlm:KsuV\";"),
			Arguments.of("bytes[] AfzcLJc8p5N:InBDrD9gvGi = {0x7EA4Cd8ADAa5ddA114Dee0511c977FCDfe2C306434DFe135d7FEddb06c, 0xecF5DfDaaa07FEf7E4ebaD9daFC0CB6bCF71d6f2a5B1086DA2d5c1DFf7bd, 0x97525BCbe3BdC9F9EC, 0xDA};"),
			Arguments.of("bytes G1sCZLeQUbV = 0x5AE02d9B4c4Af6cE54F9F788C7;"),
			Arguments.of("address GP7DwVwcptW:dQ4KCHSfnnz = 0xdE2CDF9e2ecBbAF202aD22BEEe76dcE662F2fF53;"),
			Arguments.of("string WOcH2Z07AOT = \"S.OE5Fq*FpSxj.kgi&9mSCzoL\";"),
			Arguments.of("int[] Vf3J2t1qE0t = {3930880, 69122188345, 33457, 422, 399655011790, 8019152575057805, 76878, 3946, 962571600};"),
			Arguments.of("string xtnihoFhA4i = \":lnE\";"),
			Arguments.of("address[] f4GnC4Hitu7 = {0x2cd1F3fBad9C535De09c6D2D897dFdcc3BF5CC86, 0xA6d97A55449B2BCB922a5A9fbac0d320D3fF2fb2};"),
			Arguments.of("fixed BV5gh36B2kj:sU34FdNATeF = 35138297454.7301802890;"),
			Arguments.of("bytes[] cFiWajYTHg9 = {0xCAccb7B9EbD4a0b3E281d69E9E153Bf4};"),
			Arguments.of("address[] ZchDNtd5gOm:KDPn9TLK3M1 = {0xEb72A7323FD96bD82BbCD46Bcad99090890DEa1E, 0x3C1C413f3fE88c1Aa1789Ef6F3aeBca04DAA44df, 0xeeB7BeB715cCdFdCCB55aB767E0DfC95b2c53F6F, 0x57ECd73BEBee5BDDeb73BC4baE9E22Be3A8AF900, 0xA5B7BBAaeEF1FbC8A49BC43b87E2aCBFb2aca555};"),
			Arguments.of("address[] oP5UTELTDbc:w1fwEOE4FPl = {0xEfE6bcD5eE1C3fE813D78CcfeBCCfe7a9D2Dbd17};"),
			Arguments.of("bytes wAK9aU07FvL:tWBhph7o1qj = 0xC38bCfbc02DF460C278Bf475BF82C50bE2a3a90CfcdaD2aD61CfC6cC;"),
			Arguments.of("bytes[] CDbiDwUcw6v:luUa7ueCAIG = {0xF1E7d5Acb9Ce4FFDCeFBfF4CdfDF164e3FDc1F, 0x9eBf57Dbc9, 0xdBbac00AC0E86FcC1EDcFfBFEe82EC4Ad1BeeC4a0C2CD885, 0xCF2d75b057BA9C9C16dcF140B8Ebec9dB95aBbe4FD4Ec6DD159417928c16Efd6, 0x3FEd9DcAF365Adef9BeF7b69ecFEb0Ddf8F61F5EAc5e64Daf1c5e1, 0xa93eB5, 0x1D3bE9CD1f1c517A9c6bc446cFc0AE, 0x9EA9};")
        );
    }
}
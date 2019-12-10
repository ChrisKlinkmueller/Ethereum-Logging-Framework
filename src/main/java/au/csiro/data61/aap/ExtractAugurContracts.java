package au.csiro.data61.aap;

import java.util.Arrays;
import java.util.List;

import au.csiro.data61.aap.elf.configuration.BlockNumberSpecification;
import au.csiro.data61.aap.elf.configuration.BuildException;
import au.csiro.data61.aap.elf.configuration.GenericFilterPredicateSpecification;
import au.csiro.data61.aap.elf.configuration.LogLineExportSpecification;
import au.csiro.data61.aap.elf.configuration.MethodCallSpecification;
import au.csiro.data61.aap.elf.configuration.MethodSpecification;
import au.csiro.data61.aap.elf.configuration.ParameterSpecification;
import au.csiro.data61.aap.elf.configuration.SmartContractFilterSpecification;
import au.csiro.data61.aap.elf.configuration.SmartContractQuerySpecification;
import au.csiro.data61.aap.elf.configuration.SpecificationComposer;
import au.csiro.data61.aap.elf.configuration.TypedValueAccessorSpecification;
import au.csiro.data61.aap.elf.configuration.ValueAccessorSpecification;
import au.csiro.data61.aap.elf.configuration.ValueAssignmentSpecification;
import au.csiro.data61.aap.elf.configuration.ValueMutatorSpecification;
import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.values.BlockVariables;


/**
 * ExtractAugurContracts
 */
public class ExtractAugurContracts {
    private static final long FROM = 5926257;
    private static final long TO = 5926310;

    public static void main(String[] args) throws Throwable {
        // for (String contract : contracts) {
            //Bytes32 type = new Bytes32(bytes32);
            // printCode(contract);
        // }

        try {
            Instruction program = buildProgram();
            ProgramState state = new ProgramState();
            program.execute(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // private static void printCode(String contract) {
    //     String address = createBytes32(contract);
    //     System.out.println(String.format("\tSMART CONTRACT (augurContract) (address contractAddress = lookup(%s)) {", address));
    //     System.out.println(String.format("\t\tboolean addressKnown = contains(registeredAddresses, contractAddress);"));
    //     System.out.println(String.format("\t\tIF (!contains) {"));
    //     System.out.println(String.format("\t\t\tEMIT LOG LINE (\"Block \", block.number, \": New %s registered with address '\", contractAddress, \"'.\");",contract));
    //     System.out.println(String.format("\t\t}"));
    //     System.out.println(String.format("\t}"));
    //     System.out.println();
    // }

    //     

    private static Instruction buildProgram() throws BuildException {
        final SpecificationComposer builder = new SpecificationComposer();

        builder.prepareProgramBuild(); 
            ExtractComposerUtils.addOutputFolderConfig(builder);
            ExtractComposerUtils.addConnectCall(builder);       
            builder.addVariableAssignment(
                ValueMutatorSpecification.ofVariableName("knownAddresses"), 
                ValueAccessorSpecification.addressArrayLiteral("[0x0000000000000000000000000000000000000000]"));
            builder.prepareBlockRangeBuild();
                for (String contract : CONTRACTS) {
                    addSmartContractFilter(builder, contract);
                }
            builder.buildBlockRange(
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(FROM)),
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(TO))
            );

        return builder.buildProgram();
    }



    private static void addSmartContractFilter(SpecificationComposer builder, String contractName) throws BuildException {
        builder.addInstruction(ValueAssignmentSpecification.of(
                ValueMutatorSpecification.ofVariableName("contractName"), 
                ValueAccessorSpecification.bytesLiteral("0x4D61726B6574466163746F727900000000000000000000000000000000000000"))
            );
        
        builder.prepareSmartContractFilterBuild();
            builder.prepareGenericFilterBuild();
                builder.addInstruction(
                    LogLineExportSpecification.ofValues(
                        ValueAccessorSpecification.stringLiteral("\"Block \""),
                        ValueAccessorSpecification.ofVariable(BlockVariables.BLOCK_NUMBER),
                        ValueAccessorSpecification.stringLiteral("\": New \""),
                        ValueAccessorSpecification.stringLiteral("\"" + contractName + "\""),
                        ValueAccessorSpecification.stringLiteral("\" registered with address '\""),
                        ValueAccessorSpecification.ofVariable("registeredAddress"),
                        ValueAccessorSpecification.stringLiteral("\"'.\"")
                    )
                );
                builder.addInstruction(
                    MethodCallSpecification.of(
                        MethodSpecification.of("add", "address[]", "address"), 
                        ValueAccessorSpecification.ofVariable("knownAddresses"), 
                        ValueAccessorSpecification.ofVariable("registeredAddress")
                    )
                );
            builder.buildGenericFilter(
                GenericFilterPredicateSpecification.not(
                    GenericFilterPredicateSpecification.in(
                        ValueAccessorSpecification.ofVariable("registeredAddress"), 
                        ValueAccessorSpecification.ofVariable("knownAddresses")
                    )
                )
            );

        final String hexString = createBytes32(contractName);
        builder.buildSmartContractFilter(
            SmartContractFilterSpecification.of(
                ValueAccessorSpecification.addressLiteral("0xb3337164e91b9f05c87c7662c7ac684e8e0ff3e7"),
                SmartContractQuerySpecification.ofMemberFunction(
                    "lookup", 
                    Arrays.asList(TypedValueAccessorSpecification.of("bytes32", ValueAccessorSpecification.bytesLiteral(hexString))), 
                    Arrays.asList(ParameterSpecification.of("registeredAddress", "address"))
                )
            )
        );
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String createBytes32(String contract) {
        byte[] bytes = contract.getBytes();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        final StringBuilder hexString = new StringBuilder(new String(hexChars));
        while(hexString.length() < 64) {
            hexString.append(0);
        }

        return String.format("0x%s", hexString.toString());
    }

    private static final List<String> CONTRACTS = Arrays.asList("Cash", "CompleteSets", "CreateOrder",
        "DisputeCrowdsourcerFactory", 
        "FeeWindow", "FeeWindowFactory", "FeeToken", "FeeTokenFactory", "FillOrder", "InitialReporter", "InitialReporterFactory", 
        "LegacyReputationToken", "Mailbox", "MailboxFactory", "Map", "MapFactory", "Market", "MarketFactory", "Orders", 
        "OrdersFetcher", "RepPriceOracle", "ReputationToken", "ReputationTokenFactory", "ShareToken", "ShareTokenFactory", 
        "Time", "Universe", "UniverseFactory"); 
}
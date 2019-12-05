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
import au.csiro.data61.aap.elf.configuration.SmartContractQuerySpecification;
import au.csiro.data61.aap.elf.configuration.SpecificationComposer;
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
        //     byte[] bytes = contract.getBytes();
        //     byte[] bytes32 = Arrays.copyOf(bytes, 32);
        //     //Bytes32 type = new Bytes32(bytes32);
        //     System.out.println(bytesToHex(bytes32));
        // }

        try {
            Instruction program = buildProgram();
            ProgramState state = new ProgramState();
            program.execute(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*try {            
            while (currentBlock.compareTo(lastBlock) <= 0) {
                for (String contract : contracts) {
                    byte[] bytes = contract.getBytes();
                    byte[] bytes32 = Arrays.copyOf(bytes, 32);
                    Bytes32 type = new Bytes32(bytes32);
                    
                    Function function = new Function("lookup", Arrays.asList(type), Arrays.asList(TypeReference.makeTypeReference("address")));
                    String data = FunctionEncoder.encode(function);
                    Transaction tx = Transaction.createEthCallTransaction("0xb3337164e91b9f05c87c7662c7ac684e8e0ff3e7", "0xb3337164e91b9f05c87c7662c7ac684e8e0ff3e7", data);
                    String result = client.ethCall(tx, currentBlock).toString();
                    
                    if (!result.toString().equals("0x0000000000000000000000000000000000000000000000000000000000000000")) {
                        if (!knownAddress.contains(result)) {
                            knownAddress.add(result);
                            System.out.println(String.format("Block %s: New %s contract deployed and registered with address '%s'.", currentBlock, contract, result));
                        }
                    }

                }

                currentBlock = currentBlock.add(BigInteger.ONE);
            }
        }
        finally {
            client.close();
        }*/
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static Instruction buildProgram() throws BuildException {
        final SpecificationComposer builder = new SpecificationComposer();

        builder.prepareProgramBuild(); 
            ExtractComposerUtils.addOutputFolderConfig(builder);
            ExtractComposerUtils.addConnectCall(builder);       
            builder.addVariableAssignment(
                ValueMutatorSpecification.ofVariableName("knownAddresses"), 
                ValueAccessorSpecification.addressArrayLiteral("[0x0000000000000000000000000000000000000000]"));
            builder.prepareBlockRangeBuild();
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
                                ValueAccessorSpecification.ofVariable("contractName"),
                                ValueAccessorSpecification.stringLiteral("\": registered with address '\""),
                                ValueAccessorSpecification.ofVariable("registeredContract"),
                                ValueAccessorSpecification.stringLiteral("\"'.\"")
                            )
                        );
                        builder.addInstruction(
                            MethodCallSpecification.of(
                                MethodSpecification.of("add", "address[]", "address"), 
                                ValueAccessorSpecification.ofVariable("registeredContract")
                            )
                        );
                    builder.buildGenericFilter(
                        GenericFilterPredicateSpecification.not(
                            GenericFilterPredicateSpecification.in(
                                ValueAccessorSpecification.ofVariable("registeredContract"), 
                                ValueAccessorSpecification.ofVariable("knownAddresses")
                            )
                        )
                    );

                builder.buildSmartContractFilter(
                    SmartContractQuerySpecification.ofMemberFunction(
                        "0xb3337164e91b9f05c87c7662c7ac684e8e0ff3e7", 
                        "lookup", 
                        Arrays.asList(ParameterSpecification.of("contractName", "bytes32")), 
                        Arrays.asList(ParameterSpecification.of("registeredContract", "address"))
                    )
                );

            builder.buildBlockRange(
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(FROM)),
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(TO))
            );

        return builder.buildProgram();
    }



    public static final List<String> contracts = Arrays.asList("Cash", "CompleteSets", "CreateOrder",
        "DisputeCrowdsourcerFactory", 
        "FeeWindow", "FeeWindowFactory", "FeeToken", "FeeTokenFactory", "FillOrder", "InitialReporter", "InitialReporterFactory", 
        "LegacyReputationToken", "Mailbox", "MailboxFactory", "Map", "MapFactory", "Market", "MarketFactory", "Orders", 
        "OrdersFetcher", "RepPriceOracle", "ReputationToken", "ReputationTokenFactory", "ShareToken", "ShareTokenFactory", 
        "Time", "Universe", "UniverseFactory"); 
}
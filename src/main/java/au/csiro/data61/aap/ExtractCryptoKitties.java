package au.csiro.data61.aap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import au.csiro.data61.aap.etl.configuration.AddressListSpecification;
import au.csiro.data61.aap.etl.configuration.BlockNumberSpecification;
import au.csiro.data61.aap.etl.configuration.BuildException;
import au.csiro.data61.aap.etl.configuration.CsvExportSpecification;
import au.csiro.data61.aap.etl.configuration.LogEntrySignatureSpecification;
import au.csiro.data61.aap.etl.configuration.MethodSpecification;
import au.csiro.data61.aap.etl.configuration.ProgramBuilder;
import au.csiro.data61.aap.etl.configuration.ValueAccessorSpecification;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.values.BlockVariables;
import au.csiro.data61.aap.etl.core.values.LogEntryVariables;
import au.csiro.data61.aap.etl.core.values.TransactionVariables;

/**
 * ExtractCryptoKitties
 */
public class ExtractCryptoKitties {
    private static final String START_BLOCK = "4605167";
    private static final String END_BLOCK = "4608167";
    private static final String URL = "ws://localhost:8546/";
    private static final String FOLDER = "C:/Development/xes-blockchain/v0.2/test_output/kitties";

    public static void main(String[] args) {
        try {
            Instruction program = buildProgram();
            ProgramState state = new ProgramState();
            program.execute(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Instruction buildProgram() throws BuildException {
        final ProgramBuilder builder = new ProgramBuilder();

        builder.prepareProgramBuild();
            builder.addMethodCall(MethodSpecification.of("connect", "string"), ValueAccessorSpecification.stringLiteral(URL));
            builder.addMethodCall(MethodSpecification.of("setOutputFolder", "string"), ValueAccessorSpecification.stringLiteral(FOLDER));
            builder.prepareBlockRangeBuild();
                builder.prepareTransactionFilterBuild();
                    builder.prepareLogEntryFilterBuilder();
                    addCsvExport(builder);
                    builder.buildLogEntryFilter(
                        AddressListSpecification.ofAddress("0x06012c8cf97BEaD5deAe237070F9587f8E7A266d"), 
                        LogEntrySignatureSpecification.of(
                            "Birth", 
                            new String[]{"address", "uint256", "uint256", "uint256", "uint256"}, 
                            new String[]{"owner", "kittyId", "matronId", "sireId", "genes"},
                            new boolean[]{false, false, false, false, false}
                        )    
                    );
                builder.buildTransactionFilter(AddressListSpecification.ofAny(), AddressListSpecification.ofAny());
            builder.buildBlockRange(
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(START_BLOCK)),
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(END_BLOCK))
            );

        return builder.buildProgram();
    }    

    private static void addCsvExport(ProgramBuilder builder) throws BuildException {
        final List<String> names = Arrays.asList(
            BlockVariables.BLOCK_NUMBER, 
            TransactionVariables.TX_TRANSACTIONINDEX, 
            LogEntryVariables.LOG_INDEX,
            "kittyId"
        );
        final List<ValueAccessorSpecification> valueAccessors = names.stream()
            .map(name -> ValueAccessorSpecification.ofVariable(name))
            .collect(Collectors.toList());
        final CsvExportSpecification export = CsvExportSpecification.of(
            "events", 
            names, 
            valueAccessors
        );
        builder.addInstruction(export);
    }


}
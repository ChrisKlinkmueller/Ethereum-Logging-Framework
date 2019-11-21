package au.csiro.data61.aap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import au.csiro.data61.aap.etl.configuration.AddressListSpecification;
import au.csiro.data61.aap.etl.configuration.BlockNumberSpecification;
import au.csiro.data61.aap.etl.configuration.BuildException;
import au.csiro.data61.aap.etl.configuration.LogEntrySignatureSpecification;
import au.csiro.data61.aap.etl.configuration.ProgramBuilder;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.Method;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.SetOutputFolderInstruction;
import au.csiro.data61.aap.etl.core.filters.BlockVariables;
import au.csiro.data61.aap.etl.core.filters.LogEntryVariables;
import au.csiro.data61.aap.etl.core.filters.TransactionVariables;
import au.csiro.data61.aap.etl.core.readers.ClientConnectionMethod;
import au.csiro.data61.aap.etl.core.values.Literal;
import au.csiro.data61.aap.etl.core.values.UserVariables;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;
import au.csiro.data61.aap.etl.core.writers.AddCsvRowInstruction;

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
            builder.addMethodCall(new SetOutputFolderInstruction(), Arrays.asList(Literal.stringLiteral(FOLDER)), null);
            Method connectionMethod = new ClientConnectionMethod();
            builder.addMethodCall(connectionMethod, Arrays.asList(Literal.stringLiteral(URL)), null);
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
            builder.buildBlockRange(BlockNumberSpecification.ofBlockNumber(START_BLOCK), BlockNumberSpecification.ofBlockNumber(END_BLOCK));

        return builder.buildProgram();
    }    

    private static void addCsvExport(ProgramBuilder builder) throws BuildException {
        final List<String> names = Arrays.asList(
            BlockVariables.BLOCK_NUMBER, 
            TransactionVariables.TX_TRANSACTIONINDEX, 
            LogEntryVariables.LOG_INDEX,
            "kittyId"
        );
        final List<ValueAccessor> valueAccessors = names.stream()
            .map(name -> UserVariables.createValueAccessor(name))
            .collect(Collectors.toList());
        final Instruction export = new AddCsvRowInstruction("events", names, valueAccessors);
        builder.addInstruction(export);
    }


}
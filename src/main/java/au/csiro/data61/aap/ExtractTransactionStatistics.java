package au.csiro.data61.aap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.csiro.data61.aap.etl.configuration.AddressListSpecification;
import au.csiro.data61.aap.etl.configuration.BlockNumberSpecification;
import au.csiro.data61.aap.etl.configuration.BuildException;
import au.csiro.data61.aap.etl.configuration.ProgramBuilder;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.Method;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.SetOutputFolderInstruction;
import au.csiro.data61.aap.etl.core.values.ValueAccessor;
import au.csiro.data61.aap.etl.core.writers.AddCsvRowInstruction;
import au.csiro.data61.aap.etl.library.types.types.IntegerOperations;
import au.csiro.data61.aap.etl.core.filters.BlockVariables;
import au.csiro.data61.aap.etl.core.values.Literal;
import au.csiro.data61.aap.etl.core.filters.TransactionVariables;
import au.csiro.data61.aap.etl.core.readers.ClientConnectionMethod;
import au.csiro.data61.aap.etl.core.values.UserVariables;

/**
 * ExtractTransactionStatistics
 */
public class ExtractTransactionStatistics {
    private static final String URL = "ws://localhost:8546/";
    private static final String FOLDER = "C:/Development/xes-blockchain/v0.2/test_output";
    private static final String[] BLOCK_VARIABLES = {
        BlockVariables.BLOCK_HASH, 
        BlockVariables.BLOCK_NUMBER, 
        BlockVariables.BLOCK_TRANSACTIONS, 
        BlockVariables.BLOCK_GAS_USED
    };
    private static final long START = 6000000l;
    private static final long END = 6001000l;
    
    public static void main(String[] args) {
        try {
            Instruction program = buildProgram();
            ProgramState state = new ProgramState();
            program.execute(state);
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    private static final String TOTAL_EARNINGS = "totalEarnings";
    private static final String TRANSACTION_EARNINGS = "transactionEarnings";
    private static Instruction buildProgram() throws BuildException {
        final ProgramBuilder builder = new ProgramBuilder();
        builder.prepareProgramBuild();

            Method connectionMethod = new ClientConnectionMethod();
            builder.addMethodCall(connectionMethod, Arrays.asList(Literal.stringLiteral(URL)), null);
            builder.addMethodCall(new SetOutputFolderInstruction(), Arrays.asList(Literal.stringLiteral(FOLDER)), null);

            builder.prepareBlockRangeBuild();
                builder.addVariableAssignmentWithIntegerValue(TOTAL_EARNINGS, 0);
                
                builder.prepareTransactionFilterBuild();
                    builder.addMethodCall(
                        IntegerOperations::multiply,
                        Arrays.asList(
                            UserVariables.createValueAccessor(TransactionVariables.TX_GAS_USED), 
                            UserVariables.createValueAccessor(TransactionVariables.TX_GASPRICE)
                        ),
                        UserVariables.createValueMutator(TRANSACTION_EARNINGS)
                    );

                    builder.addMethodCall(
                        IntegerOperations::add, 
                        Arrays.asList(
                            UserVariables.createValueAccessor(TOTAL_EARNINGS), 
                            UserVariables.createValueAccessor(TRANSACTION_EARNINGS)
                        ), 
                        UserVariables.createValueMutator(TOTAL_EARNINGS)
                    );
                builder.buildTransactionFilter(AddressListSpecification.ofAny(), AddressListSpecification.ofAny());
                
                addCsvExport(builder);
            builder.buildBlockRange(BlockNumberSpecification.ofBlockNumber(START), BlockNumberSpecification.ofBlockNumber(END));
        
        return builder.buildProgram();
    }

    private static void addCsvExport(ProgramBuilder builder) throws BuildException {
        final List<String> names = Stream.concat(Arrays.stream(BLOCK_VARIABLES), Stream.of(TOTAL_EARNINGS)).collect(Collectors.toList());
        final List<ValueAccessor> valueAccessors = names.stream()
            .map(name -> UserVariables.createValueAccessor(name))
            .collect(Collectors.toList());
        final Instruction export = new AddCsvRowInstruction("block_statistics", names, valueAccessors);
        builder.addInstruction(export);
    }
}
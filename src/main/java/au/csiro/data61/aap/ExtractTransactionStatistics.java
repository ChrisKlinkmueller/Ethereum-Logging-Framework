package au.csiro.data61.aap;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.csiro.data61.aap.etl.configuration.BuildException;
import au.csiro.data61.aap.etl.configuration.ProgramBuilder;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.Method;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.SetOutputFolderInstruction;
import au.csiro.data61.aap.etl.core.ValueAccessor;
import au.csiro.data61.aap.etl.core.writers.AddCsvRowInstruction;
import au.csiro.data61.aap.etl.library.ConfigurationMethods;
import au.csiro.data61.aap.etl.library.types.types.IntegerOperations;
import au.csiro.data61.aap.etl.library.values.DataSourceVariables;
import au.csiro.data61.aap.etl.library.values.Literal;
import au.csiro.data61.aap.etl.library.values.Variables;

/**
 * ExtractTransactionStatistics
 */
public class ExtractTransactionStatistics {
    private static final String URL = "ws://localhost:8546/";
    private static final String FOLDER = "C:/Development/xes-blockchain/v0.2/test_output";
    private static final String[] BLOCK_VARIABLES = {
        DataSourceVariables.BLOCK_HASH, 
        DataSourceVariables.BLOCK_NUMBER, 
        DataSourceVariables.BLOCK_TRANSACTIONS, 
        DataSourceVariables.BLOCK_GAS_USED
    };
    private static final long START = 6000000l;
    private static final long END = 6000100l;
    
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
    private static Instruction buildProgram() throws BuildException {
        final ProgramBuilder builder = new ProgramBuilder();
        builder.prepareProgramBuild();

            Method connectionMethod = ConfigurationMethods::connectClient;
            builder.addMethodCall(connectionMethod, Arrays.asList(Literal.stringLiteral(URL)), null);
            builder.addMethodCall(new SetOutputFolderInstruction(), Arrays.asList(Literal.stringLiteral(FOLDER)), null);

            builder.prepareBlockRangeBuild();
                addDataSourceVariableInstructions(builder, BLOCK_VARIABLES, DataSourceVariables::createValueCreationInstruction);
                builder.addVariableAssignmentWithIntegerValue(TOTAL_EARNINGS, 0);
                
                builder.prepareTransactionFilterBuild();
                    addDataSourceVariableInstructions(builder, new String[]{DataSourceVariables.TX_VALUE}, DataSourceVariables::createValueCreationInstruction);
                    builder.addMethodCall(
                        IntegerOperations::add, 
                        Arrays.asList(Variables.createValueAccessor(TOTAL_EARNINGS), Variables.createValueAccessor(DataSourceVariables.TX_VALUE)), 
                        Variables.createValueMutator(TOTAL_EARNINGS)
                    );
                    addDataSourceVariableInstructions(builder, new String[]{DataSourceVariables.TX_VALUE}, DataSourceVariables::createValueRemovalInstruction);
                builder.buildTransactionFilter(null, null);
                
                addCsvExport(builder);
                addDataSourceVariableInstructions(builder, BLOCK_VARIABLES, DataSourceVariables::createValueRemovalInstruction);
            builder.buildBlockRange(Literal.integerLiteral(START), Literal.integerLiteral(END));
        
        return builder.buildProgram();
    }

    private static void addCsvExport(ProgramBuilder builder) throws BuildException {
        final List<String> names = Stream.concat(Arrays.stream(BLOCK_VARIABLES), Stream.of(TOTAL_EARNINGS)).collect(Collectors.toList());
        final List<ValueAccessor> valueAccessors = names.stream()
            .map(name -> Variables.createValueAccessor(name))
            .collect(Collectors.toList());
        final Instruction export = new AddCsvRowInstruction("block_statistics", names, valueAccessors);
        builder.addInstruction(export);
    }

    private static void addDataSourceVariableInstructions(ProgramBuilder builder, String[] variables, Function<String, Instruction> instructionCreator) throws BuildException {
        for (String variable : variables) {
            Instruction instruction = instructionCreator.apply(variable);
            builder.addInstruction(instruction);
        }
    }
}
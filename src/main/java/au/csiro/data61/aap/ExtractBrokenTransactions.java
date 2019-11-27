package au.csiro.data61.aap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import au.csiro.data61.aap.etl.configuration.AddressListSpecification;
import au.csiro.data61.aap.etl.configuration.BlockNumberSpecification;
import au.csiro.data61.aap.etl.configuration.BuildException;
import au.csiro.data61.aap.etl.configuration.CsvExportSpecification;
import au.csiro.data61.aap.etl.configuration.GenericFilterPredicateSpecification;
import au.csiro.data61.aap.etl.configuration.MethodSpecification;
import au.csiro.data61.aap.etl.configuration.ProgramBuilder;
import au.csiro.data61.aap.etl.configuration.ValueAccessorSpecification;
import au.csiro.data61.aap.etl.configuration.ValueMutatorSpecification;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;
import au.csiro.data61.aap.etl.core.values.BlockVariables;
import au.csiro.data61.aap.etl.core.values.TransactionVariables;

/**
 * ExtractBrokenTransactions
 */
public class ExtractBrokenTransactions {
    private static final String URL = "ws://localhost:8546/";
    private static final String FOLDER = "C:/Development/xes-blockchain/v0.2/test_output";
    private static final long START = 8000000l;
    private static final long END = 8010000l;
    private static final String BLOCK_FAILUERS = "blockFailures";
    
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
            builder.addVariableAssignment(ValueMutatorSpecification.ofVariableName(BLOCK_FAILUERS), ValueAccessorSpecification.integerLiteral(0l));
            
            builder.prepareTransactionFilterBuild();
                builder.prepareGenericFilterBuild();
                    builder.addMethodCall(
                        MethodSpecification.of("add", "int", "int"), 
                        ValueMutatorSpecification.ofVariableName(BLOCK_FAILUERS),
                        ValueAccessorSpecification.ofVariable(BLOCK_FAILUERS),
                        ValueAccessorSpecification.integerLiteral(1l)
                    );
                    addCsvExport(
                        builder, 
                        "failed_transactions", 
                        TransactionVariables.TX_BLOCKNUMBER, 
                        TransactionVariables.TX_TRANSACTIONINDEX,
                        TransactionVariables.TX_HASH
                    );
                builder.buildGenericFilter(
                    GenericFilterPredicateSpecification.not(  
                        GenericFilterPredicateSpecification.ofBooleanVariable(
                            ValueAccessorSpecification.ofVariable(TransactionVariables.TX_SUCCESS)
                        )                            
                    )
                );
            builder.buildTransactionFilter(AddressListSpecification.ofAny(), AddressListSpecification.ofAny());            
            addCsvExport(builder, "blocks", BlockVariables.BLOCK_NUMBER, BlockVariables.BLOCK_HASH, BlockVariables.BLOCK_TRANSACTIONS, BLOCK_FAILUERS);
        builder.buildBlockRange(
            BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(START)), 
            BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(END))
        );
        return builder.buildProgram();
    }

    private static void addCsvExport(ProgramBuilder builder, String tableName, String... variableNames) throws BuildException {
        final List<String> names = Arrays.asList(variableNames);
        final List<ValueAccessorSpecification> valueAccessors = names.stream()
            .map(name -> ValueAccessorSpecification.ofVariable(name))
            .collect(Collectors.toList());
        builder.addInstruction(CsvExportSpecification.of(tableName, names, valueAccessors));
    }
    
}
package au.csiro.data61.aap;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import au.csiro.data61.aap.elf.configuration.AddressListSpecification;
import au.csiro.data61.aap.elf.configuration.BlockNumberSpecification;
import au.csiro.data61.aap.elf.configuration.BuildException;
import au.csiro.data61.aap.elf.configuration.CsvExportSpecification;
import au.csiro.data61.aap.elf.configuration.GenericFilterPredicateSpecification;
import au.csiro.data61.aap.elf.configuration.MethodSpecification;
import au.csiro.data61.aap.elf.configuration.SpecificationComposer;
import au.csiro.data61.aap.elf.configuration.ValueAccessorSpecification;
import au.csiro.data61.aap.elf.configuration.ValueMutatorSpecification;
import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.values.BlockVariables;
import au.csiro.data61.aap.elf.core.values.TransactionVariables;

/**
 * ExtractUnsuccesfulTransactions
 */
public class ExtractUnsuccesfulTransactions {
    private static final String URL = "ws://localhost:8546/";
    private static final String FOLDER = "C:/Development/xes-blockchain/v0.2/test_output";
    private static final long START = 8700000l;
    private static final long END = 8701000l;
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
        final SpecificationComposer builder = new SpecificationComposer();
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
                        String.format("failed_transactions_%s-%s", START, END), 
                        TransactionVariables.TX_BLOCKNUMBER, 
                        TransactionVariables.TX_TRANSACTIONINDEX,
                        TransactionVariables.TX_HASH,
                        TransactionVariables.TX_FROM,
                        TransactionVariables.TX_TO,
                        TransactionVariables.TX_INPUT,
                        TransactionVariables.TX_CONTRACT_ADRESS,
                        TransactionVariables.TX_GAS_USED,
                        TransactionVariables.TX_GAS,
                        TransactionVariables.TX_GASPRICE,
                        TransactionVariables.TX_VALUE,
                        TransactionVariables.TX_LOGS_BLOOM,
                        TransactionVariables.TX_NONCE,
                        TransactionVariables.TX_V,
                        TransactionVariables.TX_R,
                        TransactionVariables.TX_S
                    );
                builder.buildGenericFilter(
                    GenericFilterPredicateSpecification.not(  
                        GenericFilterPredicateSpecification.ofBooleanVariable(
                            ValueAccessorSpecification.ofVariable(TransactionVariables.TX_SUCCESS)
                        )                            
                    )
                );
            builder.buildTransactionFilter(AddressListSpecification.ofAny(), AddressListSpecification.ofAny());            
            addCsvExport(builder, String.format("blocks_%s-%s", START, END), BlockVariables.BLOCK_NUMBER, BlockVariables.BLOCK_HASH, BlockVariables.BLOCK_TRANSACTIONS, BLOCK_FAILUERS);
        builder.buildBlockRange(
            BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(START)), 
            BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(END))
        );
        return builder.buildProgram();
    }

    private static void addCsvExport(SpecificationComposer builder, String tableName, String... variableNames) throws BuildException {
        final List<String> names = Arrays.asList(variableNames);
        final List<ValueAccessorSpecification> valueAccessors = names.stream()
            .map(name -> ValueAccessorSpecification.ofVariable(name))
            .collect(Collectors.toList());
        builder.addInstruction(CsvExportSpecification.of(tableName, names, valueAccessors));
    }
    
}
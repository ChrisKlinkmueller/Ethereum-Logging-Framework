package au.csiro.data61.aap;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import au.csiro.data61.aap.elf.configuration.AddressListSpecification;
import au.csiro.data61.aap.elf.configuration.BlockNumberSpecification;
import au.csiro.data61.aap.elf.configuration.BuildException;
import au.csiro.data61.aap.elf.configuration.CsvColumnSpecification;
import au.csiro.data61.aap.elf.configuration.CsvExportSpecification;
import au.csiro.data61.aap.elf.configuration.MethodSpecification;
import au.csiro.data61.aap.elf.configuration.SpecificationComposer;
import au.csiro.data61.aap.elf.configuration.ValueAccessorSpecification;
import au.csiro.data61.aap.elf.configuration.ValueMutatorSpecification;
import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;
import au.csiro.data61.aap.elf.core.values.BlockVariables;
import au.csiro.data61.aap.elf.core.values.TransactionVariables;

/**
 * ExtractTransactionStatistics
 */
public class ExtractTransactionStatistics {
    private static final String URL = "ws://localhost:8546/";
    private static final String FOLDER = "C:/Development/xes-blockchain/v0.2/test_output";
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
        final SpecificationComposer builder = new SpecificationComposer();
        builder.prepareProgramBuild();

        builder.addMethodCall(MethodSpecification.of("connect", "string"), ValueAccessorSpecification.stringLiteral(URL));
        builder.addMethodCall(MethodSpecification.of("setOutputFolder", "string"), ValueAccessorSpecification.stringLiteral(FOLDER));
            
            builder.prepareBlockRangeBuild();
                builder.addVariableAssignment(ValueMutatorSpecification.ofVariableName(TOTAL_EARNINGS), ValueAccessorSpecification.integerLiteral(0l));
                
                builder.prepareTransactionFilterBuild();
                    builder.addMethodCall(
                        MethodSpecification.of("multiply", "int", "int"), 
                        ValueMutatorSpecification.ofVariableName(TRANSACTION_EARNINGS),
                        ValueAccessorSpecification.ofVariable(TransactionVariables.TX_GAS_USED),
                        ValueAccessorSpecification.ofVariable(TransactionVariables.TX_GASPRICE)
                    );

                    builder.addMethodCall(
                        MethodSpecification.of("add", "int", "int"), 
                        ValueMutatorSpecification.ofVariableName(TOTAL_EARNINGS),
                        ValueAccessorSpecification.ofVariable(TOTAL_EARNINGS),
                        ValueAccessorSpecification.ofVariable(TRANSACTION_EARNINGS)
                    );
                builder.buildTransactionFilter(AddressListSpecification.ofAny(), AddressListSpecification.ofAny());
                
                addCsvExport(builder);
            builder.buildBlockRange(
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(START)), 
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(END))
            );
        return builder.buildProgram();
    }

    private static void addCsvExport(SpecificationComposer builder) throws BuildException {
        final List<String> names = Arrays.asList(
            BlockVariables.BLOCK_HASH, 
            BlockVariables.BLOCK_NUMBER, 
            BlockVariables.BLOCK_TRANSACTIONS, 
            BlockVariables.BLOCK_GAS_USED,
            BlockVariables.BLOCK_DIFFICULTY,
            BlockVariables.BLOCK_TOTAL_DIFFICULTY,
            TOTAL_EARNINGS
        );
        LinkedList<CsvColumnSpecification> columns = new LinkedList<>();
        for (String name : names) {
            columns.add(CsvColumnSpecification.of(name, ValueAccessorSpecification.ofVariable(name)));
        }

        final CsvExportSpecification export = CsvExportSpecification.of(ValueAccessorSpecification.stringLiteral("block_statistics"), columns);
        builder.addInstruction(export);
    }
}
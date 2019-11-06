package au.csiro.data61.aap.library;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import au.csiro.data61.aap.spec.Variable;
import au.csiro.data61.aap.spec.types.AddressType;
import au.csiro.data61.aap.spec.types.ArrayType;
import au.csiro.data61.aap.spec.types.BoolType;
import au.csiro.data61.aap.spec.types.BytesType;
import au.csiro.data61.aap.spec.types.IntegerType;
import au.csiro.data61.aap.spec.types.SolidityType;
import au.csiro.data61.aap.spec.types.StringType;

/**
 * DefaultVariables
 */
public class DefaultVariables {
    private static final Set<Variable> BLOCK_VARIABLES;
    private static final Set<Variable> TRANSACTION_VARIABLES;
    private static final Set<Variable> LOG_VARIABLES;

    public static Stream<Variable> defaultGlobalVariableStream() {
        return Stream.empty();
    }

    public static Stream<Variable> defaultBlockVariableStream() {
        return BLOCK_VARIABLES.stream();
    }

    public static Stream<Variable> defaultTransactionVariableStream() {
        return TRANSACTION_VARIABLES.stream();
    }

    public static Stream<Variable> defaultLogEntryVariableStream() {
        return LOG_VARIABLES.stream();
    }

    public static Stream<Variable> defaultSmartContractVariableStream() {
        return Stream.empty();
    }

    static {
        BLOCK_VARIABLES = new HashSet<>();
        TRANSACTION_VARIABLES = new HashSet<>();
        LOG_VARIABLES = new HashSet<>();

        addVariable(BLOCK_VARIABLES, IntegerType.DEFAULT_INSTANCE, "block.number");
        addVariable(BLOCK_VARIABLES, BytesType.DEFAULT_INSTANCE, "block.hash");
        addVariable(BLOCK_VARIABLES, BytesType.DEFAULT_INSTANCE, "block.parentHash");
        addVariable(BLOCK_VARIABLES, BytesType.DEFAULT_INSTANCE, "block.nonce");
        addVariable(BLOCK_VARIABLES, BytesType.DEFAULT_INSTANCE, "block.sha3Uncles");
        addVariable(BLOCK_VARIABLES, StringType.DEFAULT_INSTANCE, "block.logsBloom");
        addVariable(BLOCK_VARIABLES, BytesType.DEFAULT_INSTANCE, "block.transactionsRoot");
        addVariable(BLOCK_VARIABLES, BytesType.DEFAULT_INSTANCE, "block.stateRoot");
        addVariable(BLOCK_VARIABLES, BytesType.DEFAULT_INSTANCE, "block.receiptsRoot");
        addVariable(BLOCK_VARIABLES, AddressType.DEFAULT_INSTANCE, "block.miner");
        addVariable(BLOCK_VARIABLES, IntegerType.DEFAULT_INSTANCE, "block.difficulty");
        addVariable(BLOCK_VARIABLES, IntegerType.DEFAULT_INSTANCE, "block.totalDifficulty");
        addVariable(BLOCK_VARIABLES, StringType.DEFAULT_INSTANCE, "block.extraData");
        addVariable(BLOCK_VARIABLES, IntegerType.DEFAULT_INSTANCE, "block.size");
        addVariable(BLOCK_VARIABLES, IntegerType.DEFAULT_INSTANCE, "block.gasLimit");
        addVariable(BLOCK_VARIABLES, IntegerType.DEFAULT_INSTANCE, "block.gasUsed");
        addVariable(BLOCK_VARIABLES, IntegerType.DEFAULT_INSTANCE, "block.imestamp");
        addVariable(BLOCK_VARIABLES, IntegerType.DEFAULT_INSTANCE, "block.transactions");
        addVariable(BLOCK_VARIABLES, new ArrayType(BytesType.DEFAULT_INSTANCE),"block.uncles");

        // TRANSACTION VARIABLES
        addVariable(TRANSACTION_VARIABLES, BytesType.DEFAULT_INSTANCE, "tx.blockHash");
        addVariable(TRANSACTION_VARIABLES, IntegerType.DEFAULT_INSTANCE, "tx.blockNumber");
        addVariable(TRANSACTION_VARIABLES, AddressType.DEFAULT_INSTANCE, "tx.from");
        addVariable(TRANSACTION_VARIABLES, IntegerType.DEFAULT_INSTANCE, "tx.gas");
        addVariable(TRANSACTION_VARIABLES, IntegerType.DEFAULT_INSTANCE, "tx.gasPrice");
        addVariable(TRANSACTION_VARIABLES, BytesType.DEFAULT_INSTANCE, "tx.hash");
        addVariable(TRANSACTION_VARIABLES, StringType.DEFAULT_INSTANCE, "tx.input");
        addVariable(TRANSACTION_VARIABLES, IntegerType.DEFAULT_INSTANCE, "tx.nonce");
        addVariable(TRANSACTION_VARIABLES, AddressType.DEFAULT_INSTANCE, "tx.to");
        addVariable(TRANSACTION_VARIABLES, IntegerType.DEFAULT_INSTANCE, "tx.transactionIndex");
        addVariable(TRANSACTION_VARIABLES, IntegerType.DEFAULT_INSTANCE, "tx.value");
        addVariable(TRANSACTION_VARIABLES, IntegerType.DEFAULT_INSTANCE, "tx.v");
        addVariable(TRANSACTION_VARIABLES, IntegerType.DEFAULT_INSTANCE, "tx.r");
        addVariable(TRANSACTION_VARIABLES, IntegerType.DEFAULT_INSTANCE, "tx.s");

        // LOG ENTRY VARIABLES
        addVariable(LOG_VARIABLES, BoolType.DEFAULT_INSTANCE, "log.removed");
        addVariable(LOG_VARIABLES, IntegerType.DEFAULT_INSTANCE, "log.logIndex");
        addVariable(LOG_VARIABLES, AddressType.DEFAULT_INSTANCE, "log.address");
    }

    private static void addVariable(Set<Variable> variables, SolidityType type, String name) {
        variables.add(new Variable(type, name, true, null));
    }
    


}
package au.csiro.data61.aap.program;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import au.csiro.data61.aap.program.suppliers.BlockchainVariable;
import au.csiro.data61.aap.program.suppliers.Literal;
import au.csiro.data61.aap.program.suppliers.MethodCall;
import au.csiro.data61.aap.program.suppliers.ValueSupplier;
import au.csiro.data61.aap.program.suppliers.Variable;
import au.csiro.data61.aap.program.types.SolidityAddress;
import au.csiro.data61.aap.program.types.SolidityArray;
import au.csiro.data61.aap.program.types.SolidityBytes;
import au.csiro.data61.aap.program.types.SolidityInteger;
import au.csiro.data61.aap.program.types.SolidityString;
import au.csiro.data61.aap.program.types.ValueCasts;
import au.csiro.data61.aap.rpc.EthereumTransaction;
import au.csiro.data61.aap.util.MethodResult;

/**
 * TransactionScope
 */
public class TransactionScope extends Scope {
    public static final Literal ANY = new Literal(SolidityString.DEFAULT_INSTANCE, "any");
    public static final Set<Variable> DEFAULT_VARIABLES;

    private final ValueSupplier senders;
    private final ValueSupplier recipients;
    private final List<BlockchainVariable<EthereumTransaction>> variables;

    @SuppressWarnings("unchecked")
    public TransactionScope(ValueSupplier senders, ValueSupplier recipients) {        
        assert isValidAddressListVariable(senders);
        assert isValidAddressListVariable(recipients);
        this.recipients = recipients;
        this.senders = senders;
        this.variables = DEFAULT_VARIABLES.stream()
            .map(variable -> new BlockchainVariable<EthereumTransaction>((BlockchainVariable<EthereumTransaction>)variable))
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        DEFAULT_VARIABLES.stream().forEach(var -> {
            String name = var.getName().replaceAll("\\.", "_").toUpperCase(); 
            System.out.println(String.format("\taddTransactionVariable(%s, \"\", EthereumTransaction::)", name));
        });
    }

    static {
        DEFAULT_VARIABLES = new HashSet<>();
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, "tx.blockHash",
                EthereumTransaction::getBlockHash, ValueCasts::stringToBytes);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.blockNumber",
                EthereumTransaction::getBlockNumber, ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityAddress.DEFAULT_INSTANCE, "tx.from", EthereumTransaction::getFrom,
                ValueCasts::stringToAddress);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.gas", EthereumTransaction::getGas,
                ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.gasPrice",
                EthereumTransaction::getGasPrice, ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityBytes.DEFAULT_INSTANCE, "tx.hash", EthereumTransaction::getHash,
                ValueCasts::stringToBytes);
        addVariable(DEFAULT_VARIABLES, SolidityString.DEFAULT_INSTANCE, "tx.input", EthereumTransaction::getInput,
                null);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.nonce", EthereumTransaction::getNonce,
                ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityAddress.DEFAULT_INSTANCE, "tx.to", EthereumTransaction::getTo,
                ValueCasts::stringToAddress);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.transactionIndex",
                EthereumTransaction::getTransactionIndex, ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.value", EthereumTransaction::getValue,
                ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.v", EthereumTransaction::getV,
                ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.r", EthereumTransaction::getR,
                ValueCasts::stringToInteger);
        addVariable(DEFAULT_VARIABLES, SolidityInteger.DEFAULT_INSTANCE, "tx.s", EthereumTransaction::getS,
                ValueCasts::stringToInteger);
    }

    public void setEnclosingScope(BlockScope enclosingScope) {
        super.setEnclosingScope(enclosingScope);
    }

    public ValueSupplier getSenders() {
        return this.senders;
    }

    public ValueSupplier getRecipients() {
        return this.recipients;
    }

    public static boolean isValidAddressListVariable(ValueSupplier variable) {
        if (variable == null || variable instanceof MethodCall) {
            return false;
        }

        if (variable == ANY) {
            return true;
        }

        if (variable.getType().conceptuallyEquals(SolidityAddress.DEFAULT_INSTANCE)) {
            return true;
        }
         
        return    variable.getType() instanceof SolidityArray 
               && ((SolidityArray)variable.getType()).getBaseType().conceptuallyEquals(SolidityAddress.DEFAULT_INSTANCE);
    }

    @Override
    public MethodResult<Void> execute(ProgramState state) {
        return MethodResult.ofError("Method not implemented.");
    }

    @Override
    public Stream<? extends Variable> variableStream() {
        return this.variables.stream();
    }

    
}
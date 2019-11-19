package au.csiro.data61.aap.etl.core;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import au.csiro.data61.aap.rpc.EthereumBlock;
import au.csiro.data61.aap.rpc.EthereumTransaction;

/**
 * Instruction
 */
@FunctionalInterface
public interface Instruction {
    public void execute(ProgramState state) throws EtlException;

    //#region MethodCall

    public static Instruction createMethodCall(List<ValueAccessor> parameterAccessors, Method method, ValueMutator resultStorer) {
        assert parameterAccessors != null && parameterAccessors.stream().allMatch(Objects::nonNull);
        assert method != null;
        return state -> callMethod(state, parameterAccessors, method, resultStorer);
    }

    private static Object callMethod(ProgramState state, List<ValueAccessor> parameterAccessors, Method method, ValueMutator resultStorer) throws EtlException {
        Object[] parameterValues = new Object[parameterAccessors.size()];
        for (int i = 0; i < parameterAccessors.size(); i++) {
            parameterValues[i] = parameterAccessors.get(i).getValue(state);
        }

        final Object result = method.call(parameterValues, state);
        
        if (resultStorer != null) {
            resultStorer.setValue(result, state);
        }

        return result;
    }

    //#endregion MethodCall



    //#region Program

    public static Instruction createProgram(Instruction...instructions) {
        return createProgram(Arrays.stream(instructions).collect(Collectors.toList()));
    }

    public static Instruction createProgram(List<Instruction> instructions) {
        assert areValidInstructions(instructions);
        return state -> executeProgram(state, instructions);
    }

    private static void executeProgram(ProgramState state, List<Instruction> instructions)  {
        try {
            for (Instruction instruction : instructions) {
                instruction.execute(state);
            }
            state.endProgram();
        }
        catch (Throwable ex) {
            final String message = "Error when executing the program.";
            state.getExceptionHandler().handleExceptionAndDecideOnAbort(message);
        }
        finally {
            state.close();
        }
    }

    //#endregion Program



    //#region BlockScope 

    public static Instruction createBlockScope(ValueAccessor fromBlock, ValueAccessor toBlock, Instruction... instructions) {
        return createBlockScope(fromBlock, toBlock, Arrays.stream(instructions).collect(Collectors.toList()));
    }

    public static Instruction createBlockScope(ValueAccessor fromBlock, ValueAccessor toBlock, List<Instruction> instructions) {
        assert fromBlock != null;
        assert toBlock != null;
        assert areValidInstructions(instructions);
        return state -> executeBlockScope(state, fromBlock, toBlock, instructions);
    }    

    public static Instruction createBlockVariableHandler(ValueMutator mutator, Function<EthereumBlock, Object> blockValue) {
        assert mutator != null;
        assert blockValue != null;
        return (state) -> {
            final EthereumBlock block = state.getDataSource().getCurrentBlock();
            final Object value = blockValue.apply(block);
            mutator.setValue(value, state);
        };
    }

    private static void executeBlockScope(
        ProgramState state, 
        ValueAccessor fromBlock, 
        ValueAccessor toBlock, 
        List<Instruction> instructions
    ) 
    throws EtlException {
        final LinkedList<EthereumBlock> knownBlocks = new LinkedList<>();
        BigInteger startBlock = (BigInteger)fromBlock.getValue(state);
        BigInteger stopBlock = (BigInteger)toBlock.getValue(state);
        BigInteger currentBlock = startBlock;
        while (currentBlock.compareTo(stopBlock) <= 0) {
            try {
                while (state.getDataSource().getClient().queryBlockNumber().compareTo(currentBlock) < 0) {
                    Thread.sleep(3000);
                }

                final EthereumBlock block = queryConfirmedBlock(state, currentBlock, knownBlocks);
                if (!block.getNumber().equals(currentBlock)) {
                    currentBlock = block.getNumber();
                }

                state.startBlock(currentBlock);
                state.getDataSource().setCurrentBlock(block);
                
                for (Instruction instruction : instructions) { 
                    instruction.execute(state);
                }

                state.endBlock();
            }
            catch (Throwable throwable) {
                final String message = String.format("Error when processing block number '%s'.", currentBlock.toString());
                final boolean abort = state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, throwable);
                if (abort) {
                    return;
                }                
            }
            finally {
                state.getDataSource().setCurrentBlock(null);
            }

            currentBlock = currentBlock.add(BigInteger.ONE);
        }
    }

    public static final int KNOWN_BLOCKS_LENGTH = 30;
    private static EthereumBlock queryConfirmedBlock(ProgramState state, BigInteger currentBlock, LinkedList<EthereumBlock> knownBlocks) throws Throwable {
        BigInteger queryBlockNumber = currentBlock;
        do {
            final EthereumBlock block = state.getDataSource().getClient().queryBlockData(queryBlockNumber);
            if (knownBlocks.isEmpty() || knownBlocks.getLast().getHash().equals(block.getParentHash())) {
                appendBlock(knownBlocks, block);
                return block;
            }

            queryBlockNumber = queryBlockNumber.subtract(BigInteger.ONE);
            knownBlocks.removeLast();
        } while (true);
    }

    public static void appendBlock(LinkedList<EthereumBlock> knownBlocks, EthereumBlock block) {
        knownBlocks.addLast(block);
        if (KNOWN_BLOCKS_LENGTH < knownBlocks.size()) {
            knownBlocks.removeFirst();
        }
    }

    //#endregion BlockScope



    //#region TransactionScope

    public static Instruction createTransactionScope(ValueAccessor senders, ValueAccessor recipients, Instruction... instructions) {
        return createTransactionScope(senders, recipients, Arrays.stream(instructions).collect(Collectors.toList()));
    }

    public static Instruction createTransactionScope(ValueAccessor senders, ValueAccessor recipients, List<Instruction> instructions) {
        assert senders != null;
        assert recipients != null;
        assert areValidInstructions(instructions);

        return (state) -> executeTransactionScope(state, senders, recipients, instructions);
    }

    private static void executeTransactionScope(ProgramState state, ValueAccessor senders, ValueAccessor recipients, List<Instruction> instructions) throws EtlException {
        final Predicate<String> senderFilter = addressFilter(state, senders);
        final Predicate<String> recipientFilter = addressFilter(state, recipients);

        for (EthereumTransaction tx : state.getDataSource().getCurrentBlock()) {
            if (senderFilter.test(tx.getFrom()) && recipientFilter.test(tx.getTo())) {
                try {
                    state.getDataSource().setCurrentTransaction(tx);
                    for (Instruction instruction : instructions) {
                        instruction.execute(state);
                    }
                }
                catch (Throwable cause) {
                    final String message = String.format("Error mapping transaction '%s' in block '%s'.", tx.getTransactionIndex(), tx.getBlockNumber());
                    final boolean abort = state.getExceptionHandler().handleExceptionAndDecideOnAbort(message, cause);
                    if (abort) {
                        throw new EtlException(message, cause);
                    }
                }
                finally {
                    state.getDataSource().setCurrentTransaction(null);
                }
            }
        }
    }

    private static Predicate<String> addressFilter(ProgramState state, ValueAccessor addresses) throws EtlException {
        final Object addressList = addresses.getValue(state);
        if (addressList == null) {
            return address -> true;
        }
        else if (addressList instanceof String) {
            final String expectedAddress = (String)addressList;
            return address -> address != null && address.equals(expectedAddress);
        }
        else if (Set.class.isAssignableFrom(addressList.getClass())) {
            try {
                @SuppressWarnings("unchecked")
                final Set<String> expectedAddresses = (Set<String>)addressList;
                if (expectedAddresses.isEmpty()) {
                    return address -> true;
                }
                else {
                    return address -> expectedAddresses.contains(address);
                }
            }
            catch (ClassCastException ex) {
                final String message = "Cannot convert address list.";
                throw new EtlException(message, ex);
            }
        }
        else {
            throw new EtlException(String.format("Value of type '%s' for addresslist not supported.", addressList.getClass()));
        }
    }


    //#endregion TransactionScope


    //#region Assertions

    private static boolean areValidInstructions(List<Instruction> instructions) {
        return instructions != null && instructions.stream().allMatch(Objects::nonNull);
    }

    //#endregion Assertions
}
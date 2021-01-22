package blf.blockchains.hyperledger.variables;

import blf.core.values.Variable;

import java.util.HashSet;
import java.util.Set;

/**
 * TransactionVariables
 */
public class HyperledgerTransactionVariables {
    static final Set<Variable> TRANSACTION_VARIABLES;

    public static final String TRANSACTION_ID = "transaction.id";
    public static final String TRANSACTION_CREATOR_MSPID = "transaction.creatorMspid";
    public static final String TRANSACTION_CREATOR_ID = "transaction.creatorId";
    public static final String TRANSACTION_PEER_NAME = "transaction.peerName";
    public static final String TRANSACTION_PEER_HASH = "transaction.peerHash";
    public static final String TRANSACTION_PEER_URL = "transaction.peerUrl";
    public static final String TRANSACTION_CHAINCODE_ID = "transaction.chaincodeId";
    public static final String TRANSACTION_RESPONSE_MESSAGE = "transaction.responseMessage";
    public static final String TRANSACTION_RESPONSE_STATUS = "transaction.responseStatus";
    public static final String TRANSACTION_ENDORSEMENT_COUNT = "transaction.endorsementCount";

    private HyperledgerTransactionVariables() {}

    private static final String TYPE_STRING = "string";
    @SuppressWarnings("unused")
    private static final String TYPE_BOOL = "bool";
    private static final String TYPE_INT = "int";
    @SuppressWarnings("unused")
    private static final String TYPE_BYTES = "bytes";
    @SuppressWarnings("unused")
    private static final String TYPE_ADDRESS = "address";

    static {
        TRANSACTION_VARIABLES = new HashSet<>();

        addTransactionVariable(TRANSACTION_ID, TYPE_INT);
        addTransactionVariable(TRANSACTION_CREATOR_MSPID, TYPE_INT);
        addTransactionVariable(TRANSACTION_CREATOR_ID, TYPE_INT);
        addTransactionVariable(TRANSACTION_PEER_NAME, TYPE_STRING);
        addTransactionVariable(TRANSACTION_PEER_HASH, TYPE_STRING);
        addTransactionVariable(TRANSACTION_PEER_URL, TYPE_STRING);
        addTransactionVariable(TRANSACTION_CHAINCODE_ID, TYPE_STRING);
        addTransactionVariable(TRANSACTION_RESPONSE_MESSAGE, TYPE_STRING);
        addTransactionVariable(TRANSACTION_RESPONSE_STATUS, TYPE_STRING);
        addTransactionVariable(TRANSACTION_ENDORSEMENT_COUNT, TYPE_INT);
    }

    private static void addTransactionVariable(String name, String type) {
        TRANSACTION_VARIABLES.add(new Variable(name, type, state -> state.getValueStore().getValue(name)));
    }
}

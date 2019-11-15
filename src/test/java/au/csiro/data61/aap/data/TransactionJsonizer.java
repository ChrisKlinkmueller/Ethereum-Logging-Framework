package au.csiro.data61.aap.data;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import au.csiro.data61.aap.rpc.EthereumTransaction;

/**
 * TransactionJsonizer
 */
public class TransactionJsonizer {
    private static final String INPUT = "input";
    private static final String R = "r";
    private static final String S = "s";
    private static final String V = "v";
    private static final String GAS = "gas";
    private static final String GAS_PRICE = "gasPrice";
    private static final String NONCE = "nonce";
    private static final String TO = "to";
    private static final String VALUE = "value";
    private static final String FROM = "from";
    private static final String HASH = "hash";
    private static final String LOG_ENTRIES = "logEntries";
    private static final String TRANSACTION_INDEX = "transactionIndex";

    
    public static class TransactionSerializer extends StdSerializer<EthereumTransaction> {
        private static final long serialVersionUID = -4264501643169092514L;

        public TransactionSerializer() {
            this(null);
        }

        public TransactionSerializer(Class<EthereumTransaction> t) {
            super(t);
        }

        @Override
        public void serialize(EthereumTransaction tx, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            gen.writeStringField(FROM, tx.getFrom());
            gen.writeStringField(GAS, tx.getGas().toString());
            gen.writeStringField(GAS_PRICE, tx.getGasPrice().toString());
            gen.writeStringField(HASH, tx.getHash());
            gen.writeStringField(INPUT, tx.getInput());
            gen.writeStringField(NONCE, tx.getNonce().toString());
            gen.writeObjectField(TRANSACTION_INDEX, tx.getTransactionIndex());
            gen.writeStringField(TO, tx.getTo());
            gen.writeStringField(VALUE, tx.getValue().toString());
            gen.writeStringField(V, tx.getV().toString());
            gen.writeStringField(R, tx.getR());
            gen.writeStringField(S, tx.getS());

            gen.writeArrayFieldStart(LOG_ENTRIES);
            for (int i = 0; i < tx.logCount(); i++) {
                gen.writeObject(tx.getLog(i));
            }
            gen.writeEndArray();

            gen.writeEndObject();
        }

        
    }
    
}
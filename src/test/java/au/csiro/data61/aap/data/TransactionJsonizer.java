package au.csiro.data61.aap.data;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import au.csiro.data61.aap.data.JsonUtil.EthereumJsonDeserializer;
import au.csiro.data61.aap.elf.core.readers.EthereumTransaction;
import au.csiro.data61.aap.elf.core.readers.RawLogEntry;
import au.csiro.data61.aap.elf.core.readers.RawTransaction;

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
            super(EthereumTransaction.class);
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
    
    public static class TransactionDeserializer extends EthereumJsonDeserializer<RawTransaction> {
        private static final long serialVersionUID = 3840861880467686269L;

        public TransactionDeserializer() {
            super(RawTransaction.class);
            this.addExtractor(FROM, JsonUtil.getStringExtractor(RawTransaction::setFrom));
            this.addExtractor(GAS, JsonUtil.getIntegerExtractor(RawTransaction::setGas));
            this.addExtractor(GAS_PRICE, JsonUtil.getIntegerExtractor(RawTransaction::setGasPrice));
            this.addExtractor(HASH, JsonUtil.getStringExtractor(RawTransaction::setHash));
            this.addExtractor(INPUT, JsonUtil.getStringExtractor(RawTransaction::setInput));
            this.addExtractor(NONCE, JsonUtil.getIntegerExtractor(RawTransaction::setNonce));
            this.addExtractor(TRANSACTION_INDEX, JsonUtil.getIntegerExtractor(RawTransaction::setTransactionIndex));
            this.addExtractor(TO, JsonUtil.getStringExtractor(RawTransaction::setTo));
            this.addExtractor(VALUE, JsonUtil.getIntegerExtractor(RawTransaction::setValue));
            this.addExtractor(V, JsonUtil.getIntegerExtractor(RawTransaction::setV));
            this.addExtractor(R, JsonUtil.getStringExtractor(RawTransaction::setR));
            this.addExtractor(S, JsonUtil.getStringExtractor(RawTransaction::setS));
            this.addExtractor(LOG_ENTRIES, JsonUtil.getListExtractor(this::addLogs, RawLogEntry.class));
        }

        private void addLogs(RawTransaction tx, List<RawLogEntry> entries) {
            entries.forEach(e -> {
                tx.addLog(e);
                e.setTransaction(tx);
            });
        }

        @Override
        protected RawTransaction createObject() {
            return new RawTransaction();
        }
        
    }
}
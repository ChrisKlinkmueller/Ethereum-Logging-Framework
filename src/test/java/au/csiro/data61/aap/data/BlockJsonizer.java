package au.csiro.data61.aap.data;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import au.csiro.data61.aap.rpc.EthereumBlock;

/**
 * BlockJsonizer
 */
class BlockJsonizer {
    private static final String GAS_USED = "gasUsed";
    private static final String GAS_LIMIT = "gasLimit";
    private static final String EXTRA_DATA = "extraData";
    private static final String DIFFICULTY = "difficulty";
    private static final String RECEIPTS_ROOT = "receiptsRoot";
    private static final String PARENT_HASH = "parentHash";
    private static final String NUMBER = "number";
    private static final String NONCE = "nonce";
    private static final String MINER = "miner";
    private static final String LOGS_BLOOM = "logsBloom";
    private static final String HASH = "hash";
    private static final String TRANSACTIONS = "transactions";
    private static final String TRANSACTION_ROOT = "transactionRoot";
    private static final String TOTAL_DIFFICULTY = "totalDifficulty";
    private static final String TIMESTAMP = "timestamp";
    private static final String STATE_ROOT = "stateRoot";
    private static final String SIZE = "size";
    private static final String SHA3_UNCLE = "sha3Uncle";

    public static class BlockSerializer extends StdSerializer<EthereumBlock> {
        private static final long serialVersionUID = 1L;

        public BlockSerializer() {
            this(null);
        }

        public BlockSerializer(Class<EthereumBlock> t) {
            super(t);
        }

        @Override
        public void serialize(EthereumBlock block, JsonGenerator gen, SerializerProvider serializer) throws IOException {
            gen.writeStartObject();

            gen.writeStringField(DIFFICULTY, block.getDifficulty().toString());
            gen.writeStringField(EXTRA_DATA, block.getExtraData());
            gen.writeStringField(GAS_LIMIT, block.getGasLimit().toString());
            gen.writeStringField(GAS_USED, block.getGasUsed().toString());
            gen.writeStringField(HASH, block.getHash());
            gen.writeStringField(LOGS_BLOOM, block.getLogsBloom());
            gen.writeStringField(MINER, block.getMiner());
            gen.writeStringField(NONCE, block.getNonce().toString());
            gen.writeStringField(NUMBER, block.getNumber().toString());
            gen.writeStringField(PARENT_HASH, block.getParentHash());
            gen.writeStringField(RECEIPTS_ROOT, block.getReceiptsRoot());
            gen.writeStringField(SHA3_UNCLE, block.getSha3uncles());
            gen.writeStringField(SIZE, block.getSize().toString());
            gen.writeStringField(STATE_ROOT, block.getStateRoot());
            gen.writeStringField(TIMESTAMP, block.getTimestamp().toString());
            gen.writeStringField(TOTAL_DIFFICULTY, block.getTotalDifficulty().toString());
            gen.writeStringField(TRANSACTION_ROOT, block.getTransactionsRoot());
            
            gen.writeArrayFieldStart(TRANSACTIONS);
            for (int i = 0; i < block.transactionCount(); i++) {
                gen.writeObject(block.getTransaction(i));
            }
            gen.writeEndArray();

            gen.writeEndObject();
        }
    }
}
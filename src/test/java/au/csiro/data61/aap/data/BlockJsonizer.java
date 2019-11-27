package au.csiro.data61.aap.data;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import au.csiro.data61.aap.data.JsonUtil.EthereumJsonDeserializer;
import au.csiro.data61.aap.elf.core.readers.EthereumBlock;
import au.csiro.data61.aap.elf.core.readers.RawBlock;
import au.csiro.data61.aap.elf.core.readers.RawTransaction;

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
    private static final String UNCLES = "uncles";

    public static class BlockSerializer extends StdSerializer<EthereumBlock> {
        private static final long serialVersionUID = 1L;

        public BlockSerializer() {
            super(EthereumBlock.class);
        }

        @Override
        public void serialize(EthereumBlock block, JsonGenerator gen, SerializerProvider serializer)
                throws IOException {
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

            gen.writeArrayFieldStart(UNCLES);
            for (int i = 0; i < block.getUncles().size(); i++) {
                gen.writeString(block.getUncles().get(i));
            }
            gen.writeEndArray();

            gen.writeArrayFieldStart(TRANSACTIONS);
            for (int i = 0; i < block.transactionCount(); i++) {
                gen.writeObject(block.getTransaction(i));
            }
            gen.writeEndArray();

            gen.writeEndObject();
        }
    }

    public static class BlockDeserializer extends EthereumJsonDeserializer<RawBlock> {
        private static final long serialVersionUID = -1896860192129257034L;

        public BlockDeserializer() {
            super(RawBlock.class);
            this.addExtractor(NUMBER, JsonUtil.getIntegerExtractor(RawBlock::setNumber));
            this.addExtractor(HASH, JsonUtil.getStringExtractor(RawBlock::setHash));
            this.addExtractor(PARENT_HASH, JsonUtil.getStringExtractor(RawBlock::setParentHash));
            this.addExtractor(NONCE, JsonUtil.getIntegerExtractor(RawBlock::setNonce));
            this.addExtractor(SHA3_UNCLE, JsonUtil.getStringExtractor(RawBlock::setSha3uncles));
            this.addExtractor(LOGS_BLOOM, JsonUtil.getStringExtractor(RawBlock::setLogsBloom));
            this.addExtractor(TRANSACTION_ROOT, JsonUtil.getStringExtractor(RawBlock::setTransactionsRoot));
            this.addExtractor(STATE_ROOT, JsonUtil.getStringExtractor(RawBlock::setStateRoot));
            this.addExtractor(RECEIPTS_ROOT, JsonUtil.getStringExtractor(RawBlock::setReceiptsRoot));
            this.addExtractor(MINER, JsonUtil.getStringExtractor(RawBlock::setMiner));
            this.addExtractor(DIFFICULTY, JsonUtil.getIntegerExtractor(RawBlock::setDifficulty));
            this.addExtractor(TOTAL_DIFFICULTY, JsonUtil.getIntegerExtractor(RawBlock::setTotalDifficulty));
            this.addExtractor(EXTRA_DATA, JsonUtil.getStringExtractor(RawBlock::setExtraData));
            this.addExtractor(SIZE, JsonUtil.getIntegerExtractor(RawBlock::setSize));
            this.addExtractor(GAS_LIMIT, JsonUtil.getIntegerExtractor(RawBlock::setGasLimit));
            this.addExtractor(GAS_USED, JsonUtil.getIntegerExtractor(RawBlock::setGasUsed));
            this.addExtractor(TIMESTAMP, JsonUtil.getIntegerExtractor(RawBlock::setTimestamp));
            this.addExtractor(TRANSACTIONS, JsonUtil.getListExtractor(this::setTransactions, RawTransaction.class));
            this.addExtractor(UNCLES, JsonUtil.getListExtractor(RawBlock::setUncles, String.class));
        }

        private void setTransactions(RawBlock block, List<RawTransaction> transactions) {
            transactions.forEach(tx -> {
                block.addTransaction(tx);
                tx.setBlock(block);
            });
        }

        @Override
        protected RawBlock createObject() {
            return new RawBlock();
        }
    }
}
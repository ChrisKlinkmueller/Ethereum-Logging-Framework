package au.csiro.data61.aap.data;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import au.csiro.data61.aap.data.BlockJsonizer.BlockSerializer;
import au.csiro.data61.aap.data.TransactionJsonizer.TransactionSerializer;
import au.csiro.data61.aap.data.LogEntryJsonizer.LogEntrySerializer;
import au.csiro.data61.aap.rpc.EthereumBlock;
import au.csiro.data61.aap.rpc.EthereumClient;
import au.csiro.data61.aap.rpc.EthereumLog;
import au.csiro.data61.aap.rpc.EthereumTransaction;

/**
 * TestDataExtractor
 */
public class TestDataExtractor {
    private static final String FILE_NAME = "C:/Development/xes-blockchain/v0.2/src/test/resources/block_data.json";
    private static final long START_BLOCK = 6000000;
    private static final long BLOCKS = 1000;


    public static void main(String[] args) {
        final EthereumClient client = createClient();
        if (client == null) {
            return;
        }

        final List<EthereumBlock> blocks = queryBlocks(client);
        if (blocks == null) {
            client.close();
            return;
        }

        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule("Serializers");
            module.addSerializer(EthereumBlock.class, new BlockSerializer(EthereumBlock.class));
            module.addSerializer(EthereumTransaction.class, new TransactionSerializer(EthereumTransaction.class));
            module.addSerializer(EthereumLog.class, new LogEntrySerializer(EthereumLog.class));
            objectMapper.registerModule(module);

            objectMapper.writeValue(new File(FILE_NAME), blocks);
        } catch (IOException e) {
            e.printStackTrace();
        }

        client.close();
    }

    private static List<EthereumBlock> queryBlocks(EthereumClient client) {
        final List<EthereumBlock> blocks = new ArrayList<>();

        for (long blockNumber = START_BLOCK; blockNumber < START_BLOCK + BLOCKS; blockNumber++) {
            try {
                final EthereumBlock block = client.queryBlockData(BigInteger.valueOf(blockNumber));
                blocks.add(block);
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }

        return blocks;
    }

    private static EthereumClient createClient() {
        try {
            return new EthereumClient();
        } catch (ConnectException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    
}
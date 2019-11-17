package au.csiro.data61.aap.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.csiro.data61.aap.rpc.EthereumBlock;
import au.csiro.data61.aap.rpc.EthereumClient;
import au.csiro.data61.aap.rpc.RawBlock;
import au.csiro.data61.aap.rpc.Web3jClient;

/**
 * TestDataExtractor
 */
public class TestDataExtractor {
    private static final String FILE_NAME = "C:/Development/xes-blockchain/v0.2/src/test/resources/block_data.json";
    private static final long START_BLOCK = 6000000;
    private static final long BLOCKS = 100;

    public static void main(String[] args) {
        
        
    }

    public static List<EthereumBlock> readBlock() {
        final String content = readContent();
        if (content == null) {
            return null;
        }

        final ObjectMapper mapper = JsonUtil.buildObjectMapper();
        try {
            List<RawBlock> blocks = mapper.readValue(content, new TypeReference<List<RawBlock>>() {});
            return blocks.stream().map(b -> (EthereumBlock)b).collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void exportBlocks() {
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
            final ObjectMapper objectMapper = JsonUtil.buildObjectMapper();
            objectMapper.writeValue(new File(FILE_NAME), blocks); 
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
        
        client.close();
        
    }

    private static String readContent() {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(FILE_NAME)))){
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }        
        return null;
    }

    private static List<EthereumBlock> queryBlocks(EthereumClient client) {
        final List<EthereumBlock> blocks = new ArrayList<>();

        for (long blockNumber = START_BLOCK; blockNumber < START_BLOCK + BLOCKS; blockNumber++) {
            try {
                final EthereumBlock block = client.queryBlockData(BigInteger.valueOf(blockNumber));
                blocks.add(block);
            } catch (Throwable ex) {
                ex.printStackTrace();
                return null;
            }
        }

        return blocks;
    }

    private static EthereumClient createClient() {
        try {
            return new Web3jClient();
        } catch (ConnectException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    
}
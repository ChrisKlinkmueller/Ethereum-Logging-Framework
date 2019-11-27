package au.csiro.data61.aap;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.core.methods.request.Transaction;

import au.csiro.data61.aap.elf.core.readers.Web3jClient;


/**
 * ExtractAugurContracts
 */
public class ExtractAugurContracts {
    private static final String URL = "ws://localhost:8546/";
    public static void main(String[] args) throws Throwable {
        final Web3jClient client = new Web3jClient(URL);
        final HashSet<String> knownAddress = new HashSet<>();
        BigInteger currentBlock = new BigInteger("7926227");
        BigInteger lastBlock = new BigInteger("8000000");

        try {            
            while (currentBlock.compareTo(lastBlock) <= 0) {
                for (String contract : contracts) {
                    byte[] bytes = contract.getBytes();
                    byte[] bytes32 = Arrays.copyOf(bytes, 32);
                    Bytes32 type = new Bytes32(bytes32);
                    
                    Function function = new Function("lookup", Arrays.asList(type), Arrays.asList(TypeReference.makeTypeReference("address")));
                    String data = FunctionEncoder.encode(function);
                    Transaction tx = Transaction.createEthCallTransaction("0xb3337164e91b9f05c87c7662c7ac684e8e0ff3e7", "0xb3337164e91b9f05c87c7662c7ac684e8e0ff3e7", data);
                    String result = client.ethCall(tx, currentBlock).toString();
                    
                    if (!result.toString().equals("0x0000000000000000000000000000000000000000000000000000000000000000")) {
                        if (!knownAddress.contains(result)) {
                            knownAddress.add(result);
                            System.out.println(String.format("Block %s: New %s contract deployed and registered with address '%s'.", currentBlock, contract, result));
                        }
                    }

                }

                currentBlock = currentBlock.add(BigInteger.ONE);
            }
        }
        finally {
            client.close();
        }
    }

    private static final List<String> contracts = Arrays.asList("Cash", "CompleteSets", "CreateOrder", "DisputeCrowdsourcerFactory", 
        "FeeWindow", "FeeWindowFactory", "FeeToken", "FeeTokenFactory", "FillOrder", "InitialReporter", "InitialReporterFactory", 
        "LegacyReputationToken", "Mailbox", "MailboxFactory", "Map", "MapFactory", "Market", "MarketFactory", "Orders", 
        "OrdersFetcher", "RepPriceOracle", "ReputationToken", "ReputationTokenFactory", "ShareToken", "ShareTokenFactory", 
        "Time", "Universe", "UniverseFactory"); 
}
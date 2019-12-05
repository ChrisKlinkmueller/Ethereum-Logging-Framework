package au.csiro.data61.aap;

/**
 * ExtractRobinHoodCoin
 */
public class ExtractRobinHoodCoin {
    // private static final String CONTRACT = "0x6db4b40c3bbbac643394574504f130f5e9ce3d05";
    // private static final String URL = "ws://localhost:8546/";
    // private static final BigInteger CURRENT_BLOCK = new BigInteger("9051600");
    
    // public static void main(String[] args) throws Throwable {
    //     final Web3jClient client = new Web3jClient(URL);
       

    //     try {   
    //         System.out.println("Name:");
    //         retrieve(client, "name", Collections.emptyList(), Arrays.asList(TypeReference.makeTypeReference("string")))
    //             .forEach(obj -> System.out.println(((Type<?>)obj).getValue()));
    //         System.out.println();

    //         System.out.println("Total Supply:");
    //         retrieve(client, "totalSupply", Collections.emptyList(), Arrays.asList(TypeReference.makeTypeReference("uint256")))
    //             .forEach(obj -> System.out.println(((Type<?>)obj).getValue()));
    //         System.out.println();
    //     }
    //     finally {
    //         client.close();
    //     }
    // }

    // @SuppressWarnings("all")
    // public static List<?> retrieve(Web3jClient client, String name, List<Type> inputParameters, List<TypeReference<?>> returnTypes) throws Throwable {
        
    //     Function function = new Function(name, inputParameters, returnTypes);
    //     String data = FunctionEncoder.encode(function);
    //     Transaction tx = Transaction.createEthCallTransaction(CONTRACT, CONTRACT, data);
    //     EthCall result = client.ethCall(tx, CURRENT_BLOCK);
    //     return FunctionReturnDecoder.decode(result.getResult(), returnTypes.stream().map(t -> (TypeReference<Type>)t).collect(Collectors.toList()));
        
    //     return null;
    // }
}
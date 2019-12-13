# Ethereum Logging Framework

This framework provides advanced logging capabilities for Ethereum applications on top of Web3J / Ethereum's JSON RPC. It consists of four components:

- Ethql: a query language for data on Ethereum
- Validator: a component to check ethql documents for specification erros
- Extractor: a component to extract, transform and format data based on ethql
- Generator: a component to create efficient logging functionality that can be embedded into smart contracts

Example ethql documents include 
- [AugurContractRegistry.ethql](./src/main/resources/AugurContractRegistry.ethql)
- [CryptoKitties.ethql](./src/main/resources/CryptoKitties.ethql)
- [GeneratorGitExample.ethql](./src/main/resources/GeneratorGitExample.ethql)
- [GeneratorGitExample.ethql](./src/main/resources/GeneratorShirtExample.ethql)
- [NetworkStatistics.ethql](./src/main/resources/NetworkStatistics.ethql)

Code demonstrating the use of the componentscan be found here
- [Validator](src/main/java/au/csiro/data61/aap/samples/ValidatorTest.java)
- [Extractor](src/main/java/au/csiro/data61/aap/samples/ExtractorTest.java)
- [Generator](src/main/java/au/csiro/data61/aap/samples/GeneratorTest.java)

## Publications

Details of the framework are described in more detail in the following publication:

[C. Klinkmüller, A. Ponomarev, A.B. Tran, I. Weber, W. van der Aalst (2019)](https://www.researchgate.net/publication/335399009_Mining_Blockchain_Processes_Extracting_Process_Mining_Data_from_Blockchain_Applications): "Mining Blockchain Processes: Extracting Process Mining Data from Blockchain Applications". In: 17th International Conference on Business Process Management (Blockchain Forum).

## License

This software is released under the CSIRO Open Source Software Licence Agreement. Details can be found [LICENSE.md](LICENSE.md). Moreover the third party components distributed with the software and their licenses are listed in [NOTICE.md](NOTICE.md).

Generated code has been tested with Solidity 0.5.10.
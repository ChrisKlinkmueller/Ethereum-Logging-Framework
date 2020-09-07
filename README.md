# Ethereum Logging Framework


This framework provides advanced logging capabilities for Ethereum applications on top of Web3J / Ethereum's JSON RPC. It consists of four components:

- Ethql: a query language for data on Ethereum
- Validator: a component to check ethql documents for specification erros
- Extractor: a component to extract, transform and format data based on ethql
- Generator: a component to create efficient logging functionality that can be embedded into smart contracts

![](framework.png)

Example ethql documents include
- [AugurContractRegistry.ethql](./src/main/resources/AugurContractRegistry.ethql)
- [CryptoKitties.ethql](./src/main/resources/CryptoKitties.ethql)
- [GeneratorShirtExample.ethql](./src/main/resources/GeneratorGitExample.ethql)
- [GeneratorGitExample.ethql](./src/main/resources/GeneratorShirtExample.ethql)
- [NetworkStatistics.ethql](./src/main/resources/NetworkStatistics.ethql)
- Scripts contributed by Hendrik Bockrath
  - [Bockrath_Forsage.ethql](./src/main/resources/Bockrath_Forsage.ethql)
  - [Bockrath_Raiden.ethql](./src/main/resources/Bockrath_Raiden.ethql)
- Scripts contributed by Martin Rebesky
  - [Rebesky_Augur.ethql](./src/main/resources/Rebesky_Augur.ethql)
  - [Rebesky_ChickenHunt.ethql](./src/main/resources/Rebesky_ChickenHunt.ethql)
  - [Rebesky_Idex1.ethql](./src/main/resources/Rebesky_Idex1.ethql)
  - [Rebesky_Idex2.ethql](./src/main/resources/Rebesky_Idex2.ethql)
  - [Rebesky_Idex3.ethql](./src/main/resources/Rebesky_Idex3.ethql)

Code demonstrating the use of the component scan be found here
- [Validator](src/main/java/au/csiro/data61/aap/samples/ValidatorTest.java)
- [Extractor](src/main/java/au/csiro/data61/aap/samples/ExtractorTest.java)
- [Generator](src/main/java/au/csiro/data61/aap/samples/GeneratorTest.java) (the generated code has been tested with Solidity 0.5.10)

## Publications

Details of the framework are described in more detail in the following publication:

[C. Klinkmüller, A. Ponomarev, A.B. Tran, I. Weber, W. van der Aalst (2019)](https://www.researchgate.net/publication/335399009_Mining_Blockchain_Processes_Extracting_Process_Mining_Data_from_Blockchain_Applications): "Mining Blockchain Processes: Extracting Process Mining Data from Blockchain Applications". In: 17th International Conference on Business Process Management (Blockchain Forum).

[C. Klinkmüller, I. Weber, A. Ponomarev, A.B. Tran, W. van der Aalst (2020)](https://arxiv.org/abs/2001.10281): Efficient Logging for Blockchain Applications. 	arXiv:2001.10281.

## Build

1. Ensure Java 13 is installed locally and the env var `JAVA_HOME` is properly set.

To check whether Java has been set up correctly, run
```bash
mvn --version
```

2. To build the project, run
```bash
cd ethereum-logging-framework
mvn verify
```

Now you're ready to develop!

## Test

To run unit tests, run
```bash
mvn test
```

## License

This software is released under the CSIRO Open Source Software Licence Agreement. Details can be found [LICENSE.md](LICENSE.md). Moreover the third party components distributed with the software and their licenses are listed in [NOTICE.md](NOTICE.md).



# Ethereum Logging Framework (ELF)

**Note:** The [Blockchain Logging Framework](https://github.com/TU-ADSP/Blockchain-Logging-Framework) is an extension of ELF that provides logging capabilities for DApps on any blockchain platform and that currently supports Ethereum and HyperLedger Fabric.

**Note:** The open [collection of resources for process mining on blockchain data](https://ingo-weber.github.io/dapp-data/) contains event logs extracted with ELF from DApps deployed on public Ethereum.

## Capabilities

This framework provides advanced logging capabilities for Ethereum applications on top of Web3J / Ethereum's JSON RP. It is essentially a lightweight ETL engine for extracting and sharing network and application specific data stored on Ethereum with off-chain components. In its current version the framework supports a broad range of use cases, e.g., extraction of data for business-level analysis of DApps, monitoring of relationships and states of individual smart contracts, or event-based streaming of data to other off-chain components.

The main features of the framework include:
- A query language that abstracts from technical details and hence minimizes configuration effort
- Syntactic and semantic validation of user-defined queries
- Extraction of standard attributes (e.g., block hash, gas price, gas used, etc.) from all confirmed blocks and of application specific data including log entry parameters, input parameters for transactions, or smart contract states
- Definition of arbitrary data filters and basic transformation operations
- [XES Certified](https://www.tf-pm.org/news/ethereum-logging-framework-elf-0-2-1-has-been-xes-certified) by the IEEE Task Force on Process Mining as an export-only/producer tool 
- A variety of target data formats including CSV, textual logs, and XES for which it is certified by the IEEE. 
- Two export modes: batch, i.e., extracting all data at once, vs. streaming, i.e., extracting data block by block, potentially endlessly
- Generation of cost efficient Solidity code for logging from user defined queries. The generated code compresses log entry attributes before emitting a log entry and it can be manually integrated into custom smart contract code.

## Architecture

The architecture of the framework consists of four components:
- [Ethql](./src/main/antlr4/au/csiro/data61/aap/elf/parsing/Ethql.g4): a query language for data on Ethereum
- [Validator](./src/main/java/au/csiro/data61/aap/elf/Validator.java): a component to check ethql documents for specification erros
- [Extractor](./src/main/java/au/csiro/data61/aap/elf/Extractor.java): a component to extract, transform and format data based on ethql
- [Generator](./src/main/java/au/csiro/data61/aap/elf/Generator.java): a component to create efficient logging functionality that can be embedded into smart contracts

![](framework.png)

## Example Queries

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

[C. Klinkmüller, I. Weber, A. Ponomarev, A.B. Tran, W. van der Aalst (2020)](https://arxiv.org/abs/2001.10281): "Efficient Logging for Blockchain Applications". arXiv:2001.10281.

[R. Hobeck, C. Klinkmüller, D. Bandara, I. Weber, W. van der Aalst (2021)](https://easychair.org/publications/preprint/HDkV): "Process Mining on Blockchain Data: a Case Study of Augur". In: 19th International Conference on Business Process Management.

P. Beck,  H. Bockrath, T. Knoche, M. Digtiar, T. Petrich, D. Romanchenko, R. Hobeck, L. Pufahl, C. Klinkmüller, I. Weber (2021): "BLF: A Blockchain Logging Framework for MiningBlockchain Data". In: 19th International Conference on Business Process Management (Demo & Resource Track).

D. Bandara, H. Bockrath, R. Hobeck, C. Klinkmüller, L. Pufahl, M. Rebesky, W. van der Aalst, I. Weber (2021): "Event Logs of Ethereum-Based Applications -- A Collection of Resources for Process Mining on Blockchain Data". In: 19th International Conference on Business Process Management (Demo & Resource Track).

## Build

1. Ensure Java 13 or higher is installed locally and the env var `JAVA_HOME` is properly set. [AdoptOpenJDK](https://adoptopenjdk.net/installation.html) provides binaries and installation guides for various operating systems.

2. Install Apache Maven. Distribution archive and installation guidelines are available at [maven.apache.org](https://maven.apache.org/index.html).

3. To check whether Java and Maven have been set up correctly, run
    ```bash
    mvn --version
    ```

4. To setup the ```lib``` folder as a local repository, run
    ```bash
    cd lib
    mvn deploy:deploy-file "-Durl=file:///<project-path>/lib" "-Dfile=Spex.jar" "-DgroupId=org.deckfour" "-DartifactId=spex" "-Dversion=1.0" "-Dpackaging=jar"
    mvn deploy:deploy-file "-Durl=file:///<project-path>/lib" "-Dfile=OpenXES-20181205.jar" "-DgroupId=org.deckfour" "-DartifactId=open-xes" "-Dversion=1.0" "-Dpackaging=jar"
    ```
   
6. To build the project, run
    ```bash
    cd ethereum-logging-framework
    mvn verify
    ```
    - To build the executable jar ```elf-cmd.jar```, add the option
      ```bash
      "-Delf.skip.assemble=false"
      ``` 
    - To skip testing, add the option
      ```bash
      "-DskipTests"
      ``` 

Now you're ready to develop!

## Test

To run unit tests, run
```bash
mvn test
```

## Execute ELF

The compiled jar ```elf-cmd.jar``` can be executed from the command line in different modes:
1. *Validator:*  ```java -jar elf-cmd.jar validate <PATH_TO_SCRIPT> (-errors|-full)?``` executes the validator on the specified script. There are two options for validation: whereas ```-errors``` only reports specification errors, the default option is ```-full``` and also includes warnings and infos. 
2. *Extractor:*   ```java -jar elf-cmd.jar extractor <PATH_TO_SCRIPT>``` executes the extractor and retrieves the data as specified in the script.
3. *Generator:* ```java -jar elf-cmd.jar generate <PATH_TO_SCRIPT>``` executes the generation of efficient logging code for the script.

## License

This software is released under the CSIRO Open Source Software Licence Agreement. Details can be found [LICENSE.md](LICENSE.md). Moreover the third party components distributed with the software and their licenses are listed in [NOTICE.md](NOTICE.md).

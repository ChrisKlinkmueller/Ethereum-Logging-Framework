package au.csiro.data61.aap;

import au.csiro.data61.aap.etl.configuration.AddressListSpecification;
import au.csiro.data61.aap.etl.configuration.BlockNumberSpecification;
import au.csiro.data61.aap.etl.configuration.BuildException;
import au.csiro.data61.aap.etl.configuration.GenericFilterPredicateSpecification;
import au.csiro.data61.aap.etl.configuration.LogEntrySignatureSpecification;
import au.csiro.data61.aap.etl.configuration.MethodSpecification;
import au.csiro.data61.aap.etl.configuration.ProgramBuilder;
import au.csiro.data61.aap.etl.configuration.ValueAccessorSpecification;
import au.csiro.data61.aap.etl.configuration.ValueMutatorSpecification;
import au.csiro.data61.aap.etl.configuration.XesExportSpecification;
import au.csiro.data61.aap.etl.configuration.XesParameterSpecification;
import au.csiro.data61.aap.etl.core.Instruction;
import au.csiro.data61.aap.etl.core.ProgramState;

/**
 * ExtractCryptoKitties
 */
public class ExtractCryptoKitties {
    private static final String START_BLOCK = "6605100"; // GENESIS: "4605167";
    private static final String END_BLOCK = "6618100"; // GENESIS: "4608167";
    private static final String URL = "ws://localhost:8546/";
    private static final String FOLDER = "C:/Development/xes-blockchain/v0.2/test_output/kitties";

    public static void main(String[] args) {
        try {
            Instruction program = buildProgram();
            ProgramState state = new ProgramState();
            program.execute(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Instruction buildProgram() throws BuildException {
        final ProgramBuilder builder = new ProgramBuilder();

        builder.prepareProgramBuild();
            builder.addMethodCall(MethodSpecification.of("connect", "string"), ValueAccessorSpecification.stringLiteral(URL));
            builder.addMethodCall(MethodSpecification.of("setOutputFolder", "string"), ValueAccessorSpecification.stringLiteral(FOLDER));
            builder.addVariableAssignment(ValueMutatorSpecification.ofVariableName("kitties"), ValueAccessorSpecification.integerArrayLiteral("{0}"));
            builder.prepareBlockRangeBuild();
                addBirthEventHandlers(builder);
                addTransferEventHandlers(builder);
                addPregnantEventHandlers(builder);
                addAuctionCancelledEventHandlers(builder);
                addAuctionCreatedEventHandlers(builder);
                addAuctionSuccessfulEventHandlers(builder);
            builder.buildBlockRange(
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(START_BLOCK)),
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(END_BLOCK))
            );

        return builder.buildProgram();
    }       

    private static void addBirthEventHandlers(ProgramBuilder builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();
        
            // add kitty to known kitties and create trace as well as birth event
            builder.addMethodCall(
                MethodSpecification.of("add", "int[]", "int"), 
                ValueAccessorSpecification.ofVariable("kitties"),
                ValueAccessorSpecification.ofVariable("kittyId") 
            );
            builder.addInstruction(XesExportSpecification.ofTraceExport(null, ValueAccessorSpecification.ofVariable("kittyId")));
            logEvent(builder, "kittyId", "born");

            // if father is known, add became father event
            logConditionalEvent(builder, "matronId", "became mother");

            // if mother is known, add became mother event
            logConditionalEvent(builder, "sireId", "became father");
        
        // event Birth(address owner, uint256 kittyId, uint256 matronId, uint256 sireId, uint256 genes);
        builder.buildLogEntryFilter(
            AddressListSpecification.ofAddress("0x06012c8cf97BEaD5deAe237070F9587f8E7A266d"), 
            LogEntrySignatureSpecification.of(
                "Birth", 
                new String[]{"address", "uint256", "uint256", "uint256", "uint256"}, 
                new String[]{"owner", "kittyId", "matronId", "sireId", "genes"},
                new boolean[]{false, false, false, false, false}
            )    
        );
    }

    private static void addTransferEventHandlers(ProgramBuilder builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();

        logConditionalEvent(builder, "tokenId", "transferred");

        // event Transfer(address from, address to, uint256 tokenId);
        builder.buildLogEntryFilter(
            AddressListSpecification.ofAddress("0x06012c8cf97BEaD5deAe237070F9587f8E7A266d"), 
            LogEntrySignatureSpecification.of(
                "Transfer", 
                new String[]{"address", "address", "uint256"}, 
                new String[]{"from", "to", "tokenId"},
                new boolean[]{false, false, false}
            )    
        );
    }

    private static void addPregnantEventHandlers(ProgramBuilder builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();

        logConditionalEvent(builder, "matronId", "conceived as mother");
        logConditionalEvent(builder, "sireId", "conceived as father");

        // event Pregnant(address owner, uint256 matronId, uint256 sireId, uint256 cooldownEndBlock);
        builder.buildLogEntryFilter(
            AddressListSpecification.ofAddress("0x06012c8cf97BEaD5deAe237070F9587f8E7A266d"), 
            LogEntrySignatureSpecification.of(
                "Pregnant", 
                new String[]{"address", "uint256", "uint256", "uint256"}, 
                new String[]{"owner", "matronId", "sireId", "cooldownEndBlock"},
                new boolean[]{false, false, false, false}
            )    
        );
    }

    private static void addAuctionCreatedEventHandlers(ProgramBuilder builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();

        logConditionalEvent(builder, "tokenId", "put up for auction");

        // event AuctionCreated(uint256 tokenId, uint256 startingPrice, uint256 endingPrice, uint256 );
        builder.buildLogEntryFilter(
            AddressListSpecification.ofAddress("0xb1690c08e213a35ed9bab7b318de14420fb57d8c"), 
            LogEntrySignatureSpecification.of(
                "AuctionCreated", 
                new String[]{"uint256", "uint256", "uint256", "uint256"}, 
                new String[]{"tokenId", "startingPrice", "endingPrice", "duration"},
                new boolean[]{false, false, false, false}
            )    
        );
    }

    private static void addAuctionSuccessfulEventHandlers(ProgramBuilder builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();

        logConditionalEvent(builder, "tokenId", "auctioned");

        // event AuctionSuccessful(uint256 tokenId, uint256 totalPrice, address winner);
        builder.buildLogEntryFilter(
            AddressListSpecification.ofAddress("0xb1690c08e213a35ed9bab7b318de14420fb57d8c"), 
            LogEntrySignatureSpecification.of(
                "AuctionSuccessful", 
                new String[]{"uint256", "uint256", "uint256"}, 
                new String[]{"tokenId", "totalPrice", "winner"},
                new boolean[]{false, false, false}
            )    
        );
    }

    private static void addAuctionCancelledEventHandlers(ProgramBuilder builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();

        logConditionalEvent(builder, "tokenId", "not auctioned");

        // event AuctionCancelled(uint256 tokenId);
        builder.buildLogEntryFilter(
            AddressListSpecification.ofAddress("0xb1690c08e213a35ed9bab7b318de14420fb57d8c"), 
            LogEntrySignatureSpecification.of(
                "AuctionCancelled", 
                new String[]{"uint256"}, 
                new String[]{"tokenId"},
                new boolean[]{false}
            )    
        );
    }

    private static void logConditionalEvent(ProgramBuilder builder, String piid, String eventName) throws BuildException {
        builder.prepareGenericFilterBuild();
            logEvent(builder, piid, eventName);
        builder.buildGenericFilter(GenericFilterPredicateSpecification.in(ValueAccessorSpecification.ofVariable("kitties"), ValueAccessorSpecification.ofVariable(piid)));

    }

    private static void logEvent(ProgramBuilder builder, String piid, String eventName) throws BuildException {
        builder.addInstruction(XesExportSpecification.ofEventExport(null, ValueAccessorSpecification.ofVariable(piid), null,
            XesParameterSpecification.ofStringParameter("concept:name", ValueAccessorSpecification.stringLiteral(eventName))
        ));
    }
}
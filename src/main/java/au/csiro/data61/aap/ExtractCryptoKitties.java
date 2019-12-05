package au.csiro.data61.aap;

import java.util.Arrays;

import au.csiro.data61.aap.elf.configuration.AddressListSpecification;
import au.csiro.data61.aap.elf.configuration.BlockNumberSpecification;
import au.csiro.data61.aap.elf.configuration.BuildException;
import au.csiro.data61.aap.elf.configuration.GenericFilterPredicateSpecification;
import au.csiro.data61.aap.elf.configuration.ParameterSpecification;
import au.csiro.data61.aap.elf.configuration.LogEntrySignatureSpecification;
import au.csiro.data61.aap.elf.configuration.SpecificationComposer;
import au.csiro.data61.aap.elf.configuration.ValueAccessorSpecification;
import au.csiro.data61.aap.elf.configuration.ValueMutatorSpecification;
import au.csiro.data61.aap.elf.configuration.XesExportSpecification;
import au.csiro.data61.aap.elf.configuration.XesParameterSpecification;
import au.csiro.data61.aap.elf.core.Instruction;
import au.csiro.data61.aap.elf.core.ProgramState;

/**
 * ExtractCryptoKitties
 */
public class ExtractCryptoKitties {
    private static final String START_BLOCK = "6605100"; // GENESIS: "4605167";
    private static final String END_BLOCK = "6618100"; // GENESIS: "4608167";
   

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
        final SpecificationComposer builder = new SpecificationComposer();

        builder.prepareProgramBuild(); 
            ExtractComposerUtils.addOutputFolderConfig(builder);
            ExtractComposerUtils.addConnectCall(builder);       
            builder.addVariableAssignment(ValueMutatorSpecification.ofVariableName("kitties"), ValueAccessorSpecification.integerArrayLiteral("[0]"));
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

    private static void addBirthEventHandlers(SpecificationComposer builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();
        
            // add kitty to known kitties and create trace as well as birth event
            ExtractComposerUtils.addMethodCall(
                builder, 
                null, 
                "add", 
                Arrays.asList("int[]", "int"), 
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
                ParameterSpecification.of("owner", "address", false), 
                ParameterSpecification.of("kittyId", "uint256", false), 
                ParameterSpecification.of("matronId", "uint256", false), 
                ParameterSpecification.of("sireId", "uint256", false), 
                ParameterSpecification.of("genes", "uint256", false)
            )    
        );
    }

    private static void addTransferEventHandlers(SpecificationComposer builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();

        logConditionalEvent(builder, "tokenId", "transferred");

        // event Transfer(address from, address to, uint256 tokenId);
        builder.buildLogEntryFilter(
            AddressListSpecification.ofAddress("0x06012c8cf97BEaD5deAe237070F9587f8E7A266d"), 
            LogEntrySignatureSpecification.of(
                "Transfer", 
                ParameterSpecification.of("from", "address", false), 
                ParameterSpecification.of("to", "address", false), 
                ParameterSpecification.of("tokenId", "uint256", false)
            )    
        );
    }

    private static void addPregnantEventHandlers(SpecificationComposer builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();

        logConditionalEvent(builder, "matronId", "conceived as mother");
        logConditionalEvent(builder, "sireId", "conceived as father");

        // event Pregnant(address owner, uint256 matronId, uint256 sireId, uint256 cooldownEndBlock);
        builder.buildLogEntryFilter(
            AddressListSpecification.ofAddress("0x06012c8cf97BEaD5deAe237070F9587f8E7A266d"), 
            LogEntrySignatureSpecification.of(
                "Pregnant", 
                ParameterSpecification.of("owner", "address", false), 
                ParameterSpecification.of("matronId", "uint256", false), 
                ParameterSpecification.of("sireId", "uint256", false), 
                ParameterSpecification.of("cooldownEndBlock", "uint256", false)
            )    
        );
    }

    private static void addAuctionCreatedEventHandlers(SpecificationComposer builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();

        logConditionalEvent(builder, "tokenId", "put up for auction");

        // event AuctionCreated(uint256 tokenId, uint256 startingPrice, uint256 endingPrice, uint256 duration);
        builder.buildLogEntryFilter(
            AddressListSpecification.ofAddress("0xb1690c08e213a35ed9bab7b318de14420fb57d8c"), 
            LogEntrySignatureSpecification.of(
                "AuctionCreated",
                ParameterSpecification.of("tokenId", "uint256", false), 
                ParameterSpecification.of("startingPrice", "uint256", false), 
                ParameterSpecification.of("endingPrice", "uint256", false), 
                ParameterSpecification.of("duration", "uint256", false)
            )    
        );
    }

    private static void addAuctionSuccessfulEventHandlers(SpecificationComposer builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();

        logConditionalEvent(builder, "tokenId", "auctioned");

        // event AuctionSuccessful(uint256 tokenId, uint256 totalPrice, address winner);
        builder.buildLogEntryFilter(
            AddressListSpecification.ofAddress("0xb1690c08e213a35ed9bab7b318de14420fb57d8c"), 
            LogEntrySignatureSpecification.of(
                "AuctionSuccessful", 
                ParameterSpecification.of("tokenId", "uint256", false), 
                ParameterSpecification.of("totalPrice", "uint256", false), 
                ParameterSpecification.of("winner", "address", false)
            )    
        );
    }

    private static void addAuctionCancelledEventHandlers(SpecificationComposer builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();

        logConditionalEvent(builder, "tokenId", "not auctioned");

        // event AuctionCancelled(uint256 tokenId);
        builder.buildLogEntryFilter(
            AddressListSpecification.ofAddress("0xb1690c08e213a35ed9bab7b318de14420fb57d8c"), 
            LogEntrySignatureSpecification.of(
                "AuctionCancelled", 
                ParameterSpecification.of("tokenId", "uint256", false)
            )    
        );
    }

    private static void logConditionalEvent(SpecificationComposer builder, String piid, String eventName) throws BuildException {
        builder.prepareGenericFilterBuild();
            logEvent(builder, piid, eventName);
        builder.buildGenericFilter(GenericFilterPredicateSpecification.in(ValueAccessorSpecification.ofVariable("kitties"), ValueAccessorSpecification.ofVariable(piid)));

    }

    private static void logEvent(SpecificationComposer builder, String piid, String eventName) throws BuildException {
        builder.addInstruction(XesExportSpecification.ofEventExport(null, ValueAccessorSpecification.ofVariable(piid), null,
            XesParameterSpecification.ofStringParameter("concept:name", ValueAccessorSpecification.stringLiteral(eventName))
        ));
    }
}
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
    private static final String END_BLOCK = "6605200"; // GENESIS: "4608167";
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
                builder.prepareTransactionFilterBuild();
                    addBirthEventHandlers(builder);
                builder.buildTransactionFilter(AddressListSpecification.ofAny(), AddressListSpecification.ofAny());
            builder.buildBlockRange(
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(START_BLOCK)),
                BlockNumberSpecification.ofBlockNumber(ValueAccessorSpecification.integerLiteral(END_BLOCK))
            );

        return builder.buildProgram();
    }       
    
    // event Birth(address owner, uint256 kittyId, uint256 matronId, uint256 sireId, uint256 genes);
        // --> became father
        // --> became mother
        // --> born
    // event Transfer(address from, address to, uint256 tokenId);
        // transfered
    // event Pregnant(address owner, uint256 matronId, uint256 sireId, uint256 cooldownEndBlock);
        // --> conceived baby as dad
        // --> conceived baby as mother
    // event AuctionCreated(uint256 tokenId, uint256 startingPrice, uint256 endingPrice, uint256 duration);
        // put up for auction
    // event AuctionSuccessful(uint256 tokenId, uint256 totalPrice, address winner);
        // not auctioned
    // event AuctionCancelled(uint256 tokenId);
        // auctioned

    private static void addBirthEventHandlers(ProgramBuilder builder) throws BuildException {
        builder.prepareLogEntryFilterBuild();
        
            // add kitty to known kitties and create trace as well as birth event
            builder.addMethodCall(
                MethodSpecification.of("add", "int[]", "int"), 
                ValueAccessorSpecification.ofVariable("kitties"),
                ValueAccessorSpecification.ofVariable("kittyId") 
            );
            builder.addInstruction(XesExportSpecification.ofTraceExport(null, ValueAccessorSpecification.ofVariable("kittyId")));
            builder.addInstruction(XesExportSpecification.ofEventExport(null, ValueAccessorSpecification.ofVariable("kittyId"), null,
                XesParameterSpecification.ofStringParameter("concept:name", ValueAccessorSpecification.stringLiteral("born"))
            ));

            // if father is known, add became father event
            builder.prepareGenericFilterBuild();
                builder.addInstruction(XesExportSpecification.ofEventExport(null, ValueAccessorSpecification.ofVariable("matronId"), null,
                    XesParameterSpecification.ofStringParameter("concept:name", ValueAccessorSpecification.stringLiteral("became mother"))
                ));
            builder.buildGenericFilter(GenericFilterPredicateSpecification.in(ValueAccessorSpecification.ofVariable("kitties"), ValueAccessorSpecification.ofVariable("matronId")));

            // if mother is known, add became mother event
            builder.prepareGenericFilterBuild();
                builder.addInstruction(XesExportSpecification.ofEventExport(null, ValueAccessorSpecification.ofVariable("sireId"), null,
                    XesParameterSpecification.ofStringParameter("concept:name", ValueAccessorSpecification.stringLiteral("became father"))
                ));          
            builder.buildGenericFilter(GenericFilterPredicateSpecification.in(ValueAccessorSpecification.ofVariable("kitties"), ValueAccessorSpecification.ofVariable("sireId")));
          
                    
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
}
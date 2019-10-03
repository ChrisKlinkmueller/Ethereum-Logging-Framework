package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.library.types.SolidityType;
import au.csiro.data61.aap.parser.XbelParser.SolTypeContext;

/**
 * SolidityTypeVisitor
 */
public class SolidityTypeVisitor extends XbelBaseVisitor<SpecificationParserResult<SolidityType<?>>> {

    @Override
    public SpecificationParserResult<SolidityType<?>> visitSolType(SolTypeContext ctx) {
        if (ctx == null  || ctx.getText() == null || ctx.getText().trim().isEmpty()) {
            return SpecificationParserResult.ofError("No type provided.");
        }

        final String keyword = ctx.getText().replaceAll("\\s+", "");
        final SolidityType<?> type = SolidityType.createType(keyword);
        if (type == null) {
            return SpecificationParserResult.ofError(String.format("'s' isn't a valid type.", ctx.getText()));
        }

        return SpecificationParserResult.ofResult(type);
    }
    
}
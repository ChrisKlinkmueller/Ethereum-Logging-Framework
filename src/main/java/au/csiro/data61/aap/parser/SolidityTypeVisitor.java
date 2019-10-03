package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.parser.XbelParser.SolTypeContext;

/**
 * SolidityTypeVisitor
 */
public class SolidityTypeVisitor extends XbelBaseVisitor<SpecificationParserResult<String>> {

    @Override
    public SpecificationParserResult<String> visitSolType(SolTypeContext ctx) {
        if (ctx == null  || ctx.getText() == null || ctx.getText().trim().isEmpty()) {
            return SpecificationParserResult.ofError("No type provided.");
        }

        return SpecificationParserResult.ofResult(ctx.getText());
    }
    
}
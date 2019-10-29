package au.csiro.data61.aap.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import au.csiro.data61.aap.parser.XbelParser.BlockFilterContext;
import au.csiro.data61.aap.parser.XbelParser.BlockNumberContext;
import au.csiro.data61.aap.parser.XbelParser.FilterContext;
import au.csiro.data61.aap.spec.CodeBlock;
import au.csiro.data61.aap.spec.Filter;

/**
 * FilterVisitor
 */
public class FilterVisitor extends XbelBaseVisitor<SpecificationParserResult<SpecBuilder<Filter>>> {

    @Override
    public SpecificationParserResult<SpecBuilder<Filter>> visitFilter(FilterContext ctx) {
        if (ctx.blockFilter() != null) {
            return this.visitBlockFilter(ctx.blockFilter());
        }
        return super.visitFilter(ctx);
    }

    @Override
    public SpecificationParserResult<SpecBuilder<Filter>> visitBlockFilter(BlockFilterContext ctx) {
        if (ctx.from.KEY_PENDING() == null) {
            return SpecificationParserResult.ofError(ctx.start, "The 'from' parameter cannot be set to 'PENDING'.");
        }
        
        if (ctx.to.KEY_EARLIEST() == null) {
            return SpecificationParserResult.ofError(ctx.start, "The 'to' parameter cannot be set to 'EARLIEST'.");
        }
               
        return SpecificationParserResult.ofResult(new BlockFilterBuilder(ctx.from, ctx.to));
    }

    private static boolean isValidBlockHierarchy(CodeBlock leave, List<List<Class<?>>> permittedFilterHierarchy) {
        final List<Class<?>> filterHierarchy = getFilterHierarchy(leave);
        return permittedFilterHierarchy.stream().anyMatch(permittedHierarchy -> equalHierarchies(filterHierarchy, permittedHierarchy));
    }

    private static boolean equalHierarchies(List<Class<?>> hierarchy1, List<Class<?>> hierarchy2) {
        return hierarchy1.size() == hierarchy2.size() 
            && IntStream.range(0, hierarchy1.size())
                .allMatch(i -> hierarchy1.get(i).equals(hierarchy2.get(i)));
    }

    private static ArrayList<Class<?>> getFilterHierarchy(CodeBlock leave) {
        final ArrayList<Class<?>> hierarchy = new ArrayList<Class<?>>();

        CodeBlock current = leave;
        while (current != null) {
            if (current.getFilter() != null) {
                hierarchy.add(0, current.getFilter().getClass());
            }
            current = current.getEnclosingBlock();
        }

        return hierarchy;
    }

    private static class BlockFilterBuilder implements SpecBuilder<Filter> {
        private static List<List<Class<?>>> PERMITTED_HIERARCHIES = List.of(List.of());
        
        private final BlockNumberContext fromCtx;
        private final BlockNumberContext toCtx;


        BlockFilterBuilder(BlockNumberContext fromCtx, BlockNumberContext toCtx) {
            this.fromCtx = fromCtx;
            this.toCtx = toCtx;
        }

		@Override
		public SpecificationParserError verify(CodeBlock block) {
            if (!isValidBlockHierarchy(block, PERMITTED_HIERARCHIES)) {
                //return SpecificationParserError error = new SpecificationParserError(token, errorMessage)
            }
            
			return null;
		}

		@Override
		public Filter build(CodeBlock block) {
			// TODO Auto-generated method stub
			return null;
		}
    }
    
}
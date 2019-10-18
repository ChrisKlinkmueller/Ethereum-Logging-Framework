package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.spec.CodeBlock;

/**
 * SpecBuilder
 */
public interface SpecBuilder<T> {
    public SpecificationParserError verify(CodeBlock block);
    public T build(CodeBlock block);
}
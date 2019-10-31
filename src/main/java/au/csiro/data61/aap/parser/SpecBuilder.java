package au.csiro.data61.aap.parser;

import au.csiro.data61.aap.spec.Scope;

/**
 * SpecBuilder
 */
public interface SpecBuilder<T> {
    public SpecificationParserError verify(Scope block);
    public T build();
}
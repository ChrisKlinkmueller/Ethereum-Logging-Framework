package au.csiro.data61.aap;

import java.util.Arrays;
import java.util.List;

import au.csiro.data61.aap.elf.configuration.BuildException;
import au.csiro.data61.aap.elf.configuration.MethodCallSpecification;
import au.csiro.data61.aap.elf.configuration.MethodSpecification;
import au.csiro.data61.aap.elf.configuration.SpecificationComposer;
import au.csiro.data61.aap.elf.configuration.ValueAccessorSpecification;
import au.csiro.data61.aap.elf.configuration.ValueMutatorSpecification;

/**
 * ExtractComposerUtils
 */
public class ExtractComposerUtils {
    private static final String URL = "\"ws://localhost:8546/\"";
    private static final String FOLDER = "\"C:/Development/xes-blockchain/v0.2/test_output\"";

    public static void addMethodCall(
        SpecificationComposer composer, 
        String variableName,
        String methodName, 
        List<String> parameterTypes, 
        ValueAccessorSpecification... accessors
    ) throws BuildException {
        final MethodSpecification method = MethodSpecification.of(methodName, parameterTypes);
        final MethodCallSpecification call = MethodCallSpecification.of(
            method, 
            variableName == null ? null : ValueMutatorSpecification.ofVariableName(variableName), 
            accessors
        );
        composer.addInstruction(call);
    }

    public static void addConnectCall(
        SpecificationComposer composer
    ) throws BuildException  {
        ExtractComposerUtils.addMethodCall(
            composer,
            null,
            "connect",
            Arrays.asList("string"),
            ValueAccessorSpecification.stringLiteral(URL)
        );
    }

    public static void addOutputFolderConfig(
        SpecificationComposer composer
    ) throws BuildException  {
        ExtractComposerUtils.addMethodCall(
            composer,
            null,
            "setOutputFolder",
            Arrays.asList("string"),
            ValueAccessorSpecification.stringLiteral(FOLDER)
        );
    }
}
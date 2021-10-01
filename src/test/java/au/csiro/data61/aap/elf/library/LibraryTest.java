package au.csiro.data61.aap.elf.library;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import au.csiro.data61.aap.elf.library.plugins.Plugin;
import au.csiro.data61.aap.elf.types.BooleanType;
import au.csiro.data61.aap.elf.types.DateType;
import au.csiro.data61.aap.elf.types.FloatType;
import au.csiro.data61.aap.elf.types.IntType;
import au.csiro.data61.aap.elf.types.ListType;
import au.csiro.data61.aap.elf.types.StructField;
import au.csiro.data61.aap.elf.types.StructType;
import io.vavr.control.Try;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class LibraryTest {
    private static final String XES_PLUGIN = "XES";
    private static final String CSV_PLUGIN = "CSV";
    private static final String TXT_PLUGIN = "TXT";
    private static final String ETH_WEB3_PLUGIN = "ETH_WEB3";    

    private static final MethodSignature SIG_1 = new MethodSignature("add", IntType.INSTANCE, IntType.INSTANCE);
    private static final Method METHOD_1 = new Method(SIG_1, IntType.INSTANCE);

    private static final MethodSignature SIG_2 = new MethodSignature("add", FloatType.INSTANCE, FloatType.INSTANCE);
    private static final Method METHOD_2 = new Method(SIG_2, IntType.INSTANCE);

    private static final MethodSignature SIG_3 = new MethodSignature("subtract", IntType.INSTANCE, IntType.INSTANCE);
    private static final Method METHOD_3 = new Method(SIG_3, IntType.INSTANCE);

    private static final MethodSignature SIG_4 = new MethodSignature("subtract", FloatType.INSTANCE, FloatType.INSTANCE);
    private static final Method METHOD_4 = new Method(SIG_4, IntType.INSTANCE);

    private static final StructField FIELD_1 = new StructField("value1", ListType.BOOLEAN_LIST);
    private static final StructField FIELD_2 = new StructField("value2", DateType.INSTANCE);
    private static final StructField FIELD_3 = new StructField("value3", ListType.FLOAT_LIST);
    private static final MethodSignature SIG_5 = new MethodSignature("extract", new StructType(FIELD_1, FIELD_2));
    private static final Method METHOD_5 = new Method(SIG_5, BooleanType.INSTANCE);
    
    private static final MethodSignature SIG_6 = new MethodSignature("extract", new StructType(FIELD_1, FIELD_2, FIELD_3));
    private static final Method METHOD_6 = new Method(SIG_6, BooleanType.INSTANCE);

    private final Library library;

    LibraryTest() {
        this.library = new Library();
    }    

    @ParameterizedTest
    @MethodSource
    @Order(1)
    void addMethod_InsertsWithoutExceptions(Method method) {
        assertDoesNotThrow(() -> this.library.addMethod(method));
    }

    @ParameterizedTest
    @MethodSource
    @Order(2)
    void addMethod_InsertFailsWithException(Method method, Class<? extends Throwable> cl) {
        assertThrows(cl, () -> this.library.addMethod(method));
    }

    @ParameterizedTest
    @MethodSource
    @Order(3)
    void findMethod_MethodIsDetected(MethodSignature signature, Method method) {
        final Try<Method> searchResult = this.library.findMethod(signature);
        assertTrue(searchResult.isSuccess());
        assertEquals(method, searchResult.get());
    }

    @ParameterizedTest
    @MethodSource
    @Order(4)
    void findMethod_MethodIsNotDetected(MethodSignature signature, String messagePrefix) {
        final Try<Method> searchResult = this.library.findMethod(signature);
        assertTrue(searchResult.isFailure());
        assertEquals(UnsupportedOperationException.class, searchResult.getCause().getClass());
        assertTrue(searchResult.getCause().getMessage().contains(messagePrefix));
    }

    @ParameterizedTest
    @MethodSource
    @Order(5)
    void addPlugin_InsertsWithoutExceptions(Plugin plugin) {
        assertDoesNotThrow(() -> this.library.addPlugin(plugin));
    }

    @ParameterizedTest
    @MethodSource
    @Order(6)
    void addPlugin_InsertFailsWithException(Plugin plugin, Class<? extends Throwable> cl) {
        assertThrows(cl, () -> this.library.addPlugin(plugin));
    }

    @ParameterizedTest
    @MethodSource
    @Order(7)
    void findPlugin_PluginIsDetected(String pluginName) {
        final Try<Plugin> searchResult = this.library.findPlugin(pluginName);
        assertTrue(searchResult.isSuccess());
        assertNotNull(searchResult.get());
        assertEquals(pluginName, searchResult.get().getName());
    }

    @ParameterizedTest
    @MethodSource
    @Order(8)
    void findPlugin_PluginIsNotDetected(String pluginName) {
        final Try<Plugin> searchResult = this.library.findPlugin(pluginName);
        assertTrue(searchResult.isFailure());
        assertEquals(UnsupportedOperationException.class, searchResult.getCause().getClass());
    }

    private static Stream<Arguments> addMethod_InsertsWithoutExceptions() {
        return Stream.of(METHOD_1, METHOD_2, METHOD_3, METHOD_4, METHOD_5, METHOD_6).map(Arguments::of);
    }

    private static Stream<Arguments> addMethod_InsertFailsWithException() {
        return Stream.of(
            Arguments.of(null, NullPointerException.class),
            Arguments.of(METHOD_1, IllegalArgumentException.class)
        );
    }

    private static Stream<Arguments> findMethod_MethodIsDetected() {
        final MethodSignature sig = new MethodSignature(
            "extract", 
            new StructType(
                new StructField("value1", ListType.BOOLEAN_LIST), 
                new StructField("value2", DateType.INSTANCE), 
                new StructField("value3", IntType.INSTANCE)
            )
        );
        final Method method = new Method(sig, BooleanType.INSTANCE);

        return Stream.of(
            Arguments.of(METHOD_1.getSignature(), METHOD_1),
            Arguments.of(METHOD_2.getSignature(), METHOD_2),
            Arguments.of(METHOD_3.getSignature(), METHOD_3),
            Arguments.of(METHOD_4.getSignature(), METHOD_4),
            Arguments.of(METHOD_5.getSignature(), METHOD_5),
            Arguments.of(METHOD_6.getSignature(), METHOD_6),
            Arguments.of(method.getSignature(), METHOD_5)
        );
    }

    private static Stream<Arguments> findMethod_MethodIsNotDetected() {
        final StructField field = new StructField("value4", ListType.STRING_LIST);

        return Stream.of(
            Arguments.of(new MethodSignature("add", BooleanType.INSTANCE, BooleanType.INSTANCE), "No"),
            Arguments.of(new MethodSignature("divide", IntType.INSTANCE, IntType.INSTANCE), "No"),
            Arguments.of(new MethodSignature(METHOD_5.getSignature().getName(), new StructType(FIELD_2)), "No"),
            Arguments.of(new MethodSignature(METHOD_5.getSignature().getName(), new StructType(FIELD_1, FIELD_2, FIELD_3, field)), "Multiple")
        );
    }

    private static Stream<Arguments> addPlugin_InsertsWithoutExceptions() {
        return pluginNameStream()
            .map(LibraryTest::createPluginMock)
            .map(Arguments::of);
    }

    private static Plugin createPluginMock(String name) {
        final Plugin mock = mock(Plugin.class);
        Mockito.when(mock.getName()).thenReturn(name);
        return mock;
    }

    private static Stream<Arguments> addPlugin_InsertFailsWithException() {
        return Stream.of(
            Arguments.of(null, NullPointerException.class),
            Arguments.of(createPluginMock(XES_PLUGIN), IllegalArgumentException.class)
        );
    }

    private static Stream<String> findPlugin_PluginIsDetected() {
        return pluginNameStream();            
    }

    private static Stream<String> findPlugin_PluginIsNotDetected() {
        return Stream.concat(
            pluginNameStream().map(String::toLowerCase), 
            Stream.of("NO", "SUCH", "PLUGIN")
        );
    }

    private static Stream<String> pluginNameStream() {
        return Stream.of(XES_PLUGIN, CSV_PLUGIN, TXT_PLUGIN, ETH_WEB3_PLUGIN);
    }

}

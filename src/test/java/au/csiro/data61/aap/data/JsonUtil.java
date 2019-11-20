package au.csiro.data61.aap.data;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;

import au.csiro.data61.aap.etl.core.readers.EthereumBlock;
import au.csiro.data61.aap.etl.core.readers.EthereumLogEntry;
import au.csiro.data61.aap.etl.core.readers.EthereumTransaction;
import au.csiro.data61.aap.etl.core.readers.RawBlock;
import au.csiro.data61.aap.etl.core.readers.RawLogEntry;
import au.csiro.data61.aap.etl.core.readers.RawTransaction;

/**
 * JsonUtil
 */
class JsonUtil {

    static ObjectMapper buildObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        
        SimpleModule module = new SimpleModule("Serializers");
        module.addSerializer(EthereumBlock.class, new BlockJsonizer.BlockSerializer());
        module.addDeserializer(RawBlock.class, new BlockJsonizer.BlockDeserializer());
        module.addSerializer(EthereumTransaction.class, new TransactionJsonizer.TransactionSerializer());
        module.addDeserializer(RawTransaction.class, new TransactionJsonizer.TransactionDeserializer());
        module.addSerializer(EthereumLogEntry.class, new LogEntryJsonizer.LogEntrySerializer());
        module.addDeserializer(RawLogEntry.class, new LogEntryJsonizer.LogEntryDeserializer());
        objectMapper.registerModule(module);
        
        return objectMapper;
    }

    static String getString(JsonNode node, String attribute) {
        return node.get(attribute).asText();
    }

    static BigInteger getInteger(JsonNode node, String attribute) {
        return new BigInteger(node.get(attribute).asText());
    }

    static <T> List<T> getList(JsonNode node, String attribute, Function<JsonNode, T> nodeMapper) {
        final ArrayNode array = (ArrayNode) node.get(attribute);
        return IntStream.range(0, array.size()).mapToObj(i -> array.get(i)).map(n -> nodeMapper.apply(n))
                .collect(Collectors.toList());
    }

    static class TokenValueExtractor<T, E> {
        private final ThrowingFunction<JsonParser, DeserializationContext, T> valueParser;
        private final BiConsumer<E, T> valueSetter;

        private TokenValueExtractor(ThrowingFunction<JsonParser, DeserializationContext, T> valueParser, BiConsumer<E, T> valueSetter) {
            assert valueParser != null;
            assert valueSetter != null;
            this.valueParser = valueParser;
            this.valueSetter = valueSetter;
        }

        public void apply(JsonParser parser, DeserializationContext ctxt, E object) throws IOException {
            final T value = this.valueParser.apply(parser, ctxt);
            this.valueSetter.accept(object, value);
        }
    }

    static abstract class EthereumJsonDeserializer<E> extends StdDeserializer<E> {
        private static final long serialVersionUID = 8774738402923840853L;
        
        private final HashMap<String, TokenValueExtractor<?, E>> extractors;
        public EthereumJsonDeserializer(Class<?> cl) {
            super(cl);
            this.extractors = new HashMap<>();
        }

        protected void addExtractor(String tokenName, TokenValueExtractor<?, E> extractor) {
            this.extractors.put(tokenName, extractor);
        }

        protected abstract E createObject();
        
        @Override
        public E deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            E object = this.createObject();
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken() == JsonToken.FIELD_NAME) {
                    parser.nextToken();
                    this.extractors.get(parser.getCurrentName()).apply(parser, ctxt, object);
                }
            }
            return object;
        }

    }

    static <E> TokenValueExtractor<Boolean, E> getBooleanExtractor(BiConsumer<E, Boolean> valueSetter) {
        return new TokenValueExtractor<Boolean, E>(JsonUtil::readValueAsBoolean, valueSetter);
    }

    static <E> TokenValueExtractor<BigInteger, E> getIntegerExtractor(BiConsumer<E, BigInteger> valueSetter) {
        return new TokenValueExtractor<BigInteger, E>(JsonUtil::readValueAsInteger, valueSetter);
    }

    static <T, E> TokenValueExtractor<List<T>, E> getListExtractor(BiConsumer<E, List<T>> valueSetter, Class<T> cl) {
        return new TokenValueExtractor<List<T>, E>((p, c) -> {
            final JavaType type = c.getTypeFactory().constructCollectionType(List.class, cl);
            JsonDeserializer<Object> des = c.findRootValueDeserializer(type);
            @SuppressWarnings("unchecked")
            final List<T> list = (List<T>)des.deserialize(p, c);
            return list;
        }, valueSetter);
    } 

    static <E> TokenValueExtractor<String, E> getStringExtractor(BiConsumer<E, String> valueSetter) {
        return new TokenValueExtractor<String, E>(JsonUtil::readValueAsString, valueSetter);
    }

    private static Boolean readValueAsBoolean(JsonParser parser, DeserializationContext ctxt) throws IOException {
        return parser.readValueAs(Boolean.class);
    }

    private static BigInteger readValueAsInteger(JsonParser parser, DeserializationContext ctxt) throws IOException {
        return parser.readValueAs(BigInteger.class);
    }

    private static String readValueAsString(JsonParser parser, DeserializationContext ctxt) throws IOException {
        return parser.getValueAsString();
    }

    private static interface ThrowingFunction<S,T,R> {
        public R apply(S s, T t) throws IOException;
    } 
}
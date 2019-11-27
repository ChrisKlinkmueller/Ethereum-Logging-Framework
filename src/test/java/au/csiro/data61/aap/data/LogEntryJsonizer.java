package au.csiro.data61.aap.data;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import au.csiro.data61.aap.data.JsonUtil.EthereumJsonDeserializer;
import au.csiro.data61.aap.elf.core.readers.EthereumLogEntry;
import au.csiro.data61.aap.elf.core.readers.RawLogEntry;

/**
 * LogEntryJsonizer
 */
public class LogEntryJsonizer {
    private static final String ADDRESS = "address";
    private static final String DATA = "data";
    private static final String LOG_INDEX = "logIndex";
    private static final String REMOVED = "removed";
    private static final String TOPICS = "topics";
    
    public static class LogEntrySerializer extends StdSerializer<EthereumLogEntry> {
        private static final long serialVersionUID = 1703283075996634952L;

        public LogEntrySerializer() {
            super(EthereumLogEntry.class);
        }

        @Override
        public void serialize(EthereumLogEntry log, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            gen.writeStringField(ADDRESS, log.getAddress());
            gen.writeStringField(DATA, log.getData());
            gen.writeStringField(LOG_INDEX, log.getLogIndex().toString());
            gen.writeBooleanField(REMOVED, log.isRemoved());
            
            gen.writeArrayFieldStart(TOPICS);
            for (int i = 0; i < log.getTopics().size(); i++) {
                gen.writeString(log.getTopics().get(i));
            }
            gen.writeEndArray();
            
            gen.writeEndObject();
        }

    }

    public static class LogEntryDeserializer extends EthereumJsonDeserializer<RawLogEntry> {
        private static final long serialVersionUID = -1896860192129257034L;

        public LogEntryDeserializer() {
            super(RawLogEntry.class);
            this.addExtractor(ADDRESS, JsonUtil.getStringExtractor(RawLogEntry::setAddress));
            this.addExtractor(DATA, JsonUtil.getStringExtractor(RawLogEntry::setData));
            this.addExtractor(LOG_INDEX, JsonUtil.getIntegerExtractor(RawLogEntry::setLogIndex));
            this.addExtractor(REMOVED, JsonUtil.getBooleanExtractor(RawLogEntry::setRemoved));
            this.addExtractor(TOPICS, JsonUtil.getListExtractor(RawLogEntry::setTopics, String.class));
        }

        @Override
        protected RawLogEntry createObject() {
            return new RawLogEntry();
        }

    }
}
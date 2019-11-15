package au.csiro.data61.aap.data;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import au.csiro.data61.aap.rpc.EthereumLog;

/**
 * LogEntryJsonizer
 */
public class LogEntryJsonizer {

    public static class LogEntrySerializer extends StdSerializer<EthereumLog> {
        private static final String ADDRESS = "address";
        private static final String DATA = "data";
        private static final String LOG_INDEX = "logIndex";
        private static final String REMOVED = "removed";
        private static final String TOPICS = "topics";
        private static final long serialVersionUID = 1703283075996634952L;

        public LogEntrySerializer() {
            this(null);
        }

        public LogEntrySerializer(Class<EthereumLog> t) {
            super(t);
        }

        @Override
        public void serialize(EthereumLog log, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            gen.writeStringField(ADDRESS, log.getAddress());
            gen.writeStringField(DATA, log.getData());
            gen.writeStringField(LOG_INDEX, log.getLogIndex().toString());
            gen.writeBooleanField(REMOVED, log.isRemoved());
            
            gen.writeArrayFieldStart(TOPICS);
            for (int i = 0; i < log.topicCount(); i++) {
                gen.writeString(log.getTopic(i));
            }

            gen.writeEndArray();

            gen.writeEndObject();
        }

    }
}
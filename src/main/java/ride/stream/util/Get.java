package ride.stream.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public class Get {


    public static Properties kafkaProperties(String groupId) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "10.30.0.39:9092");
        properties.put("key.deserializer", StringDeserializer.class);
        properties.put("value.deserializer", StringDeserializer.class);
        properties.put("group.id", groupId);
        properties.put("application.id", groupId);
        return properties;
    }

    public static ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}

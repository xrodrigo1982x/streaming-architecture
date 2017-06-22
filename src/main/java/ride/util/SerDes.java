package ride.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import ride.stream.util.Get;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

public abstract class SerDes<T> implements Deserializer<T>, Serializer<T> {

    private ObjectMapper OM;
    private Class<T> clazz;

    private ObjectMapper getOm() {
        if (OM == null)
            OM = Get.objectMapper();
        return OM;
    }

    protected SerDes(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(String topic, T element) {
        try {
            return getOm().writeValueAsString(element).getBytes(Charset.forName("UTF-8"));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public T deserialize(String s, byte[] bytes) {
        try {
            return getOm().readValue(new String(bytes, Charset.forName("UTF-8")), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

    }
}

package ride.stream.util;

import lombok.AllArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Serializable;

@AllArgsConstructor
public class TopicHolder implements Serializable {

    private static KafkaProducer<String, String> PRODUCER;
    private String topic;

    private KafkaProducer<String, String> get() {
        if (PRODUCER == null)
            PRODUCER = new KafkaProducer<>(Get.kafkaProperties(topic), new StringSerializer(), new StringSerializer());
        return PRODUCER;
    }

    public void send(ProducerRecord<String, String> record) {
        get().send(record);
    }

}

package ride.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import ride.model.CarAreaEvent;
import ride.model.CarLocationEvent;
import ride.routing.Elasticsearch;
import ride.stream.util.Get;
import ride.util.CarAreaEventSerDes;
import ride.util.CarLocationEventSerDes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ride.stream.Topics.CARS_AVAILABLE_IN_AREA;
import static ride.stream.Topics.CAR_CURRENT_LOCATION;

public class CarIndicator {

    private static KafkaProducer<String, CarAreaEvent> producer = new KafkaProducer<>(Get.kafkaProperties("cars-in-area"), new StringSerializer(), new CarAreaEventSerDes());

    public static void main(String[] args) throws Exception {
        CarLocationEventSerDes carLocationEventSerDes = new CarLocationEventSerDes();
        KStreamBuilder streamBuilder = new KStreamBuilder();
        KStream<String, CarLocationEvent> stream = streamBuilder.stream(Serdes.String(), Serdes.serdeFrom(carLocationEventSerDes, carLocationEventSerDes), CAR_CURRENT_LOCATION);

        //"10.30.0.40"

        Elasticsearch es = Feign.builder().target(Elasticsearch.class, "http://10.30.0.40:9200");
        ObjectMapper om = new ObjectMapper();

        stream.filter((k, v) -> v.isAvailable()).foreach((k, v) -> {
            try {
                String m = es.search(v.getCarId(), v.getLocation().getLatitude(), v.getLocation().getLongitude());
                Map response = om.readValue(m, HashMap.class);
                List<Map> users = (List<Map>) ((Map) response.get("hits")).get("hits");
                users.forEach(user -> {
                    CarAreaEvent id = CarAreaEvent.builder()
                            .userId((String) user.get("_id"))
                            .location(v.getLocation())
                            .timestamp(v.getTimestamp())
                            .carId(v.getCarId())
                            .build();
                    producer.send(new ProducerRecord<>(CARS_AVAILABLE_IN_AREA, id));
                });
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });

        KafkaStreams kafkaStreams = new KafkaStreams(streamBuilder, Get.kafkaProperties("car-indicator"));
        kafkaStreams.start();
    }

}

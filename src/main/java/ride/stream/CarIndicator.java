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
    public static final Elasticsearch ES = Feign.builder().target(Elasticsearch.class, "http://10.30.0.40:9200");
    public static final ObjectMapper OM = new ObjectMapper();
    public static final KStreamBuilder STREAM_BUILDER = new KStreamBuilder();

    public static void main(String[] args) throws Exception {
        createStream(STREAM_BUILDER).filter((key, carLocation) -> carLocation.isAvailable()).foreach((key, carLocation) -> {
            try {
                String searchResult = ES.search(carLocation.getCarId(), carLocation.getLocation().getLatitude(), carLocation.getLocation().getLongitude());

                ((List<Map>) ((Map) OM.readValue(searchResult, HashMap.class).get("hits")).get("hits")).forEach(user -> {
                    CarAreaEvent carEvent = CarAreaEvent.builder()
                            .userId((String) user.get("_id"))
                            .location(carLocation.getLocation())
                            .timestamp(carLocation.getTimestamp())
                            .carId(carLocation.getCarId())
                            .build();
                    producer.send(new ProducerRecord<>(CARS_AVAILABLE_IN_AREA, carEvent));
                });
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });

        startStream(STREAM_BUILDER);
    }

    private static void startStream(KStreamBuilder streamBuilder) {
        KafkaStreams kafkaStreams = new KafkaStreams(streamBuilder, Get.kafkaProperties("car-indicator"));
        kafkaStreams.start();
    }

    private static KStream<String, CarLocationEvent> createStream(KStreamBuilder streamBuilder) {
        CarLocationEventSerDes carLocationEventSerDes = new CarLocationEventSerDes();
        return streamBuilder.stream(Serdes.String(), Serdes.serdeFrom(carLocationEventSerDes, carLocationEventSerDes), CAR_CURRENT_LOCATION);
    }

}

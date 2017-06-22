package ride.web;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import ride.model.CarAreaEvent;
import ride.stream.util.Get;
import ride.util.CarAreaEventSerDes;

import javax.annotation.PostConstruct;

import static ride.stream.Topics.CARS_AVAILABLE_IN_AREA;

@EnableWebSocketMessageBroker
@Controller
@SpringBootApplication
public class WebApplication extends AbstractWebSocketMessageBrokerConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/car-location");
        registry.addEndpoint("/car-location").withSockJS();
    }

    @Autowired
    MessageSendingOperations<String> sendingOperations;

    @PostConstruct
    public void init() {
        CarAreaEventSerDes carareaEventSerDes = new CarAreaEventSerDes();
        KStreamBuilder streamBuilder = new KStreamBuilder();
        KStream<String, CarAreaEvent> stream = streamBuilder.stream(Serdes.String(), Serdes.serdeFrom(carareaEventSerDes, carareaEventSerDes), CARS_AVAILABLE_IN_AREA);

        stream.foreach((k, v) -> sendingOperations.convertAndSend("/topic/car-area-user-" + v.getUserId(), v));

        KafkaStreams kafkaStreams = new KafkaStreams(streamBuilder, Get.kafkaProperties("car-area-user"));
        kafkaStreams.start();
    }

}

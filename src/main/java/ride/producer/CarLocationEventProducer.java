package ride.producer;

import lombok.Data;
import org.apache.commons.lang3.RandomUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import ride.model.CarLocationEvent;
import ride.stream.util.Get;
import ride.util.CarLocationEventSerDes;

import java.util.Date;
import java.util.List;

import static java.lang.Character.getNumericValue;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static ride.stream.Topics.CAR_CURRENT_LOCATION;
import static ride.stream.util.Generator.aNew;

public class CarLocationEventProducer {

    private static KafkaProducer<String, CarLocationEvent> producer = new KafkaProducer<>(Get.kafkaProperties("car-location-producer"), new StringSerializer(), new CarLocationEventSerDes());

    public static void main(String[] args) {

        int count = nextInt(150, 200);

        System.out.println(count);

        List<CarLocationEventHolder> carsEvents = range(0, count).mapToObj(i -> {
            System.out.println(i);
            CarLocationEvent car = aNew(CarLocationEvent.class);
            car.setCarId(String.valueOf(i));
            car.setTimestamp(new Date());
            car.setAvailable(false);
            return new CarLocationEventHolder(car);
        }).collect(toList());

        while (true) {
            try {
                carsEvents.stream().map(holder -> holder.updateLocation().event).forEach(event -> {
                    int partition = getNumericValue(event.getCarId().charAt(event.getCarId().length() - 1));
                    String key = event.getCarId() + event.getTimestamp();
                    producer.send(new ProducerRecord<>(CAR_CURRENT_LOCATION, partition, key, event));
                });
                Thread.sleep(2000l);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Data
    private static class CarLocationEventHolder {

        private static final double FACTOR = 0.0004;

        private CarLocationEvent event;
        private boolean changeLat;
        private boolean changeLng;
        private boolean sum;

        public CarLocationEventHolder(CarLocationEvent event) {
            this.event = event;
            changeDirection();
        }

        private void changeDirection() {
            changeLat = nextInt(0, 2) == 0;
            changeLng = !changeLat;
            sum = nextInt(0, 2) == 0;
        }

        private void change() {
            if (nextInt(0, 10) == 0) {
                changeDirection();
                updateLocation();
            }
        }

        public CarLocationEventHolder updateLocation() {
            if (changeLat)
                event.getLocation().setLatitude(event.getLocation().getLatitude() + ((sum ? 1 : -1) * FACTOR));

            if (changeLng)
                event.getLocation().setLongitude(event.getLocation().getLongitude() + ((sum ? 1 : -1) * FACTOR));

            if(RandomUtils.nextInt(0, 20) == 0)
                event.setAvailable(!event.isAvailable());

            event.setTimestamp(new Date());
            return this;
        }
    }

}

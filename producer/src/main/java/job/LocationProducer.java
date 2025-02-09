package job;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.producer.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;
import java.util.concurrent.Future;

public class LocationProducer {
    private static final String TOPIC = "location_updates";
    private static final String BOOTSTRAP_SERVERS = "broker:9092";

    public static void main(String[] args) throws InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        List<UserDto> users = getAll();
        Map<Long, LocationData> datas = new HashMap<>();
        Random random = new Random();
        for (UserDto userDto : users) {
            datas.put(userDto.id(), new LocationData(-90 + 180 * random.nextDouble(),
                    -180 + 360 * random.nextDouble(),
                    userDto.id()));
        }
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            ObjectMapper objectMapper = new ObjectMapper();
            while (true) {
                UserDto userDto = users.get(random.nextInt(0, users.size()));
                LocationData locationData = datas.get(userDto.id());
                double newLatitude =  (locationData.longitude() + (random.nextDouble() - 0.5) * 0.01) % 90;
                double newLongitude =  (locationData.longitude() + (random.nextDouble() - 0.5) * 0.01) % 180;
                LocationData data = new LocationData(
                        newLatitude,
                        newLongitude,
                        userDto.id());
                String message = null;
                try {
                    message = objectMapper.writeValueAsString(data);
                } catch (JsonProcessingException e) {
                    System.out.println(e);
                }
                try {
                    Future<RecordMetadata> send = producer.send(new ProducerRecord<>(TOPIC, message));
                    send.get();
                    System.out.println("Sent location: " + message);
                } catch (Throwable a) {
                    System.out.println(a);
                }
            }
        }
    }

    private static List<UserDto> getAll() {
        UserClient userClient = new UserClient();
        return new ArrayList<>(userClient.getAllUsers());
    }
}

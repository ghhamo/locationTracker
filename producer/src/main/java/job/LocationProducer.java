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
//        createTopic();
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        List<UserDto> users = getAll();
        while (true) {
            try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
                Random random = new Random();
                ObjectMapper objectMapper = new ObjectMapper();
                double latitude = 52.5200;
                double longitude = 13.4050;
                while (true) {
                    UserDto user = users.get(random.nextInt(0, users.size()));
                    latitude += (random.nextDouble() - 0.5) * 0.01;
                    longitude += (random.nextDouble() - 0.5) * 0.01;
                    LocationData data = new LocationData(latitude, longitude, user.id());
                    String message = null;
                    try {
                        message = objectMapper.writeValueAsString(data);
                        System.out.println(message);
                    } catch (JsonProcessingException e) {
                        System.out.println(e);
                    }
                    try {
                        Future<RecordMetadata> send = producer.send(new ProducerRecord<>(TOPIC, message));
                        send.get();
                    } catch (Throwable a) {
                        System.out.println(a);
                    }
                    System.out.println("Sent location: " + message);
                    Thread.sleep(5000);
                }
            }
        }
    }

    private static List<UserDto> getAll() {
        UserClient userClient = new UserClient();
        return new ArrayList<>(userClient.getAllUsers());
    }

 /*   private static void createTopic() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        try (AdminClient adminClient = AdminClient.create(props)) {
            NewTopic newTopic = new NewTopic(TOPIC, 1, (short) 1);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            System.out.println("Topic created successfully.");
        } catch (ExecutionException e) {
            if (e.getCause() instanceof TopicExistsException) {
                System.out.println("Topic already exists.");
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}

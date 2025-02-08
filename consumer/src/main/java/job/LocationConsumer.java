package job;
import org.apache.kafka.clients.consumer.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class LocationConsumer {
    private static final String TOPIC = "location_updates";
    private static final String BOOTSTRAP_SERVERS = "broker:9092";
    private static double totalDistance = 0.0;
    private static LocationData lastLocation = null;

    public static void main(String[] args) {
        System.out.println("aaaaaa");
        KafkaConsumer<String, String> consumer = getStringStringKafkaConsumer();
        consumer.subscribe(Collections.singletonList(TOPIC));
        ObjectMapper objectMapper = new ObjectMapper();

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : records) {
                try {
                    LocationData location = objectMapper.readValue(record.value(), LocationData.class);
                    if (lastLocation != null) {
                        totalDistance += haversine(lastLocation.latitude(), lastLocation.longitude(), location.latitude(), location.longitude());
                    }
                    UserClient userClient = new UserClient();
                    userClient.updateDistance(location.userId(), totalDistance);
                    lastLocation = location;
                    System.out.println("Total Distance Traveled of user: " + location.userId() + " is " + totalDistance + " km");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static KafkaConsumer<String, String> getStringStringKafkaConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "location-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());

        return new KafkaConsumer<>(props);
    }

    // Haversine formula to calculate distance in km
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

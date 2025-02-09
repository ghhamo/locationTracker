package job;

import org.apache.kafka.clients.consumer.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;

public class LocationConsumer {
    private static final String TOPIC = "location_updates";
    private static final String BOOTSTRAP_SERVERS = "broker:9092";

    public static void main(String[] args) throws InterruptedException {
        KafkaConsumer<String, String> consumer = getStringStringKafkaConsumer();
        consumer.subscribe(Collections.singletonList(TOPIC));
        ObjectMapper objectMapper = new ObjectMapper();

        final HashMap<Long, LocationData> lastLocations = new HashMap<>();
        HashMap<Long, Double> distances = new HashMap<>();

        while (true) {
            Thread.sleep(10000);
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            System.out.println("Received " + records.count() + " location updates");
            for (ConsumerRecord<String, String> record : records) {
                LocationData lastLocation;
                try {
                    LocationData newLocation = objectMapper.readValue(record.value(), LocationData.class);
                    Long userId = newLocation.userId();
                    lastLocation = lastLocations.get(userId);
                    if (lastLocation != null) {
                        double distance = haversine(
                                lastLocation.latitude(), lastLocation.longitude(),
                                newLocation.latitude(), newLocation.longitude());
                        double totalDistance = distances.getOrDefault(userId, 0d) + distance;
                        distances.put(userId, totalDistance);
                    }
                    lastLocations.put(userId, newLocation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!distances.isEmpty()) {
                UserClient userClient = new UserClient();
                for (Map.Entry<Long, Double> entry : distances.entrySet()) {
                    System.out.println("User with id: " + entry.getKey() + " traveled total distance of " + entry.getValue());
                    userClient.updateDistance(entry.getKey(), distances.get(entry.getKey()));
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
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Apply the Haversine formula
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in kilometers
    }
}

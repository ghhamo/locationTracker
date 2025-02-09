package job;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final Random random = new Random();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserData createUser(String username) {
        UserData userData = new UserData();
        userData.setUsername(username);
        return userRepository.save(userData);
    }

    public double getDistanceTraveled(Long id) {
        return userRepository.findById(id)
                .map(UserData::getDistanceTraveled)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserData updateDistanceTraveled(Long id, double distance) {
        return userRepository.findById(id)
                .map(userData -> {
                    userData.setDistanceTraveled(userData.getDistanceTraveled() + distance);
                    return userRepository.save(userData);
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<UserData> getAllUsers() {
        return userRepository.findAll();
    }

    @PostConstruct
    public void seedUsers() {
        if (userRepository.count() == 0) {
            IntStream.range(0, 10).forEach(i -> {
                UserData userData = new UserData();
                userData.setUsername("User" + i);
                userData.setDistanceTraveled(0);
                userRepository.save(userData);
            });
        }
    }
}

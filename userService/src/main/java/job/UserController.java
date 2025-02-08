package job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public UserData createUser(@RequestBody Map<String, String> request) {
        return userService.createUser(request.get("username"));
    }

    @GetMapping("/{id}/distance")
    public double getDistanceTraveled(@PathVariable Long id) {
        return userService.getDistanceTraveled(id);
    }

    @PutMapping("/{id}/update-distance")
    public UserData updateDistance(@PathVariable Long id, @RequestBody Map<String, Double> request) {
        return userService.updateDistanceTraveled(id, request.get("distance"));
    }

    @GetMapping
    public List<UserData> getAllUsers() {
        return userService.getAllUsers();
    }
}

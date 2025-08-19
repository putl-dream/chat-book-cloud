package fun.amireux.chat.book.user.config;

import fun.amireux.chat.book.user.vo.LoginRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        if (StringUtils.equals(request.getPassword(), "111")) {
            return ResponseEntity.ok("Login success (UserService)");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}


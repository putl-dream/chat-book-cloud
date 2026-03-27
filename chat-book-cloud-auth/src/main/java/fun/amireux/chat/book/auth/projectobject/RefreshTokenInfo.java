package fun.amireux.chat.book.auth.projectobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenInfo implements Serializable {
    private Integer userId;
    private Instant issuedAt;
    private Instant expiresAt;
    private String deviceInfo;
}

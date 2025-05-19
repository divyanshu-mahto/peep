package email.peep.peep.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @Column(name = "open_id")
    private String openId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "access_token", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "access_token_expiry", nullable = false)
    private Instant expiration;
}

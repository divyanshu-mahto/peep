package email.peep.peep.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "rules")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id", unique = true)
    private UUID ruleId;

    @Column(name = "rule_text", nullable = false)
    private String ruleText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "open_id", nullable = false)
    @JsonBackReference
    private User user;
}

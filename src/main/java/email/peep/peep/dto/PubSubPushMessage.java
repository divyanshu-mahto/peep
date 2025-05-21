package email.peep.peep.dto;

import lombok.Data;

@Data
public class PubSubPushMessage {
    private PubSubMessage message;
    private String subscription;
}
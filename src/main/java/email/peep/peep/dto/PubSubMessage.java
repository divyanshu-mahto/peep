package email.peep.peep.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PubSubMessage {
    private Map<String, String> attributes;
    private String data;
    private String messageId;
    private String publishTime;
}
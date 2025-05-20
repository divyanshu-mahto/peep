package email.peep.peep.dto;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PubSubMessage {
    private Map<String, String> attributes;
    private String data;
    private String messageId;
    private String publishTime;

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }

    public String getDecodedData() {
        if (data == null) {
            return null;
        }
        return new String(java.util.Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "PubSubMessage{" +
                "attributes=" + attributes +
                ", data='" + data + '\'' +
                ", decodedData='" + getDecodedData() + '\'' +
                ", messageId='" + messageId + '\'' +
                ", publishTime='" + publishTime + '\'' +
                '}';
    }
}
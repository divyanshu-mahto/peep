package email.peep.peep.model;

import lombok.Data;

@Data
public class NotificationMessage {
    private String from;
    private String heading;
    private String description;
    private String priority;
    private String deadline;
    private boolean relevant;
}

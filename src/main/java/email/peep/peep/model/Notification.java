package email.peep.peep.model;

import lombok.Data;

@Data
public class Notification {
    private String classification;
    private String description;
    private Boolean isImportant;
}

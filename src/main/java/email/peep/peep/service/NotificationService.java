package email.peep.peep.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import email.peep.peep.model.NotificationMessage;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final FirebaseMessaging firebaseMessaging;

    public NotificationService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }


    public String sendNotification(String token, NotificationMessage notificationMessage) {

        String body;
        if(!notificationMessage.getDeadline().isEmpty() && notificationMessage.getDeadline().equals("null")){
            body = notificationMessage.getDescription();
        }else{
            body = notificationMessage.getDescription()+" | Deadline: "+notificationMessage.getDeadline();
        }

        Notification notification = Notification.builder()
                .setTitle(notificationMessage.getFrom() + " | " + notificationMessage.getHeading())
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(token)
                .setNotification(notification)
                // .putData("key", "value")
                .build();

        try {
            return firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return "Error sending notification: " + e.getMessage();
        }
    }
}

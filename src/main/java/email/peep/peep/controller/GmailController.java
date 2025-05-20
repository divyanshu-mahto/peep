package email.peep.peep.controller;

import com.google.api.services.gmail.model.Label;
import email.peep.peep.dto.PubSubMessage;
import email.peep.peep.dto.PubSubPushMessage;
import email.peep.peep.model.User;
import email.peep.peep.repository.UserRepository;
import email.peep.peep.service.GmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gmail")
public class GmailController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    GmailService gmailService;

    @GetMapping("labels/{email}")
    public ResponseEntity<String> getEmailLabels(@PathVariable("email") String email){
        User user = userRepository.findByEmail(email);
        if(user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        try {
            List<Label> list = gmailService.getLabels(user.getAccessToken());
            return ResponseEntity.ok("Success\n"+list.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching labels: " + e.getMessage());
        }
    }


    @PostMapping("/pubsub")
    public ResponseEntity<String> receiveMessage(@RequestBody PubSubPushMessage pushMessage) {
        if (pushMessage == null || pushMessage.getMessage() == null) {
            System.out.println("Received invalid Pub/Sub push message (null or missing inner message).");
            return new ResponseEntity<>("Invalid message format", HttpStatus.BAD_REQUEST);
        }

        PubSubMessage innerMessage = pushMessage.getMessage();

        String messageId = innerMessage.getMessageId();
        String decodedData = innerMessage.getDecodedData();
        Map<String, String> attributes = innerMessage.getAttributes();
        String subscriptionName = pushMessage.getSubscription();

        System.out.println("--- Message Arrived ---");
        System.out.println("  Subscription: {}"+ subscriptionName);
        System.out.println("  Message ID: {}"+ messageId);
        System.out.println("  Payload (decoded): {}"+ decodedData);
        System.out.println("  Attributes: {}"+ attributes);
        System.out.println("-----------------------");

        return new ResponseEntity<>("Message processed successfully", HttpStatus.OK);
    }

}

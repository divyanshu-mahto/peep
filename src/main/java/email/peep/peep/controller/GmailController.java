package email.peep.peep.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.gmail.model.Label;
import email.peep.peep.dto.PubSubMessage;
import email.peep.peep.dto.PubSubPushMessage;
import email.peep.peep.model.User;
import email.peep.peep.repository.UserRepository;
import email.peep.peep.service.AuthService;
import email.peep.peep.service.GmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/gmail")
public class GmailController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    GmailService gmailService;

    @Autowired
    AuthService authService;

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
    public ResponseEntity<String> receiveMessage(@RequestBody PubSubPushMessage pushMessage) throws IOException, GeneralSecurityException {
        if (pushMessage == null || pushMessage.getMessage() == null) {
            System.out.println("Received invalid Pub/Sub push message (null or missing inner message).");
            return new ResponseEntity<>("Invalid message format", HttpStatus.BAD_REQUEST);
        }

        PubSubMessage pubSubMessage = pushMessage.getMessage();

        String data = pubSubMessage.getData();
        String publishTime = pubSubMessage.getPublishTime();


        byte[] decodedBytes = Base64.getUrlDecoder().decode(data);
        String decodedJsonString = new String(decodedBytes, StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(decodedJsonString);

        String emailAddress = jsonNode.get("emailAddress").asText();
        String historyId = jsonNode.get("historyId").asText();

        System.out.println("----------------Got new message-----------------------");
        System.out.println(historyId);
        System.out.println(emailAddress);
        System.out.println(publishTime);
        System.out.println("------------------------------------------------------");

        User user = userRepository.findByEmail(emailAddress);

        //verify if access token is valid
        //else refresh access token
        authService.getValidAccessToken(user);
        user = userRepository.findByEmail(emailAddress);

        gmailService.readGmail(user, historyId);

        return new ResponseEntity<>("Message processed successfully", HttpStatus.OK);
    }

}

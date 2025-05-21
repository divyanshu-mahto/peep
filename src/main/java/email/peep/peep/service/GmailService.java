package email.peep.peep.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import email.peep.peep.model.User;
import email.peep.peep.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;


@Service
public class GmailService {

    @Autowired
    UserRepository userRepository;

    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public List<Label> getLabels(String accessToken) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = new GoogleCredential().setAccessToken(accessToken);
        String APPLICATION_NAME = "Gmail API Java Quickstart";
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        String user = "me";
        ListLabelsResponse listResponse = service.users().labels().list(user).execute();

        List<Label> labels = listResponse.getLabels();

        if (labels.isEmpty()) {
            System.out.println("No labels found.");
        } else {
            System.out.println("Labels:");
            for (Label label : labels) {
                System.out.printf("- %s\n", label.getName());
            }
        }
        return labels;
    }

    @Value("${gmail.pubsub.topic}")
    private String topicName;

    public Gmail getGmailService(String accessToken) {
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

        return new Gmail.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("Peep-demo-1")
                .build();
    }

    public WatchResponse startWatch(String accessToken) throws IOException {
        Gmail service = getGmailService(accessToken);

        WatchRequest request = new WatchRequest()
                .setTopicName(topicName)
                .setLabelIds(List.of("INBOX"))
                .setLabelFilterAction("include");

        WatchResponse response = service.users().watch("me", request).execute();

        System.out.println("Watch response: " + response);
        return response;
    }

    public void readGmail(String email, String historyId) throws GeneralSecurityException, IOException {
        System.out.println("----Attempting to read message----------");

        User user = userRepository.findByEmail(email);

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = new GoogleCredential().setAccessToken(user.getAccessToken());
        String APPLICATION_NAME = "Peep-demo-1";
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        ListHistoryResponse listHistoryResponse = service.users().history().list("me")
                .setStartHistoryId(user.getHistoryId())
                .setHistoryTypes(List.of("messageAdded"))
                .execute();

        List<History> historyList = listHistoryResponse.getHistory();

        if(historyList == null || historyList.isEmpty()){
            System.out.println("No new message added");
        }
        else {

            for (History history : historyList) {
                if (history.getMessagesAdded() != null) {
                    for (HistoryMessageAdded messageAdded : history.getMessagesAdded()) {
                        String messageId = messageAdded.getMessage().getId();

                        Message message = service.users().messages().get("me", messageId).setFormat("full").execute();

                        System.out.println("Id: " + messageId);
                        System.out.println("From: " + getHeader(message.getPayload().getHeaders(), "from"));
                        System.out.println("Subject: " + getHeader(message.getPayload().getHeaders(), "subject"));

                        String body = extractBody(message.getPayload());
                        System.out.println("Body: " + body);
                    }
                }
            }
        }

        user.setHistoryId(BigInteger.valueOf(Long.parseLong(historyId)));
        userRepository.save(user);
    }

    private String getHeader(List<MessagePartHeader> headers, String name) {
        if (headers == null) return null;
        for (MessagePartHeader header : headers) {
            if (header.getName().equalsIgnoreCase(name)) {
                return header.getValue();
            }
        }
        return null;
    }

    private String extractBody(MessagePart part) {
        if (part.getBody() != null && part.getBody().getData() != null) {
            byte[] bytes = Base64.getUrlDecoder().decode(part.getBody().getData());
            return new String(bytes, StandardCharsets.UTF_8);
        }

        if (part.getParts() != null) {
            for (MessagePart sub : part.getParts()) {
                String text = extractBody(sub);
                if (!text.isEmpty()) {
                    return text;
                }
            }
        }
        return "";
    }
}

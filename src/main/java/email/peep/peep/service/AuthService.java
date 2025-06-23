package email.peep.peep.service;

import com.google.api.services.gmail.model.WatchResponse;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import email.peep.peep.model.User;
import email.peep.peep.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    GmailService gmailService;

    final RestTemplate restTemplate = new RestTemplate();

    @Value("${GOOGLE_OAUTH_CLIENT_ID}")
    private String clientId;

    @Value("${GOOGLE_OAUTH_CLIENT_SECRET}")
    private String clientSecret;

    public void createUser(User user) throws IOException {
        Optional<User> existing = userRepository.findById(user.getOpenId());

        WatchResponse watchResponse = gmailService.startWatch(user.getAccessToken());
        BigInteger historyId = watchResponse.getHistoryId();

        if(existing.isPresent()) {
            User existingUser = existing.get();
            existingUser.setAccessToken(user.getAccessToken());
            existingUser.setExpiration(user.getExpiration());
            existingUser.setHistoryId(historyId);
            userRepository.save(existingUser);
        }
        else {
            user.setHistoryId(historyId);
            userRepository.save(user);
        }
    }

    public String getValidAccessToken(User user) throws IOException {
        UserCredentials userCredentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(user.getRefreshToken())
                .setAccessToken(new AccessToken(
                        user.getAccessToken(),
                        Date.from(user.getExpiration())
                ))
                .build();

        if(Instant.now().isAfter(user.getExpiration())){
            System.out.println("Refreshing Access token for "+user.getEmail());
            userCredentials.refresh();
            AccessToken newAccessToken = userCredentials.getAccessToken();
            user.setAccessToken(newAccessToken.getTokenValue());
            user.setExpiration(newAccessToken.getExpirationTime().toInstant());
            userRepository.save(user);

        }
        return userCredentials.getAccessToken().getTokenValue();
    }

    @Scheduled(fixedRate = 600 * 60 * 1000)
    public void refreshWatch() throws IOException {
        List<User> users = userRepository.findAll();
        for(User user : users){
            try{
                String accessToken = getValidAccessToken(user);
                gmailService.startWatch(accessToken);
                System.out.println("Watch refreshed for: "+user.getEmail());
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Error in refreshing watch for "+user.getEmail()+": "+e.getMessage());
            }
        }
    }
}

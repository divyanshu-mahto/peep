package email.peep.peep.service;

import com.google.api.services.gmail.model.WatchResponse;
import email.peep.peep.model.User;
import email.peep.peep.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    GmailService gmailService;


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
}

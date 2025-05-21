package email.peep.peep.service;

import email.peep.peep.model.User;
import email.peep.peep.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    GmailService gmailService;


    public void createUser(User user) throws IOException {
        Optional<User> existing = userRepository.findById(user.getOpenId());
        if(existing.isPresent()) {
            User existingUser = existing.get();
            existingUser.setAccessToken(user.getAccessToken());
            existingUser.setExpiration(user.getExpiration());
            userRepository.save(existingUser);
        }
        else {
            userRepository.save(user);
        }
        gmailService.startWatch(user.getAccessToken());
    }
}

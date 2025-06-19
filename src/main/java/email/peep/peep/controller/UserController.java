package email.peep.peep.controller;

import email.peep.peep.model.Rule;
import email.peep.peep.model.User;
import email.peep.peep.repository.RuleRepository;
import email.peep.peep.repository.UserRepository;
import email.peep.peep.service.AuthService;
import email.peep.peep.service.GeminiService;
import email.peep.peep.service.JwtService;
import email.peep.peep.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    AuthService authService;

    @Autowired
    GeminiService geminiService;

    @RequestMapping("/user")
    public Principal user(Principal user) {
        return user;
    }

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RuleRepository ruleRepository;

    @GetMapping("/login/success")
    public ResponseEntity<String> signup(OAuth2AuthenticationToken token, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient){
        User user = new User();
        user.setOpenId(token.getPrincipal().getAttribute("sub"));
        user.setName(token.getPrincipal().getAttribute("name"));
        user.setEmail(token.getPrincipal().getAttribute("email"));
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
        user.setAccessToken(accessToken.getTokenValue());
        if(refreshToken != null){
            user.setRefreshToken(refreshToken.getTokenValue());
        }
        user.setExpiration(accessToken.getExpiresAt());

        try {
            String jwtToken = jwtService.generateToken(user.getEmail());
            authService.createUser(user);
            return new ResponseEntity<>(jwtToken, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //create new rule
    @PostMapping("/create-rule")
    public ResponseEntity<?> addRule(@RequestHeader("Authorization") String authHeader, @RequestBody String rule){

        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            userService.createRule(user, rule);
            return new ResponseEntity<>("Rule created",HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //get all rules
    @GetMapping("/all-rules")
    public ResponseEntity<?> allRules(@RequestHeader("Authorization") String authHeader){
        try{
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            List<Rule> rules = userService.getAllRules(user);

            return new ResponseEntity<>(rules, HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //edit existing rule

    //delete a rule
    @DeleteMapping("/rule")
    public ResponseEntity<?> deleteRule(@RequestHeader("Authorization") String authHeader, @RequestBody UUID ruleId){
        try{
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            ruleRepository.deleteById(ruleId);

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //add fcm token
    @PostMapping("/set-fcm")
    public ResponseEntity<?> setFcm(@RequestHeader("Authorization") String authHeader, @RequestBody String fcmToken){
        try{
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            user.setFcmToken(fcmToken);
            userRepository.save(user);

            return new ResponseEntity<>("Token Updated", HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

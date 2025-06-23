package email.peep.peep.controller;

import email.peep.peep.model.Rule;
import email.peep.peep.model.User;
import email.peep.peep.repository.RuleRepository;
import email.peep.peep.repository.UserRepository;
import email.peep.peep.service.AuthService;
import email.peep.peep.service.GeminiService;
import email.peep.peep.service.JwtService;
import email.peep.peep.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
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

    @Value("${frontend.url}")
    private String frontendUrl;

    @GetMapping("/login/success")
    public ResponseEntity<String> signup(OAuth2AuthenticationToken token, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, HttpServletResponse response){
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

            if (user == null || !jwtService.validateToken(token, email)) {
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

            if (user == null || !jwtService.validateToken(token, email)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            List<Rule> rules = userService.getAllRules(user);

            return new ResponseEntity<>(rules, HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //all rules 2
    @GetMapping("/all-rules2")
    public ResponseEntity<?> allRules2(@CookieValue(name = "Authorization", required = false) String authHeader){
        System.out.println("JWT: "+authHeader);
        System.out.println("Got request on all rules 2");

        if(authHeader == null || authHeader.isBlank()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        System.out.println("JWT: "+authHeader);

        try{
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null || !jwtService.validateToken(token, email)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            List<Rule> rules = userService.getAllRules(user);

            return new ResponseEntity<>(rules, HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //edit existing rule
    @PostMapping("/edit-rule")
    public ResponseEntity<?> modifyRule(@RequestHeader("Authorization") String authHeader, @RequestBody UUID ruleId){
        try{
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null || !jwtService.validateToken(token, email)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            ruleRepository.deleteById(ruleId);

            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //delete a rule
    @DeleteMapping("/rule")
    public ResponseEntity<?> deleteRule(@RequestHeader("Authorization") String authHeader, @RequestBody UUID ruleId){
        try{
            String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            User user = userRepository.findByEmail(email);

            if (user == null || !jwtService.validateToken(token, email)) {
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

            if (user == null || !jwtService.validateToken(token, email)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            System.out.println("----------Recieved fcm token: "+fcmToken);

            user.setFcmToken(fcmToken);
            userRepository.save(user);

            System.out.println("-------------Token updated------------");
            return new ResponseEntity<>("Token Updated", HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

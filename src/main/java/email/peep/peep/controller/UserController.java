package email.peep.peep.controller;

import email.peep.peep.model.Notification;
import email.peep.peep.model.User;
import email.peep.peep.service.AuthService;
import email.peep.peep.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

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
            authService.createUser(user);
            return new ResponseEntity<>( "Login Success", HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/askai")
    public ResponseEntity<?> askAi(){
        Notification resp = geminiService.jsonResponseGemini();
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}

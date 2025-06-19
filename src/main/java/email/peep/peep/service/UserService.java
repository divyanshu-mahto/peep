package email.peep.peep.service;

import email.peep.peep.model.Rule;
import email.peep.peep.model.User;
import email.peep.peep.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private RuleRepository ruleRepository;

    @Transactional
    public void createRule(User user, String rule){
        try {
            Rule newRule = new Rule();
            newRule.setRuleText(rule);
            newRule.setUser(user);

            ruleRepository.save(newRule);

        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public List<Rule> getAllRules(User user){
        return user.getRules();
    }


}

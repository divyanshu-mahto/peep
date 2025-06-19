package email.peep.peep.service;

import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.google.gson.Gson;
import email.peep.peep.model.NotificationMessage;
import email.peep.peep.model.Rule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeminiService {

    public NotificationMessage jsonResponseGemini(String emailContent, List<Rule> rules){
        Client client = Client.builder().vertexAI(true).build();

        Schema schema = Schema.builder()
                .type("object")
                .properties(ImmutableMap.of(
                        "from", Schema.builder().type(Type.Known.STRING).description("Who sent the mail(just the name in short one word only not entire email address)").build(),
                        "heading", Schema.builder().type(Type.Known.STRING).description("what the email is regarding(very short) should act as notification title").build(),
                        "description", Schema.builder().type(Type.Known.STRING).description("Short summary of email content should act as notification body").build(),
                        "priority", Schema.builder().type(Type.Known.STRING).description("high or medium or low").build(),
                        "deadline", Schema.builder().type(Type.Known.STRING).description("If the email is relevant is there any deadline for any of the task defined in the mail else return string 'null'").build(),
                        "relevant", Schema.builder().type(Type.Known.BOOLEAN).description("If the email is relevant return true else false").build()
                ))
                .required(List.of("from", "heading", "description", "priority", "deadline", "relevant"))
                .build();

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .candidateCount(1)
                        .responseSchema(schema)
                        .build();

        StringBuilder allRules = new StringBuilder();
        int i = 1;
        for(Rule rule : rules){
            allRules.append("Rule "+i++);
            allRules.append("'"+rule.getRuleText()+"' , ");
        }

        String prompt = "Classify the following email:\n\n" +
                "EMAIL CONTENT: " + emailContent + "\n\n" +
                "FOLLOW THESE RULES: " + allRules + "\n\n" +
                "Return a JSON object with the following specific fields: 'from', 'heading', 'description', 'priority', and 'deadline'. " +
                "It is CRITICAL that ALL five fields are present in the JSON response, even if a field's value is 'null' or a brief summary.\n" +
                "Instructions for each field:\n" +
                "- 'from': Provide the sender's name in one short word (e.g., 'John', 'Sarah'). Do NOT include email addresses.\n" +
                "- 'heading': Create a very short, concise title for the email's main topic, suitable as a notification title.\n" +
                "- 'description': Generate a short summary of the email's content, to be used as the notification body. Always provide a summary.\n" +
                "- 'priority': Determine the urgency and importance and classify it as 'high', 'medium', or 'low'. Always provide a priority.\n" +
                "- 'deadline': Extract any explicit date or time mentioned for a task or event in the email. If no specific deadline is found, return the exact string 'null'.\n" +
                "- 'relevant': Does the email matches to any of the rules.\n" +
                "Ensure the JSON output is strictly formatted according to the schema provided, and all specified fields are always included.";

        GenerateContentResponse response =
                client.models.generateContent("gemini-2.0-flash-001", prompt, config);

        String jsonResponse = response.text();
        System.out.println("Raw JSON Response: " + jsonResponse);

        Gson gson = new Gson();
        NotificationMessage notificationMessage = gson.fromJson(jsonResponse, NotificationMessage.class);

        return notificationMessage;
    }
}

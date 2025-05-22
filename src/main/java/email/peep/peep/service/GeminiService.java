package email.peep.peep.service;

import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import com.google.gson.Gson;
import email.peep.peep.model.Notification;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    public Notification jsonResponseGemini(){
        Client client = Client.builder().vertexAI(true).build();

        Schema schema = Schema.builder()
                .type("object")
                .properties(ImmutableMap.of(
                        "classification", Schema.builder().type(Type.Known.STRING).description("The category or label for the response").build(),
                        "description", Schema.builder().type(Type.Known.STRING).description("Detailed explanation").build(),
                        "isImportant", Schema.builder().type(Type.Known.BOOLEAN).description("Whether this is important").build()
                ))
                .build();

        GenerateContentConfig config =
                GenerateContentConfig.builder()
                        .responseMimeType("application/json")
                        .candidateCount(1)
                        .responseSchema(schema)
                        .build();

        String prompt = "Classify the statement: 'A random number'. " +
                "Return a JSON object with 'classification', 'description', and 'isImportant' fields.";

        GenerateContentResponse response =
                client.models.generateContent("gemini-2.0-flash-001", prompt, config);

        String jsonResponse = response.text();
        System.out.println("Raw JSON Response: " + jsonResponse);

        Gson gson = new Gson();
        Notification notification = gson.fromJson(jsonResponse, Notification.class);

        return notification;
    }
}

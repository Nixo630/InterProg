package gui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.util.Base64;
import java.time.Duration;
import java.net.http.HttpTimeoutException;

import org.json.JSONArray;
import org.json.JSONObject;

public class GeminiAPIRequest {
    private static final String apiKey = "to complete";
    private static String apiUrl = "";

    public static String geminiRequest(String prompt, File[] files, boolean[] goodPDFs) throws Exception {
        apiUrl = "https://generativelanguage.googleapis.com/v1/models/"+Settings.loadProperties().getProperty("aiVersion")+":generateContent?key=" + apiKey;
        
        // Création du client HTTP
        HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

        JSONObject requestBody = new JSONObject();
        JSONArray contentsArray = new JSONArray();

        // Ajout du texte de la requête avec rôle
        JSONObject textContent = new JSONObject();
        textContent.put("role", "user");
        textContent.put("parts", new JSONObject().put("text", prompt));
        contentsArray.put(textContent);

        // Ajout des fichiers PDF
        for (int i = 0; i < 3; i++) {
            if (goodPDFs[i]) {
                contentsArray.put(createPdfPart(files[i], "user"));
            }
        }

        requestBody.put("contents", contentsArray);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                System.out.println(response.body());
                return "Vous n'avez plus de points";
            }
            else if (response.statusCode() == 400) {
                System.out.println(response.body());
                return "Requete trop longue";
            }
            else if (response.statusCode() != 200) {
                System.out.println(response.body());
                return "Problem";
            }

            String textResponse = "";
            int start = response.body().indexOf("\"text\": \"");
            int end = response.body().indexOf("\"role\": \"model\"");

            if (start < 0 || end < 0) {
                return "Problem";
            }
            end -= 36;
            start += 9;
            for (int i = start; i <= end; i++) {
                textResponse += response.body().charAt(i);
            }
            return textResponse;
        } catch (HttpTimeoutException e) {
            return "Timeout";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "La requête a été interrompue";
        }
    }

    private static JSONObject createPdfPart(File pdfFile, String role) throws IOException {
        byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
        String base64EncodedPdf = Base64.getEncoder().encodeToString(pdfBytes);

        JSONObject pdfPart = new JSONObject();
        pdfPart.put("mimeType", "application/pdf");
        pdfPart.put("data", base64EncodedPdf);

        JSONObject part = new JSONObject();
        part.put("inlineData", pdfPart);

        JSONObject content = new JSONObject();
        content.put("role", role);
        content.put("parts", new JSONArray().put(part));

        return content;
    }
}

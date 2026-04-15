package service;

import com.google.gson.*;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GeminiAIService {

    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build();

    private static final String[] STORE_NAMES = {
        "Maju Jaya Store",
        "Sejahtera Stall",
        "Nusantara Kitchen"
    };

    /**
     * Generate 6 rekomendasi (2 per form) sesuai kategori buyer (makanan/minuman).
     * Return array of 6: [form1_opt1, form1_opt2, form2_opt1, form2_opt2, form3_opt1, form3_opt2]
     * Format tiap item: "Nama Item - Rp harga"
     */
    public static String[][] generateOptions(String buyerMessage, int sellerIndex)
            throws IOException {

        String prompt = "You are a food/drink seller named \"" + STORE_NAMES[sellerIndex] + "\".\n"
            + "A buyer requested: \"" + buyerMessage + "\"\n\n"
            + "Detect if the buyer wants FOOD or DRINK based on the request.\n"
            + "Then suggest 6 menu items of the SAME category (all food OR all drink).\n"
            + "Each item must be different.\n\n"
            + "Reply ONLY in this exact JSON format, no explanation, no extra text:\n"
            + "{\n"
            + "  \"category\": \"food\" or \"drink\",\n"
            + "  \"form1_opt1\": {\"name\": \"item name\", \"price\": 15000},\n"
            + "  \"form1_opt2\": {\"name\": \"item name\", \"price\": 12000},\n"
            + "  \"form2_opt1\": {\"name\": \"item name\", \"price\": 18000},\n"
            + "  \"form2_opt2\": {\"name\": \"item name\", \"price\": 14000},\n"
            + "  \"form3_opt1\": {\"name\": \"item name\", \"price\": 10000},\n"
            + "  \"form3_opt2\": {\"name\": \"item name\", \"price\": 13000}\n"
            + "}";

        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject body = new JsonObject();
        body.addProperty("model", "llama-3.3-70b-versatile");
        body.add("messages", messages);
        body.addProperty("temperature", 0.7);

        Request request = new Request.Builder()
            .url(API_URL)
            .post(RequestBody.create(
                body.toString(),
                MediaType.parse("application/json")
            ))
            .addHeader("Authorization", "Bearer " + API_KEY)
            .addHeader("Content-Type", "application/json")
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code()
                    + " " + response.body().string());
            }

            String responseBody = response.body().string();
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
            String groqText = responseJson
                .getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

            groqText = groqText.trim()
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

            JsonObject rec = JsonParser.parseString(groqText).getAsJsonObject();

            // Parse 6 opsi jadi array [form][option]
            String[][] result = new String[3][2];
            String[] keys = {"form1_opt1", "form1_opt2", "form2_opt1", "form2_opt2", "form3_opt1", "form3_opt2"};
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 2; j++) {
                    JsonObject item = rec.getAsJsonObject(keys[i * 2 + j]);
                    String name  = item.get("name").getAsString();
                    long price   = item.get("price").getAsLong();
                    result[i][j] = name + " - Rp " + formatRupiah(price);
                }
            }
            return result;
        }
    }

    private static String formatRupiah(long amount) {
        String raw = String.valueOf(amount);
        StringBuilder sb = new StringBuilder();
        int start = raw.length() % 3;
        if (start > 0) sb.append(raw, 0, start);
        for (int i = start; i < raw.length(); i += 3) {
            if (sb.length() > 0) sb.append(".");
            sb.append(raw, i, i + 3);
        }
        return sb.toString();
    }

    // Method lama tetap ada biar tidak error di tempat lain
    public static String[] generateRecommendation(String buyerMessage, int sellerIndex)
            throws IOException {
        String[][] opts = generateOptions(buyerMessage, sellerIndex);
        return new String[]{
            opts[0][0], opts[1][0], opts[2][0], ""
        };
    }
}
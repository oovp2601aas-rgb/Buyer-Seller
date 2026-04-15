package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Product;

/**
 * SellerAIService - AI-powered assistant for sellers (Buyer-Kings Edition)
 * 
 * ARCHITECTURE: Service Layer (separated from UI)
 * PURPOSE: Generate intelligent, buyer-centric responses that help sellers 
 *          understand and fulfill buyer desires for food items (Nasi Padang, 
 *          spicy/sweet foods, beverages)
 * 
 * FLOW: Buyer Message -> AI Analysis -> Seller Form Auto-fill -> Seller Sends -> Buyer Receives Offer
 */
public class SellerAIService {

    private List<Product> catalog;
    private Map<Integer, PendingOrder> pendingOrders;

    public enum ResponseType {
        PRODUCT_EXPLANATION,
        PRICE_ESTIMATION,
        STOCK_AVAILABILITY
    }

    private class PendingOrder {
        Product product;
        int quantity;
        String preference; // buyer's taste preference

        PendingOrder(Product product, int quantity, String preference) {
            this.product = product;
            this.quantity = quantity;
            this.preference = preference;
        }
    }

    public SellerAIService() {
        this.catalog = new ArrayList<>();
        this.pendingOrders = new HashMap<>();
        initializeCatalog();
    }

    private void initializeCatalog() {
        catalog.add(new Product("Nasi Padang", 25000.0, 20)); // Authentic Padang rice
        catalog.add(new Product("Ayam Pop", 18000.0, 15));    // Mild fried chicken
        catalog.add(new Product("Rendang", 22000.0, 12));     // Spicy beef
        catalog.add(new Product("Gulai Cincang", 20000.0, 10)); // Spicy minced beef
        catalog.add(new Product("Sambal Ijo", 5000.0, 30));   // Green chili (spicy!)
        catalog.add(new Product("Es Teh Manis", 8000.0, 50)); // Sweet iced tea
        catalog.add(new Product("Es Jeruk", 10000.0, 40));    // Orange juice (sweet & fresh)
        catalog.add(new Product("Jus Alpukat", 15000.0, 25)); // Avocado juice (creamy sweet)
        catalog.add(new Product("Es Campur", 12000.0, 20));   // Mixed ice dessert (sweet)
        catalog.add(new Product("Kolak Pisang", 10000.0, 15)); // Sweet banana compote
    }

    /**
     * Generate AI-suggested response for seller to send to buyer
     * 
     * @param buyerMessage The message from the buyer (what they want/crave)
     * @param responseType Which form field to generate response for
     * @param requestId    Optional request ID for context tracking
     * @return Suggested response text (ready to send to buyer)
     */
    public String generateResponse(String buyerMessage, ResponseType responseType, int requestId) {
        if (buyerMessage == null || buyerMessage.trim().isEmpty()) {
            return "";
        }

        String message = buyerMessage.toLowerCase().trim();
        BuyerIntent intent = analyzeBuyerIntent(message);

        switch (responseType) {
            case PRODUCT_EXPLANATION:
                return generateBuyerCentricExplanation(message, intent);

            case PRICE_ESTIMATION:
                return generateBuyerFriendlyPricing(message, requestId, intent);

            case STOCK_AVAILABILITY:
                return generateStockWithUrgency(message, requestId, intent);

            default:
                return "";
        }
    }

    /**
     * Analyze what the buyer really wants (taste profile, mood, occasion)
     */
    private BuyerIntent analyzeBuyerIntent(String message) {
        BuyerIntent intent = new BuyerIntent();
        
        // Detect taste preferences
        if (message.contains("spicy") || message.contains("hot") || message.contains("pedas") || 
            message.contains("sambal") || message.contains("chili")) {
            intent.spicy = true;
            intent.preference = "spicy";
        }
        if (message.contains("sweet") || message.contains("manis") || message.contains("dessert") || 
            message.contains("sugar") || message.contains("honey")) {
            intent.sweet = true;
            intent.preference = "sweet";
        }
        if (message.contains("mild") || message.contains("not spicy") || message.contains("plain") ||
            message.contains("soft")) {
            intent.mild = true;
            intent.preference = "mild";
        }
        
        // Detect meal type
        if (message.contains("heavy") || message.contains("full") || message.contains("meal") || 
            message.contains("lunch") || message.contains("dinner") || message.contains("padang")) {
            intent.heavyMeal = true;
        }
        if (message.contains("light") || message.contains("snack") || message.contains("drink") || 
            message.contains("beverage") || message.contains("thirsty") || message.contains("refresh")) {
            intent.lightRefreshment = true;
        }
        
        // Detect mood/occasion
        if (message.contains("hungry") || message.contains("starving") || message.contains("craving")) {
            intent.hungry = true;
        }
        if (message.contains("refresh") || message.contains("cool") || message.contains("hot day")) {
            intent.refreshing = true;
        }
        
        // Detect budget sensitivity
        if (message.contains("cheap") || message.contains("affordable") || message.contains("budget") || 
            message.contains("student") || message.contains("promo")) {
            intent.budgetConscious = true;
        }
        if (message.contains("premium") || message.contains("best") || message.contains("special")) {
            intent.premium = true;
        }
        
        return intent;
    }

    /**
     * Generate explanation focused on buyer's satisfaction and experience
     */
    private String generateBuyerCentricExplanation(String message, BuyerIntent intent) {
        // If buyer wants SPICY + HEAVY MEAL
        if (intent.spicy && intent.heavyMeal) {
            return "Perfect choice for spice lovers! Our Nasi Padang comes with authentic Rendang (slow-cooked spicy beef) and Sambal Ijo that will satisfy your cravings. The rich coconut curry with green chilies gives you that addictive heat that keeps you coming back. Want extra sambal on the side?";
        }
        
        // If buyer wants SPICY only
        if (intent.spicy) {
            return "Love the heat? You've got to try our Gulai Cincang with extra Sambal Ijo! The green chili sambal is freshly ground daily - spicy, tangy, and incredibly aromatic. Perfect for drizzling over rice or dipping. How spicy do you want it?";
        }
        
        // If buyer wants SWEET + REFRESHMENT
        if (intent.sweet && intent.lightRefreshment) {
            return "Looking for something sweet and refreshing? Our Es Campur is a crowd favorite - mixed fruits, jelly, and coconut milk over crushed ice. Or try our Jus Alpukat for creamy sweetness that cools you down instantly. Perfect for this weather!";
        }
        
        // If buyer wants SWEET only
        if (intent.sweet) {
            return "Sweet tooth calling? Our Kolak Pisang (warm banana in palm sugar coconut milk) is comfort in a bowl, or go for Es Campur if you want something icy sweet with various textures. Both are made with less sugar so you taste the natural sweetness!";
        }
        
        // If buyer wants MILD/Not spicy
        if (intent.mild) {
            return "Prefer milder flavors? Our Ayam Pop is perfect - tender fried chicken that's savory but not spicy, served with light broth. Pair it with plain rice and skip the sambal. Comfort food without the burn!";
        }
        
        // If buyer is HUNGRY (big portion)
        if (intent.hungry && intent.heavyMeal) {
            return "Hungry? Our Nasi Padang is generous! You get rice + 2 side dishes of your choice (Rendang, Ayam Pop, or Gulai). The portions are big enough to keep you full for hours. Add an extra rice if you're really starving!";
        }
        
        // If buyer wants DRINKS only
        if (intent.lightRefreshment && !intent.heavyMeal) {
            return "Thirsty? Our Es Teh Manis is brewed fresh (not too sweet), or upgrade to Es Jeruk for fresh-squeezed orange juice. For something indulgent, the Jus Alpukat is creamy and filling - almost like a meal itself!";
        }
        
        // If buyer mentions BUDGET
        if (intent.budgetConscious) {
            return "Great news! We have budget-friendly options starting from just Rp 8,000. Nasi Padang with one side dish is Rp 25,000 and very filling. Or grab Es Teh Manis + light snack for under Rp 20,000 total. Good food doesn't have to break the bank!";
        }
        
        // Default: Ask clarifying question to help buyer decide
        return "I'd love to help you find the perfect meal! Are you looking for something spicy and hearty like our Nasi Padang, or maybe something sweet and refreshing? Any dietary preferences I should know about?";
    }

    /**
     * Generate pricing that emphasizes value and options for buyer
     */
    private String generateBuyerFriendlyPricing(String message, int requestId, BuyerIntent intent) {
        Product p = detectProduct(message);
        int qty = extractQuantity(message);
        
        if (p != null) {
            // Check stock first
            if (qty > p.getStock()) {
                return "I'd love to fulfill that, but I only have " + p.getStock() + " " + p.getName() + " left right now. How about " + p.getStock() + " instead? Or I can suggest alternatives that are fully stocked!";
            }

            double unitPrice = p.getPrice();
            double subtotal = unitPrice * qty;
            double discount = 0;
            double total = subtotal;
            String specialNote = "";

            // Student/Budget deal
            if (intent.budgetConscious && qty >= 2) {
                discount = subtotal * 0.15; // 15% off for budget buyers buying multiple
                total = subtotal - discount;
                specialNote = "Student/Budget Bundle applied! ";
            }
            // Regular bulk discount
            else if (qty >= 5) {
                discount = subtotal * 0.10;
                total = subtotal - discount;
                specialNote = "Group order discount applied! ";
            }
            // Sweet combo deal
            else if (intent.sweet && intent.heavyMeal) {
                specialNote = "Sweet & Savory Combo: Add Es Campur for only Rp 8,000 more (save Rp 4,000)! ";
            }

            // Store context
            pendingOrders.put(requestId, new PendingOrder(p, qty, intent.preference));

            StringBuilder sb = new StringBuilder();
            sb.append("Here's your personalized offer:\n\n");
            sb.append("🍽️ ").append(qty).append("x ").append(p.getName()).append("\n");
            sb.append("💰 Rp ").append(String.format("%,.0f", unitPrice)).append(" each\n");
            sb.append("Subtotal: Rp ").append(String.format("%,.0f", subtotal)).append("\n");
            
            if (discount > 0) {
                sb.append("🎉 Discount: -Rp ").append(String.format("%,.0f", discount)).append("\n");
            }
            sb.append("✨ Total: Rp ").append(String.format("%,.0f", total)).append("\n\n");
            
            if (!specialNote.isEmpty()) {
                sb.append("💡 ").append(specialNote).append("\n");
            }
            
            // Add value context
            if (intent.budgetConscious) {
                sb.append("That's only Rp ").append(String.format("%,.0f", total/qty)).append(" per person - great value for authentic taste!");
            } else {
                sb.append("Ready to confirm? I can prepare this immediately!");
            }
            
            return sb.toString();
        }

        // If no specific product detected, offer curated options based on intent
        if (intent.spicy) {
            return "Spicy cravings? Here are your options:\n" +
                   "• Nasi Padang + Rendang: Rp 25,000 (the full experience)\n" +
                   "• Gulai Cincang only: Rp 20,000 (for the spice lovers)\n" +
                   "• Extra Sambal Ijo: Rp 5,000 (add to anything!)\n\n" +
                   "Which one calls to you?";
        }
        if (intent.sweet) {
            return "Sweet treats menu:\n" +
                   "• Es Campur: Rp 12,000 (mixed fruits & jelly)\n" +
                   "• Jus Alpukat: Rp 15,000 (creamy avocado)\n" +
                   "• Kolak Pisang: Rp 10,000 (warm comfort)\n\n" +
                   "Perfect for dessert or a sweet break!";
        }
        
        return "What would you like to order? I have Nasi Padang sets (Rp 25k), individual sides (Rp 18-22k), and refreshing drinks (Rp 8-15k). Let me know what you're craving!";
    }

    /**
     * Generate stock info with buyer-friendly urgency and alternatives
     */
    private String generateStockWithUrgency(String message, int requestId, BuyerIntent intent) {
        // Handle confirmation first
        if (message.contains("yes") || message.contains("confirm") || message.contains("ok") || 
            message.contains("deal") || message.contains("sure")) {
            if (pendingOrders.containsKey(requestId)) {
                PendingOrder order = pendingOrders.get(requestId);
                
                if (order.quantity > order.product.getStock()) {
                    return "Oh no! Someone just grabbed the last portions while we were chatting. I only have " + 
                           order.product.getStock() + " " + order.product.getName() + " left now. " +
                           "Should I reserve those " + order.product.getStock() + " for you instead?";
                }

                order.product.deductStock(order.quantity);
                pendingOrders.remove(requestId);
                
                return "✅ Confirmed! I've reserved " + order.quantity + " " + order.product.getName() + 
                       " just for you. Your order is being prepared now! " +
                       "Remaining stock: " + order.product.getStock() + ". See you soon! 🎉";
            } else {
                return "I'd love to confirm, but let me know what you'd like to order first! What are you craving today?";
            }
        }

        // Standard stock check with buyer-centric language
        Product p = detectProduct(message);
        if (p != null) {
            int stock = p.getStock();
            
            if (stock == 0) {
                // Suggest alternative
                String alternative = suggestAlternative(p, intent);
                return "Ah, " + p.getName() + " just sold out! " + alternative;
            }
            else if (stock <= 3) {
                pendingOrders.put(requestId, new PendingOrder(p, extractQuantity(message), intent.preference));
                return "⚡ Only " + stock + " " + p.getName() + " left! They're going fast today. " +
                       "Want me to reserve " + (extractQuantity(message) > stock ? stock : extractQuantity(message)) + 
                       " for you before someone else grabs them?";
            }
            else if (stock <= 10) {
                return "✅ " + p.getName() + " is available (" + stock + " portions left). " +
                       "Popular item today - shall I set aside your order now?";
            }
            else {
                return "✅ Plenty of " + p.getName() + " available (" + stock + " portions). " +
                       "Fresh and ready for you! How many would you like?";
            }
        }

        // General stock inquiry without specific product
        return "Everything on our menu is freshly prepared today! Nasi Padang and drinks are well-stocked. " +
               "What are you in the mood for? I can check specific availability once you let me know!";
    }

    /**
     * Suggest alternative when item is out of stock
     */
    private String suggestAlternative(Product outOfStock, BuyerIntent intent) {
        if (outOfStock.getName().toLowerCase().contains("rendang")) {
            return "But our Gulai Cincang is equally spicy and flavorful, or try Ayam Pop for something milder. Both are available!";
        }
        if (outOfStock.getName().toLowerCase().contains("padang")) {
            return "But I can make you a custom plate with Ayam Pop + Gulai + Rice for Rp 23,000. Same satisfaction!";
        }
        if (intent.sweet) {
            return "But our Es Campur is just as sweet and refreshing, and we have plenty!";
        }
        return "But I have other delicious options available. What else interests you?";
    }

    // Helper methods
    private Product detectProduct(String message) {
        for (Product p : catalog) {
            if (message.contains(p.getName().toLowerCase())) {
                return p;
            }
        }
        return null;
    }

    private int extractQuantity(String message) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    /**
     * Generate all three responses at once for auto-fill
     */
    public String[] generateAllResponses(String buyerMessage, int requestId) {
        BuyerIntent intent = analyzeBuyerIntent(buyerMessage.toLowerCase());
        return new String[] {
            generateBuyerCentricExplanation(buyerMessage, intent),
            generateBuyerFriendlyPricing(buyerMessage, requestId, intent),
            generateStockWithUrgency(buyerMessage, requestId, intent)
        };
    }

    /**
     * Inner class to capture buyer's intent and preferences
     */
    private class BuyerIntent {
        boolean spicy = false;
        boolean sweet = false;
        boolean mild = false;
        boolean heavyMeal = false;
        boolean lightRefreshment = false;
        boolean hungry = false;
        boolean refreshing = false;
        boolean budgetConscious = false;
        boolean premium = false;
        String preference = "general";
    }
}
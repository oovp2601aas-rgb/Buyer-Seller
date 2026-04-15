package main;

import controller.ChatController;
import java.awt.*;
import javax.swing.*;
import ui.BuyerPanel;
import ui.RecommendationPanel;
import ui.SellerPanel;

/**
 * ChatApplication – Entry point
 *
 * Windows:
 *   1  BuyerFrame           – buyer chat
 *   2  RecommendationFrame  – aggregated seller menu recommendations
 *   3  SellerFrame[0]       – Seller 1 dashboard
 *   4  SellerFrame[1]       – Seller 2 dashboard
 *   5  SellerFrame[2]       – Seller 3 dashboard
 *
 * All share 1 ChatController.
 */
public class ChatApplication {

    private ChatController     controller;
    private BuyerFrame         buyerFrame;
    private RecommendationFrame recFrame;
    private SellerFrame[]      sellerFrames = new SellerFrame[3];

    public ChatApplication() {
        controller = new ChatController();
        buyerFrame = new BuyerFrame(controller);
        recFrame   = new RecommendationFrame(controller);      // ← NEW
        for (int i = 0; i < 3; i++) {
            sellerFrames[i] = new SellerFrame(controller, i);
        }
    }

    public void showAll() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int frameW = 520;
        int frameH = 700;

        // ── Buyer — left side, vertically centred ──
        buyerFrame.setSize(frameW, frameH);
        int buyerX = 20;
        int buyerY = (screen.height - frameH) / 2;
        buyerFrame.setLocation(buyerX, buyerY);
        buyerFrame.setVisible(true);

        // ── Recommendation — next to buyer ──
        recFrame.setSize(frameW, frameH);
        recFrame.setLocation(buyerX + frameW + 10, buyerY);
        recFrame.setVisible(true);

        // ── Sellers — right side, stacked / offset ──
        int sellerBaseX = buyerX + frameW + 10 + frameW + 10;
        for (int i = 0; i < 3; i++) {
            sellerFrames[i].setSize(frameW, frameH);
            sellerFrames[i].setLocation(sellerBaseX + i * 25, buyerY + i * 25);
            sellerFrames[i].setVisible(true);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  BuyerFrame
    // ══════════════════════════════════════════════════════════
    static class BuyerFrame extends JFrame {
        BuyerFrame(ChatController controller) {
            setTitle("\uD83D\uDECD Buyer");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            BuyerPanel buyerPanel = new BuyerPanel();
            controller.setBuyerPanel(buyerPanel);
            add(buyerPanel);

            JMenuBar mb = new JMenuBar();
            JMenu fm = new JMenu("File");
            JMenuItem clearItem = new JMenuItem("Clear All Chats");
            clearItem.addActionListener(e -> controller.clearAllChats());
            JMenuItem exitItem = new JMenuItem("Exit");
            exitItem.addActionListener(e -> System.exit(0));
            fm.add(clearItem); fm.addSeparator(); fm.add(exitItem);
            mb.add(fm);
            setJMenuBar(mb);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  RecommendationFrame  ← NEW
    // ══════════════════════════════════════════════════════════
    static class RecommendationFrame extends JFrame {
        RecommendationFrame(ChatController controller) {
            setTitle("\uD83C\uDF7D\uFE0F Recommendations for Buyers");
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            RecommendationPanel rp = new RecommendationPanel();
            controller.setRecommendationPanel(rp);
            add(rp);

            JMenuBar mb = new JMenuBar();
            JMenu fm = new JMenu("File");
            JMenuItem clearItem = new JMenuItem("Clear Recommendations");
            clearItem.addActionListener(e -> rp.clearAll());
            fm.add(clearItem);
            mb.add(fm);
            setJMenuBar(mb);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  SellerFrame
    // ══════════════════════════════════════════════════════════
    static class SellerFrame extends JFrame {
        SellerFrame(ChatController controller, int sellerIndex) {
            setTitle("\uD83D\uDC68\u200D\uD83C\uDF73 Seller " + (sellerIndex + 1));
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            SellerPanel sellerPanel = new SellerPanel(sellerIndex);
            controller.addSellerPanel(sellerPanel);
            add(sellerPanel);

            JMenuBar mb = new JMenuBar();
            JMenu fm = new JMenu("File");
            JMenuItem clearItem = new JMenuItem("Clear My Requests");
            clearItem.addActionListener(e -> sellerPanel.clearAllRequests());
            fm.add(clearItem);
            mb.add(fm);
            setJMenuBar(mb);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Main
    // ══════════════════════════════════════════════════════════
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ChatApplication app = new ChatApplication();
            app.showAll();

            System.out.println("==============================================");
            System.out.println("Food Chat System — Multi Window");
            System.out.println("  Buyer Frame           : 1");
            System.out.println("  Recommendation Frame  : 1  ← NEW");
            System.out.println("  Seller Frames         : 3");
            System.out.println("  Controller            : 1 shared ChatController");
            System.out.println("==============================================");
        });
    }
}
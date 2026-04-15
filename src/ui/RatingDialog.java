package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import model.OrderHistory.Transaction;

/**
 * RatingDialog - Dialog for giving a 1-5 star rating + comment
 * on a transaction. Called from OrderHistoryDialog.
 */
public class RatingDialog extends JDialog {

    private static final Color COL_BG     = Color.WHITE;
    private static final Color COL_HEADER = new Color(103, 58, 183);
    private static final Color COL_STAR   = new Color(255, 193, 7);   // yellow
    private static final Color COL_LINE   = new Color(220, 220, 220);

    private static final Font FONT_TITLE  = new Font("Segoe UI Emoji", Font.BOLD, 17);
    private static final Font FONT_BOLD   = new Font("Segoe UI Emoji", Font.BOLD, 14);
    private static final Font FONT_BODY   = new Font("Dialog", Font.PLAIN, 14);
    private static final Font FONT_STAR   = new Font("Dialog", Font.PLAIN, 36);
    private static final Font FONT_SMALL  = new Font("Dialog", Font.PLAIN, 12);

    private final Transaction tx;
    private final Runnable    onSaved;   // callback to refresh OrderHistoryDialog

    private int      selectedRating = 0;
    private JLabel[] stars          = new JLabel[5];
    private JTextArea reviewArea;

    public RatingDialog(Frame owner, Transaction tx, Runnable onSaved) {
        super(owner, "\u2B50 Give Rating", true);
        this.tx      = tx;
        this.onSaved = onSaved;

        // If already rated, start from the previous value
        this.selectedRating = tx.rating;

        setSize(420, 460);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(COL_BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setBackground(COL_HEADER);
        p.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel lbl = new JLabel("\u2B50  Give Rating & Review");
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(Color.WHITE);
        p.add(lbl);
        return p;
    }

    // ── Body ──────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(COL_BG);
        body.setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));

        // Transaction info
        JLabel txLbl = new JLabel(tx.txId + "  •  " + tx.date);
        txLbl.setFont(FONT_SMALL);
        txLbl.setForeground(Color.GRAY);
        txLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(txLbl);

        body.add(Box.createVerticalStrut(20));

        // Rating question
        JLabel question = new JLabel("How satisfied are you with this order?");
        question.setFont(FONT_BOLD);
        question.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(question);

        body.add(Box.createVerticalStrut(16));

        // ── Stars ──
        JPanel starRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        starRow.setBackground(COL_BG);
        starRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (int i = 0; i < 5; i++) {
            final int starVal = i + 1;
            stars[i] = new JLabel("\u2B50");
            stars[i].setFont(FONT_STAR);
            stars[i].setForeground(COL_STAR);
            stars[i].setCursor(new Cursor(Cursor.HAND_CURSOR));

            stars[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    highlightStars(starVal); // hover preview
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    highlightStars(selectedRating); // revert to selection
                }
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedRating = starVal;
                    highlightStars(selectedRating);
                }
            });

            starRow.add(stars[i]);
        }

        body.add(starRow);

        // Rating text label
        JLabel ratingLbl = new JLabel(" ");
        ratingLbl.setFont(FONT_SMALL);
        ratingLbl.setForeground(COL_HEADER);
        ratingLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(Box.createVerticalStrut(6));
        body.add(ratingLbl);

        // Update label when star is clicked
        for (int i = 0; i < 5; i++) {
            final int starVal = i + 1;
            stars[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ratingLbl.setText(ratingLabel(starVal));
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    ratingLbl.setText(ratingLabel(starVal));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    ratingLbl.setText(ratingLabel(selectedRating));
                }
            });
        }

        // Initialize stars from previous rating (if any)
        highlightStars(selectedRating);
        ratingLbl.setText(ratingLabel(selectedRating));

        body.add(Box.createVerticalStrut(20));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(COL_LINE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        body.add(sep);

        body.add(Box.createVerticalStrut(16));

        // Comment
        JLabel reviewLbl = new JLabel("Comment (optional):");
        reviewLbl.setFont(FONT_BOLD);
        reviewLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(reviewLbl);

        body.add(Box.createVerticalStrut(8));

        reviewArea = new JTextArea(tx.review); // pre-fill with previous review if any
        reviewArea.setFont(FONT_BODY);
        reviewArea.setLineWrap(true);
        reviewArea.setWrapStyleWord(true);
        reviewArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        reviewArea.setBackground(new Color(248, 245, 255));

        JScrollPane sp = new JScrollPane(reviewArea);
        sp.setBorder(BorderFactory.createLineBorder(new Color(200, 180, 240), 1, true));
        sp.setPreferredSize(new Dimension(360, 90));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(sp);

        return body;
    }

    // ── Footer ────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        p.setBackground(COL_BG);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COL_LINE));

        // Cancel
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(FONT_BOLD);
        cancelBtn.setForeground(Color.GRAY);
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COL_LINE, 1, true),
                BorderFactory.createEmptyBorder(10, 24, 10, 24)));
        cancelBtn.addActionListener(e -> dispose());

        // Save
        JButton saveBtn = new JButton("Save Rating");
        saveBtn.setFont(FONT_BOLD);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(COL_HEADER);
        saveBtn.setOpaque(true);
        saveBtn.setContentAreaFilled(true);
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        saveBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { saveBtn.setBackground(COL_HEADER.darker()); }
            public void mouseExited (MouseEvent e) { saveBtn.setBackground(COL_HEADER); }
        });

        saveBtn.addActionListener(e -> {
            if (selectedRating == 0) {
                JOptionPane.showMessageDialog(this,
                        "Please select a star rating first!",
                        "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // Save to transaction
            tx.rating = selectedRating;
            tx.review = reviewArea.getText().trim();

            // Callback to refresh OrderHistoryDialog
            if (onSaved != null) onSaved.run();

            JOptionPane.showMessageDialog(this,
                    "Rating " + tx.getStarString() + " saved successfully!",
                    "Thank You!", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        p.add(cancelBtn);
        p.add(saveBtn);
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────

    /** Highlight stars 1..val as filled, the rest as empty */
    private void highlightStars(int val) {
        for (int i = 0; i < 5; i++) {
            stars[i].setText(i < val ? "\u2605" : "\u2606");
        }
    }

    private String ratingLabel(int val) {
        switch (val) {
            case 1: return "Very Bad";
            case 2: return "Bad";
            case 3: return "Average";
            case 4: return "Good";
            case 5: return "Excellent!";
            default: return " ";
        }
    }

    // ── Static factory ────────────────────────────────────────
    public static void show(Component parent, Transaction tx, Runnable onSaved) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(parent);
        new RatingDialog(owner, tx, onSaved).setVisible(true);
    }
}
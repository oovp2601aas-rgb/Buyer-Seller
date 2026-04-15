package ui;

import controller.ChatController;
import java.awt.*;
import javax.swing.*;
import model.ChatRequest;
import service.GeminiAIService;

public class RequestPanel extends JPanel {
    private ChatRequest    request;
    private ChatController controller;
    private int            sellerIndex;

    private JTextArea[] formFields = new JTextArea[3];

    private static final String[] FORM_LABELS = {
        "Form 1", "Form 2", "Form 3"
    };

    private static final Color[] SELLER_COLORS = {
        new Color(33, 150, 243),
        new Color(0,  150, 136),
        new Color(233, 30, 99)
    };

    public RequestPanel(ChatRequest request, ChatController controller, int sellerIndex) {
        this.request     = request;
        this.controller  = controller;
        this.sellerIndex = sellerIndex;
        initComponents();
    }

    public RequestPanel(ChatRequest request, ChatController controller) {
        this(request, controller, 0);
    }

    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        setPreferredSize(new Dimension(450, 390));
        setMinimumSize(new Dimension(400, 390));

        JPanel lp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        lp.setBackground(Color.WHITE);
        lp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        JLabel rl = new JLabel(request.getRequestLabel());
        rl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lp.add(rl);
        add(lp);

        add(Box.createVerticalStrut(4));

        JPanel mp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        mp.setBackground(Color.WHITE);
        mp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        JLabel ml = new JLabel(request.getBuyerMessage());
        ml.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ml.setForeground(new Color(60, 60, 60));
        mp.add(ml);
        add(mp);

        add(Box.createVerticalStrut(10));

        for (int i = 0; i < 3; i++) {
            add(createFormRow(i));
            add(Box.createVerticalStrut(6));
        }

        add(Box.createVerticalStrut(8));
        add(createAIButton());
    }

    private JPanel createAIButton() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        Color sellerColor = sellerIndex < SELLER_COLORS.length
            ? SELLER_COLORS[sellerIndex]
            : new Color(106, 27, 154);

        JButton btnAI = new JButton("AI Suggest");
        btnAI.setBackground(sellerColor);
        btnAI.setForeground(Color.WHITE);
        btnAI.setFont(new Font("Arial", Font.BOLD, 13));
        btnAI.setFocusPainted(false);
        btnAI.setOpaque(true);
        btnAI.setContentAreaFilled(true);
        btnAI.setBorderPainted(false);
        btnAI.setPreferredSize(new Dimension(160, 35));
        btnAI.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAI.addActionListener(e -> {
            String buyerMessage = request.getBuyerMessage();
            if (buyerMessage == null || buyerMessage.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No buyer request yet.",
                    "AI Suggest", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            btnAI.setEnabled(false);
            btnAI.setText("Loading...");

            new Thread(() -> {
                try {
                    String[][] opts = GeminiAIService.generateOptions(buyerMessage, sellerIndex);

                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < 3; i++) {
                            final int formIdx = i;
                            String[] choices = {opts[i][0], opts[i][1], "Skip"};
                            int choice = JOptionPane.showOptionDialog(
                                null,
                                "Pilih rekomendasi untuk Form " + (i + 1) + ":",
                                "AI Suggest - Form " + (i + 1),
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                choices,
                                choices[0]
                            );
                            if (choice == 0 || choice == 1) {
                                formFields[formIdx].setText(opts[formIdx][choice]);
                            }
                        }

                        btnAI.setEnabled(true);
                        btnAI.setText("AI Suggest");
                    });

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null,
                            "AI error: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                        btnAI.setEnabled(true);
                        btnAI.setText("AI Suggest");
                    });
                }
            }).start();
        });

        panel.add(btnAI);
        return panel;
    }

    private JPanel createFormRow(int idx) {
        int formNumber = idx + 1;

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        row.setPreferredSize(new Dimension(420, 70));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel fl = new JLabel(FORM_LABELS[idx]);
        fl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fl.setPreferredSize(new Dimension(55, 32));
        fl.setForeground(new Color(130, 130, 130));
        row.add(fl, BorderLayout.WEST);

        formFields[idx] = new JTextArea();
        formFields[idx].setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formFields[idx].setBackground(new Color(245, 255, 245));
        formFields[idx].setLineWrap(true);
        formFields[idx].setWrapStyleWord(true);
        formFields[idx].setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JScrollPane sp = new JScrollPane(formFields[idx]);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 235, 210), 1, true));
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        row.add(sp, BorderLayout.CENTER);

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        bp.setBackground(Color.WHITE);

        CircularButton sub = new CircularButton("➤", new Color(0, 150, 136), 32);
        sub.setToolTipText("Submit Form " + formNumber);
        final int fi = formNumber;
        sub.addActionListener(ev -> handleSubmit(fi));
        bp.add(sub);

        row.add(bp, BorderLayout.EAST);
        return row;
    }

    private void handleSubmit(int formIndex) {
        int idx = formIndex - 1;
        if (idx < 0 || idx >= formFields.length) return;
        String v = formFields[idx].getText().trim();
        if (!v.isEmpty()) {
            controller.onSellerFormSubmit(request.getRequestId(), formIndex, v, sellerIndex);
        }
    }

    public void fillForm(int formIndex, String value) {
        int idx = formIndex - 1;
        if (idx >= 0 && idx < formFields.length && formFields[idx] != null) {
            formFields[idx].setText(value);
        }
    }

    public ChatRequest getRequest() { return request; }

    public void updateRequest(ChatRequest r) {
        this.request = r;
        if (r.getProductExplanation() != null
                && formFields[0] != null
                && formFields[0].getText().isEmpty()) {
            formFields[0].setText(r.getProductExplanation());
        }
    }
}
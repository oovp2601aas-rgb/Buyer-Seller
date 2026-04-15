package ui;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

public class Bubble extends JPanel {
    public enum BubbleType { BUYER, WAITING, SELLER }
    private String message;
    private BubbleType type;
    private int requestId=-1, formIndex=-1;
    private controller.ChatController controller;
    private static final Color COLOR_BUYER=new Color(227,242,253),COLOR_WAITING=new Color(255,253,231),COLOR_SELLER=new Color(232,245,233),COLOR_BORDER=new Color(230,230,230);
    private static final int BUBBLE_WIDTH=480;
    private int quantity=1;
    private double unitPrice=0.0;
    private JLabel qtyLabel,priceLabel;

    public Bubble(String message,BubbleType type){
        this.message=message; this.type=type;
        if(type==BubbleType.SELLER) this.unitPrice=parsePrice(message);
        initComponents();
    }
    public void setController(controller.ChatController ctrl){this.controller=ctrl;}

    private double parsePrice(String msg){
        if(msg==null)return 0;
        String m=msg.toLowerCase();
        Matcher rp=Pattern.compile("(?i)rp\\.?\\s*([\\d.,]+)").matcher(m);
        if(rp.find()){String raw=rp.group(1).replace(".","").replace(",","");try{return Double.parseDouble(raw);}catch(NumberFormatException ignored){}}
        Matcher k=Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:k|rb|ribu)").matcher(m);
        if(k.find()){String raw=k.group(1).replace(",",".");try{return Double.parseDouble(raw)*1000;}catch(NumberFormatException ignored){}}
        return 0;
    }

    private String formatPrice(double amount){
        if(unitPrice>=1000){
            long val=Math.round(amount); String raw=String.valueOf(val);
            StringBuilder sb=new StringBuilder(); int start=raw.length()%3;
            if(start>0)sb.append(raw,0,start);
            for(int i=start;i<raw.length();i+=3){if(sb.length()>0)sb.append(".");sb.append(raw,i,i+3);}
            return "Rp "+sb.toString();
        }
        return String.format("$%.2f",amount);
    }

    private void initComponents(){
        setOpaque(false);
        setLayout(type==BubbleType.BUYER?new FlowLayout(FlowLayout.RIGHT,10,5):new FlowLayout(FlowLayout.LEFT,10,5));
        JPanel bubbleContent=new JPanel(){
            @Override protected void paintComponent(Graphics g){
                Color bg; switch(type){case BUYER:bg=COLOR_BUYER;break;case WAITING:bg=COLOR_WAITING;break;default:bg=COLOR_SELLER;break;}
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g2.setColor(COLOR_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                g2.dispose(); super.paintComponent(g);
            }
        };
        bubbleContent.setOpaque(false); bubbleContent.setLayout(new BorderLayout());
        bubbleContent.setBorder(BorderFactory.createEmptyBorder(12,18,12,18));
        String fontFamily=(type==BubbleType.WAITING)?"Segoe UI Emoji, Apple Color Emoji, Noto Color Emoji, Segoe UI":"Segoe UI";
        String htmlMsg="<html><body style='width:"+BUBBLE_WIDTH+"px; font-size:13pt; font-family:"+fontFamily+";'>"+message.replace("\n","<br>")+"</body></html>";
        JLabel messageLabel=new JLabel(htmlMsg);
        messageLabel.setFont(new Font(type==BubbleType.WAITING?"Segoe UI Emoji":"Segoe UI",Font.PLAIN,14));
        messageLabel.setForeground(Color.BLACK);
        bubbleContent.add(messageLabel,BorderLayout.CENTER);
        if(type==BubbleType.SELLER) bubbleContent.add(buildSellerBottom(),BorderLayout.SOUTH);
        add(bubbleContent);
    }

    private JPanel buildSellerBottom(){
        JPanel wrapper=new JPanel(new BorderLayout(8,0)); wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(6,0,0,0));

        // Price label (left)
        if(unitPrice>0){
            priceLabel=new JLabel("Total: "+formatPrice(unitPrice));
            priceLabel.setFont(new Font("Segoe UI",Font.BOLD,13)); priceLabel.setForeground(new Color(27,94,32));
            wrapper.add(priceLabel, BorderLayout.WEST);
        }

        // Qty stepper (right) — no Choose button
        JPanel qtyPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT,4,0)); qtyPanel.setOpaque(false);
        JButton minusBtn=createStepperBtn("−");
        qtyLabel=new JLabel("1"); qtyLabel.setFont(new Font("Segoe UI",Font.BOLD,15));
        qtyLabel.setForeground(new Color(33,37,41)); qtyLabel.setPreferredSize(new Dimension(26,30));
        qtyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JButton plusBtn=createStepperBtn("+");
        minusBtn.addActionListener(e->{if(quantity>1){quantity--;refreshQty();}});
        plusBtn.addActionListener(e->{quantity++;refreshQty();});
        qtyPanel.add(minusBtn); qtyPanel.add(qtyLabel); qtyPanel.add(plusBtn);
        wrapper.add(qtyPanel, BorderLayout.EAST);

        return wrapper;
    }

    private JButton createStepperBtn(String text){
        JButton btn=new JButton(text); btn.setFont(new Font("Segoe UI",Font.BOLD,14));
        btn.setPreferredSize(new Dimension(30,30)); btn.setMinimumSize(new Dimension(30,30)); btn.setMaximumSize(new Dimension(30,30));
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(245,245,245)); btn.setForeground(new Color(33,37,41));
        btn.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(new Color(180,180,180),8),BorderFactory.createEmptyBorder(2,4,2,4)));
        return btn;
    }

    private void refreshQty(){
        if(qtyLabel!=null)qtyLabel.setText(String.valueOf(quantity));
        if(priceLabel!=null&&unitPrice>0)priceLabel.setText("Total: "+formatPrice(unitPrice*quantity));
        revalidate(); repaint();
    }

    public void setRequestId(int id){this.requestId=id;} public int getRequestId(){return requestId;}
    public void setFormIndex(int idx){this.formIndex=idx;} public int getFormIndex(){return formIndex;}
    public String getText(){return message;} public BubbleType getType(){return type;}
    @Override public Dimension getMaximumSize(){return new Dimension(Integer.MAX_VALUE,getPreferredSize().height);}

    public static class RoundedBorder extends AbstractBorder {
        private final Color color; private final int radius;
        public RoundedBorder(Color color,int radius){this.color=color;this.radius=radius;}
        @Override public void paintBorder(Component c,Graphics g,int x,int y,int width,int height){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.drawRoundRect(x,y,width-1,height-1,radius,radius); g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c){return new Insets(6,6,6,6);}
    }
}
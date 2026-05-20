package com.project.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage; // 💡 가상 도화지 클래스 수입
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.project.client.ClientMain;

public class UiMain {
    private JTextArea chatArea;
    private JPanel canvasPanel; 
    private ClientMain client;
    
    private int lastX, lastY; 
    private String currentColor = "BLACK"; 
    
    // 💡 화면 뒤에 숨겨진 찐짜 '순백색 가상 도화지' 변수
    private BufferedImage canvasImage; 

    public UiMain() {
        client = new ClientMain(this); 
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("실시간 캐치마인드");
        frame.setSize(900, 600); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 💡 1. 2000x2000 짜리 거대한 가상 도화지를 만들고 순백색으로 쫙 칠해둡니다.
        canvasImage = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvasImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 2000, 2000);
        g2d.dispose();

        // 💡 2. 화면 패널은 가상 도화지(canvasImage)를 그대로 복사해서 보여주기만 합니다.
        canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // 회색 찌꺼기 방지
                g.drawImage(canvasImage, 0, 0, null); // 가상 도화지 출력
            }
        };
        
        canvasPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }
        });

        canvasPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int currentX = e.getX();
                int currentY = e.getY();

                drawRemoteLine(lastX, lastY, currentX, currentY, currentColor);
                client.sendMessage("[DRAW]," + lastX + "," + lastY + "," + currentX + "," + currentY + "," + currentColor);

                lastX = currentX;
                lastY = currentY;
            }
        });
        frame.add(canvasPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setSize(300, 600);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        rightPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JTextField inputField = new JTextField(8);
        JButton actionBtn = new JButton("접속");
        
        JButton blackBtn = new JButton("검");
        JButton redBtn = new JButton("빨");
        JButton blueBtn = new JButton("파");
        JButton eraserBtn = new JButton("지우개"); 
        JButton clearBtn = new JButton("전체지움");

        blackBtn.addActionListener(e -> currentColor = "BLACK");
        redBtn.addActionListener(e -> currentColor = "RED");
        blueBtn.addActionListener(e -> currentColor = "BLUE");
        eraserBtn.addActionListener(e -> currentColor = "WHITE"); 
        
        clearBtn.addActionListener(e -> client.sendMessage("[CLEAR]"));

        actionBtn.addActionListener(e -> {
            String text = inputField.getText();
            if (text.isEmpty()) return;

            if (actionBtn.getText().equals("접속")) {
                client.connect("127.0.0.1", 8000, text);
                actionBtn.setText("전송");
                inputField.setText("");
            } else {
                client.sendMessage("[CHAT]," + text);
                inputField.setText("");
            }
        });

        bottomPanel.add(inputField);
        bottomPanel.add(actionBtn);
        bottomPanel.add(blackBtn);
        bottomPanel.add(redBtn);
        bottomPanel.add(blueBtn);
        bottomPanel.add(eraserBtn);
        bottomPanel.add(clearBtn);
        
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(rightPanel, BorderLayout.EAST); 

        frame.setVisible(true);
    }

    // 💡 선 긋기 메서드 진화: 화면이 아니라 가상 도화지에 그립니다.
    public void drawRemoteLine(int x1, int y1, int x2, int y2, String colorName) {
        Graphics2D g2d = canvasImage.createGraphics();
        
        if (colorName.equals("RED")) {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3)); 
        } else if (colorName.equals("BLUE")) {
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(3));
        } else if (colorName.equals("WHITE")) {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(20)); // 지우개는 아주 두껍게!
        } else {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(3));
        }
        
        g2d.drawLine(x1, y1, x2, y2); 
        g2d.dispose(); 
        
        canvasPanel.repaint(); // 도화지에 그렸으니 화면을 새로고침!
    }

    // 💡 지우기 메서드 진화: 가상 도화지를 순백색으로 덮어버립니다.
    public void clearCanvas() {
        Graphics2D g2d = canvasImage.createGraphics();
        g2d.setColor(Color.WHITE); 
        g2d.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight()); 
        g2d.dispose();
        
        canvasPanel.repaint(); // 도화지 지웠으니 화면 새로고침!
    }

    public void appendChat(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength()); 
    }

    public static void main(String[] args) {
        new UiMain();
    }
}
package com.project.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage; 
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
    
    private BufferedImage canvasImage; 

    public UiMain() {
        client = new ClientMain(this); 
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("실시간 캐치마인드");
        // 💡 IP 입력칸이 추가되어 공간이 필요하므로 가로 길이를 1000으로 늘렸습니다.
        frame.setSize(1000, 600); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        canvasImage = new BufferedImage(2000, 2000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvasImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 2000, 2000);
        g2d.dispose();

        canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); 
                g.drawImage(canvasImage, 0, 0, null); 
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
        
        // 💡 IP를 입력받을 텍스트 필드 추가 (기본값으로 내 컴퓨터 주소인 127.0.0.1 세팅)
        JTextField ipField = new JTextField("127.0.0.1", 9); 
        JTextField inputField = new JTextField(7); // 닉네임 및 채팅 입력칸
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
            String ip = ipField.getText(); // 💡 입력된 IP 가져오기

            // 닉네임이나 IP 칸이 비어있으면 아무 일도 안 함
            if (text.isEmpty() || ip.isEmpty()) return; 

            if (actionBtn.getText().equals("접속")) {
                // 💡 코드가 아닌 '화면에 적힌 IP'로 접속하도록 변경!
                client.connect(ip, 8000, text);
                
                actionBtn.setText("전송");
                inputField.setText("");
                ipField.setEnabled(false); // 💡 접속 후에는 IP를 수정 못하게 비활성화
            } else {
                client.sendMessage("[CHAT]," + text);
                inputField.setText("");
            }
        });

        // 💡 IP 입력칸을 하단 패널 제일 왼쪽에 추가합니다.
        bottomPanel.add(ipField);
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
            g2d.setStroke(new BasicStroke(20)); 
        } else {
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(3));
        }
        
        g2d.drawLine(x1, y1, x2, y2); 
        g2d.dispose(); 
        
        canvasPanel.repaint(); 
    }

    public void clearCanvas() {
        Graphics2D g2d = canvasImage.createGraphics();
        g2d.setColor(Color.WHITE); 
        g2d.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight()); 
        g2d.dispose();
        
        canvasPanel.repaint(); 
    }

    public void appendChat(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength()); 
    }

    public static void main(String[] args) {
        new UiMain();
    }
}
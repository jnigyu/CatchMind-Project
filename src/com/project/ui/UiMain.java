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
    private JFrame frame; // 타이틀 변경을 위해 멤버 변수로 분리
    private JTextArea chatArea;
    private JPanel canvasPanel; 
    private ClientMain client;
    private JButton readyStartBtn; // 💡 게임 제어 버튼
    
    private int lastX, lastY; 
    private String currentColor = "BLACK"; 
    private BufferedImage canvasImage; 

    // 💡 출제자 권한 통제 변수 (기본값 false로 그림 차단)
    private boolean isDrawer = false; 
    private boolean amIReady = false; 

    public UiMain() {
        client = new ClientMain(this); 
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("실시간 캐치마인드 - 대기실");
        frame.setSize(1040, 600); 
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
                if (!isDrawer) return; // 💡 출제자가 아니면 클릭 차단
                lastX = e.getX();
                lastY = e.getY();
            }
        });

        canvasPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isDrawer) return; // 💡 출제자가 아니면 마우스 드래그 조작 차단!

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
        
        JTextField ipField = new JTextField("127.0.0.1", 9); 
        JTextField inputField = new JTextField(7); 
        JButton actionBtn = new JButton("접속");
        
        // 버튼 리스트
        readyStartBtn = new JButton("준비");
        readyStartBtn.setEnabled(false); // 접속 전까지 잠금
        
        JButton blackBtn = new JButton("검");
        JButton redBtn = new JButton("빨");
        JButton blueBtn = new JButton("파");
        JButton eraserBtn = new JButton("지우개"); 
        JButton clearBtn = new JButton("전체지움");

        // 💡 버튼들도 출제자일 때만 반응하도록 통제 적용
        blackBtn.addActionListener(e -> { if(isDrawer) currentColor = "BLACK"; });
        redBtn.addActionListener(e -> { if(isDrawer) currentColor = "RED"; });
        blueBtn.addActionListener(e -> { if(isDrawer) currentColor = "BLUE"; });
        eraserBtn.addActionListener(e -> { if(isDrawer) currentColor = "WHITE"; }); 
        clearBtn.addActionListener(e -> { if(isDrawer) client.sendMessage("[CLEAR]"); });

        // 💡 준비 / 시작 버튼 이벤트 로직
        readyStartBtn.addActionListener(e -> {
            String state = readyStartBtn.getText();
            if (state.equals("준비")) {
                amIReady = true;
                client.sendMessage("[READY],true");
                readyStartBtn.setText("준비완료");
            } else if (state.equals("준비완료")) {
                amIReady = false;
                client.sendMessage("[READY],false");
                readyStartBtn.setText("준비");
            } else if (state.equals("시작")) {
                client.sendMessage("[START]"); // 💡 시작 버튼 상태일 때 누르면 게임 시작 패킷 발사!
            }
        });

        actionBtn.addActionListener(e -> {
            String text = inputField.getText();
            String ip = ipField.getText(); 

            if (text.isEmpty() || ip.isEmpty()) return; 

            if (actionBtn.getText().equals("접속")) {
                client.connect(ip, 8000, text);
                actionBtn.setText("전송");
                inputField.setText("");
                ipField.setEnabled(false); 
                readyStartBtn.setEnabled(true); // 접속 성공했으니 준비 버튼 잠금 해제
            } else {
                client.sendMessage("[CHAT]," + text);
                inputField.setText("");
            }
        });

        bottomPanel.add(ipField);
        bottomPanel.add(inputField);
        bottomPanel.add(actionBtn);
        bottomPanel.add(readyStartBtn); // 제어 버튼 부착
        bottomPanel.add(blackBtn);
        bottomPanel.add(redBtn);
        bottomPanel.add(blueBtn);
        bottomPanel.add(eraserBtn);
        bottomPanel.add(clearBtn);
        
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(rightPanel, BorderLayout.EAST); 

        frame.setVisible(true);
    }

    // 💡 네트워크 신호에 따라 버튼 글씨 제어하는 유틸리티 공장들
    public void changeToStartButton() { readyStartBtn.setText("시작"); }
    public void changeToReadyButton() { 
        if(amIReady) readyStartBtn.setText("준비완료");
        else readyStartBtn.setText("준비");
    }
    public void resetReadyState() {
        amIReady = false;
        isDrawer = false;
        readyStartBtn.setText("준비");
        frame.setTitle("실시간 캐치마인드 - 대기실");
    }

    // 💡 출제자 모드 전환 및 타이틀 알림 알람 기능
    public void setGameRole(boolean drawerMode, String titleMessage) {
        this.isDrawer = drawerMode;
        frame.setTitle("실시간 캐치마인드 - " + titleMessage);
        clearCanvas(); // 게임 시작 시 도화지 한번 청소해 주는 센스
    }

    public void drawRemoteLine(int x1, int y1, int x2, int y2, String colorName) {
        Graphics2D g2d = canvasImage.createGraphics();
        if (g2d != null) {
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
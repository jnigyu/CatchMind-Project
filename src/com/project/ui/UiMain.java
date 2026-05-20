package com.project.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage; 
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel; 
import javax.swing.JOptionPane; 
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider; // 💡 슬라이더 수입
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.project.client.ClientMain;

public class UiMain {
    private JFrame frame; 
    private JTextArea chatArea;
    private JPanel canvasPanel; 
    private ClientMain client;
    private JButton readyStartBtn; 
    private JLabel timerLabel; 
    
    // 💡 전역 변수로 승격하여 잠금 처리할 수 있게 변경
    private JTextField inputField; 
    
    private int lastX, lastY; 
    private String currentColor = "BLACK"; 
    private int currentThickness = 3; // 💡 펜의 기본 두께 설정
    private BufferedImage canvasImage; 

    private boolean isDrawer = false; 
    private boolean amIReady = false; 
    private int savedReady = 0;
    private int savedTotal = 0;

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
        canvasPanel.setLayout(new BorderLayout()); 
        
        JPanel topTimerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topTimerPanel.setOpaque(false); 
        timerLabel = new JLabel("대기 중.. ⏱️");
        timerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        timerLabel.setForeground(Color.RED); 
        topTimerPanel.add(timerLabel);
        canvasPanel.add(topTimerPanel, BorderLayout.NORTH); 
        
        canvasPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isDrawer) return; 
                lastX = e.getX();
                lastY = e.getY();
            }
        });

        canvasPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isDrawer) return; 

                int currentX = e.getX();
                int currentY = e.getY();

                drawRemoteLine(lastX, lastY, currentX, currentY, currentColor, currentThickness);
                // 💡 좌표 통신 시 두께(currentThickness) 데이터도 함께 발송!
                client.sendMessage("[DRAW]," + lastX + "," + lastY + "," + currentX + "," + currentY + "," + currentColor + "," + currentThickness);

                lastX = currentX;
                lastY = currentY;
            }
        });
        
        // 💡 [도화지 하단 툴바 패널 생성] - 그리기 도구를 채팅창에서 분리
        JPanel drawToolPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        drawToolPanel.setBackground(Color.LIGHT_GRAY);
        
        JButton blackBtn = new JButton("검");
        JButton redBtn = new JButton("빨");
        JButton blueBtn = new JButton("파");
        JButton greenBtn = new JButton("초"); // 색상 추가
        JButton yellowBtn = new JButton("노"); // 색상 추가
        JButton eraserBtn = new JButton("지우개"); 
        JButton clearBtn = new JButton("전체지움");
        
        // 💡 1부터 30까지 조절 가능한 두께 슬라이더 (기본값 3)
        JSlider thicknessSlider = new JSlider(1, 30, 3);
        thicknessSlider.setBackground(Color.LIGHT_GRAY);
        
        blackBtn.addActionListener(e -> currentColor = "BLACK");
        redBtn.addActionListener(e -> currentColor = "RED");
        blueBtn.addActionListener(e -> currentColor = "BLUE");
        greenBtn.addActionListener(e -> currentColor = "GREEN");
        yellowBtn.addActionListener(e -> currentColor = "YELLOW");
        
        // 지우개를 누르면 하얀색으로 바뀌며 자동으로 두께를 20으로 굵게 세팅해줌
        eraserBtn.addActionListener(e -> { 
            currentColor = "WHITE"; 
            thicknessSlider.setValue(20); 
        }); 
        clearBtn.addActionListener(e -> { if(isDrawer) client.sendMessage("[CLEAR]"); });
        
        // 슬라이더를 움직일 때마다 현재 두께 변수 업데이트
        thicknessSlider.addChangeListener(e -> currentThickness = thicknessSlider.getValue());

        drawToolPanel.add(new JLabel("색상: "));
        drawToolPanel.add(blackBtn);
        drawToolPanel.add(redBtn);
        drawToolPanel.add(blueBtn);
        drawToolPanel.add(greenBtn);
        drawToolPanel.add(yellowBtn);
        drawToolPanel.add(new JLabel(" | "));
        drawToolPanel.add(eraserBtn);
        drawToolPanel.add(clearBtn);
        drawToolPanel.add(new JLabel(" | 펜 굵기: "));
        drawToolPanel.add(thicknessSlider);

        canvasPanel.add(drawToolPanel, BorderLayout.SOUTH);
        frame.add(canvasPanel, BorderLayout.CENTER);

        // -------------------------------------------------------------

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setSize(300, 600);
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        rightPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        
        JTextField ipField = new JTextField("127.0.0.1", 9); 
        inputField = new JTextField(7); 
        JButton actionBtn = new JButton("접속");
        
        readyStartBtn = new JButton("준비 (0/0)"); 
        readyStartBtn.setEnabled(false); 

        readyStartBtn.addActionListener(e -> {
            String state = readyStartBtn.getText();
            if (state.startsWith("준비")) {
                if (!amIReady) {
                    amIReady = true;
                    client.sendMessage("[READY],true");
                    readyStartBtn.setText("준비완료");
                } else {
                    amIReady = false;
                    client.sendMessage("[READY],false");
                    readyStartBtn.setText("준비 (" + savedReady + "/" + savedTotal + ")");
                }
            } else if (state.equals("준비완료")) {
                amIReady = false;
                client.sendMessage("[READY],false");
            } else if (state.equals("시작")) {
                client.sendMessage("[START]"); 
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
                readyStartBtn.setEnabled(true); 
            } else {
                client.sendMessage("[CHAT]," + text);
                inputField.setText("");
            }
        });

        // 💡 하단 패널이 아주 심플해졌습니다.
        bottomPanel.add(ipField);
        bottomPanel.add(inputField);
        bottomPanel.add(actionBtn);
        bottomPanel.add(readyStartBtn); 
        
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(rightPanel, BorderLayout.EAST); 

        frame.setVisible(true);
    }

    public void updateReadyButtonText(int ready, int total) {
        this.savedReady = ready;
        this.savedTotal = total;
        if (readyStartBtn.getText().equals("시작")) return; 
        
        if (amIReady) {
            readyStartBtn.setText("준비완료");
        } else {
            readyStartBtn.setText("준비 (" + ready + "/" + total + ")");
        }
    }

    public void changeToStartButton() { readyStartBtn.setText("시작"); }
    
    public void resetReadyState() {
        amIReady = false;
        isDrawer = false;
        readyStartBtn.setText("준비 (0/0)");
        timerLabel.setText("대기 중.. ⏱️");
        frame.setTitle("실시간 캐치마인드 - 대기실");
        
        // 💡 대기실로 돌아가면 다시 채팅 잠금 해제
        inputField.setEnabled(true);
        inputField.setText("");
    }

    public void updateTimerLabel(String seconds) {
        timerLabel.setText("남은 시간: " + seconds + "초 ⏳");
    }

    public void showFinalScoreDialog(String winner, String rankings) {
        timerLabel.setText("게임 종료 🏁");
        JOptionPane.showMessageDialog(frame, 
            "🏆 최종 우승자: [" + winner + "] 님!! 🏆\n\n📊 [최종 등수 나열]\n" + rankings, 
            "게임 결과 발표", 
            JOptionPane.INFORMATION_MESSAGE);
        resetReadyState();
    }

    public void setGameRole(boolean drawerMode, String titleMessage) {
        this.isDrawer = drawerMode;
        frame.setTitle("실시간 캐치마인드 - " + titleMessage);
        
        // 💡 출제자인 경우 채팅 입력창을 잠그고 경고 문구 표시
        if (drawerMode) {
            inputField.setEnabled(false);
            inputField.setText("출제자 채팅금지");
        } else {
            inputField.setEnabled(true);
            inputField.setText("");
        }
        
        clearCanvas(); 
    }

    // 💡 두께(thickness) 파라미터 수신 및 적용
    public void drawRemoteLine(int x1, int y1, int x2, int y2, String colorName, int thickness) {
        Graphics2D g2d = canvasImage.createGraphics();
        if (g2d != null) {
            switch(colorName) {
                case "RED": g2d.setColor(Color.RED); break;
                case "BLUE": g2d.setColor(Color.BLUE); break;
                case "GREEN": g2d.setColor(Color.GREEN); break;
                case "YELLOW": g2d.setColor(Color.YELLOW); break;
                case "WHITE": g2d.setColor(Color.WHITE); break;
                default: g2d.setColor(Color.BLACK); break;
            }
            
            // 💡 CAP_ROUND 속성을 부여하여 펜 끝과 꺾이는 선을 동그랗고 부드럽게 개선
            g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); 
            
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
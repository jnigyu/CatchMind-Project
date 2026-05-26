package com.project.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints; 
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage; 
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel; 
import javax.swing.JOptionPane; 
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider; 
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.project.client.ClientMain;

public class UiMain {
    private JFrame frame; 
    private JTextArea chatArea;        
    private JPanel canvasPanel;        
    private ClientMain client;         
    private JButton readyStartBtn;     
    private JLabel timerLabel;         
    private JTextField inputField;     
    private JLabel strokeValueLabel;   
    
    private int lastX, lastY;          
    private String currentColor = "BLACK"; 
    private int currentThickness = 3;      
    private BufferedImage canvasImage;     
    private boolean isEraser = false;      

    private boolean isDrawer = false;      
    private boolean amIReady = false;      
    private int savedReady = 0;            
    private int savedTotal = 0;            

    // 💡 제시어 입력 단계인지를 판별하는 제어 플래그 변수 추가
    private boolean isWordSettingPhase = false; 

    private final Color neonBlue = new Color(0, 180, 216);      
    private final Color darkBg = new Color(30, 31, 34);         
    private final Color toolbarBg = new Color(43, 45, 49);      
    private final Color cyberGreen = new Color(0, 245, 150);    

    private final int VIRTUAL_WIDTH = 800;
    private final int VIRTUAL_HEIGHT = 500;

    public UiMain() {
        client = new ClientMain(this); 
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        Font gameFont = new Font("휴먼둥근헤드라인", Font.PLAIN, 14);
        Font gameBoldFont = new Font("휴먼둥근헤드라인", Font.PLAIN, 15);
        Font titleFont = new Font("휴먼둥근헤드라인", Font.PLAIN, 16);
        
        UIManager.put("Button.font", gameBoldFont); 
        UIManager.put("Label.font", gameBoldFont); 
        UIManager.put("TextField.font", gameFont);
        UIManager.put("TextArea.font", gameFont);

        frame = new JFrame("실시간 캐치마인드 - 게임 룸");
        frame.setSize(1150, 720); 
        frame.setMinimumSize(new Dimension(950, 580)); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(12, 12));

        canvasImage = new BufferedImage(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvasImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        g2d.dispose();

        canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); 
                g.drawImage(canvasImage, 0, 0, getWidth(), getHeight(), null); 
            }
        };
        canvasPanel.setLayout(new BorderLayout()); 
        canvasPanel.setBorder(BorderFactory.createLineBorder(neonBlue, 3)); 
        
        JPanel topTimerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topTimerPanel.setOpaque(false); 
        topTimerPanel.setBorder(new EmptyBorder(10, 10, 0, 15)); 
        timerLabel = new JLabel("대기 중.. ");
        timerLabel.setFont(titleFont);
        timerLabel.setForeground(cyberGreen); 
        topTimerPanel.add(timerLabel);
        canvasPanel.add(topTimerPanel, BorderLayout.NORTH); 
        
        canvasPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isDrawer) return; 
                lastX = (int) (((double) e.getX() / canvasPanel.getWidth()) * VIRTUAL_WIDTH);
                lastY = (int) (((double) e.getY() / canvasPanel.getHeight()) * VIRTUAL_HEIGHT);
            }
        });

        canvasPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isDrawer) return; 

                int currentX = (int) (((double) e.getX() / canvasPanel.getWidth()) * VIRTUAL_WIDTH);
                int currentY = (int) (((double) e.getY() / canvasPanel.getHeight()) * VIRTUAL_HEIGHT);

                String colorToDraw = isEraser ? "WHITE" : currentColor;
                
                drawRemoteLine(lastX, lastY, currentX, currentY, colorToDraw, currentThickness);
                client.sendMessage("[DRAW]," + lastX + "," + lastY + "," + currentX + "," + currentY + "," + colorToDraw + "," + currentThickness);

                lastX = currentX;
                lastY = currentY;
            }
        });
        
        JPanel drawToolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        drawToolPanel.setBackground(toolbarBg); 
        drawToolPanel.setBorder(new EmptyBorder(6, 12, 6, 12));
        
        JLabel palLabel = new JLabel("컬러 팔레트: ");
        palLabel.setForeground(Color.LIGHT_GRAY);
        drawToolPanel.add(palLabel);
        
        drawToolPanel.add(createColorCircleBtn(Color.RED, "RED", "빨강"));
        drawToolPanel.add(createColorCircleBtn(new Color(255, 127, 0), "ORANGE", "주황"));
        drawToolPanel.add(createColorCircleBtn(Color.YELLOW, "YELLOW", "노랑"));
        drawToolPanel.add(createColorCircleBtn(Color.GREEN, "GREEN", "초록"));
        drawToolPanel.add(createColorCircleBtn(Color.BLUE, "BLUE", "파랑"));
        drawToolPanel.add(createColorCircleBtn(new Color(139, 0, 255), "PURPLE", "보라"));
        drawToolPanel.add(createColorCircleBtn(new Color(255, 182, 193), "PINK", "분홍"));
        drawToolPanel.add(createColorCircleBtn(new Color(139, 69, 19), "BROWN", "갈색"));
        drawToolPanel.add(createColorCircleBtn(Color.GRAY, "GRAY", "회색"));
        drawToolPanel.add(createColorCircleBtn(Color.BLACK, "BLACK", "검정"));
        
        drawToolPanel.add(new JLabel(" | "));
        
        JButton eraserBtn = new JButton("지우개 패드");
        JButton clearBtn = new JButton("화면 전체 리셋");
        
        eraserBtn.setBackground(darkBg);
        clearBtn.setBackground(darkBg);
        eraserBtn.setForeground(Color.WHITE);
        clearBtn.setForeground(Color.WHITE);
        
        eraserBtn.addActionListener(e -> {
            isEraser = true;
            strokeValueLabel.setText("지우개 두께: " + currentThickness + " ");
        }); 
        clearBtn.addActionListener(e -> { 
            if(isDrawer) client.sendMessage("[CLEAR]"); 
        });
        
        strokeValueLabel = new JLabel("붓 두께: 3 ");
        strokeValueLabel.setForeground(Color.LIGHT_GRAY);
        JSlider thicknessSlider = new JSlider(1, 30, currentThickness);
        thicknessSlider.setPreferredSize(new Dimension(100, 25)); 
        thicknessSlider.setBackground(toolbarBg);
        thicknessSlider.addChangeListener(e -> {
            currentThickness = thicknessSlider.getValue();
            strokeValueLabel.setText((isEraser ? "지우개 두께: " : "붓 두께: ") + currentThickness + " ");
        });

        drawToolPanel.add(eraserBtn);
        drawToolPanel.add(clearBtn);
        drawToolPanel.add(new JLabel(" | "));
        drawToolPanel.add(strokeValueLabel);
        drawToolPanel.add(thicknessSlider);

        canvasPanel.add(drawToolPanel, BorderLayout.SOUTH);
        frame.add(canvasPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(new EmptyBorder(0, 0, 10, 15)); 
        rightPanel.setPreferredSize(new Dimension(340, 0)); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; 
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(darkBg); 
        chatArea.setForeground(Color.WHITE); 
        chatArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(toolbarBg, 2));
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.85; 
        rightPanel.add(scrollPane, gbc);

        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(4, 3, 4, 3); 
        
        JTextField ipField = new JTextField("127.0.0.1"); 
        inputField = new JTextField(); 
        JButton actionBtn = new JButton("접속");
        readyStartBtn = new JButton("준비 (0/0)"); 
        readyStartBtn.setEnabled(false); 
        
        ipField.setBackground(darkBg); ipField.setForeground(Color.WHITE);
        inputField.setBackground(darkBg); inputField.setForeground(Color.WHITE);
        actionBtn.setBackground(darkBg); actionBtn.setForeground(Color.WHITE);
        readyStartBtn.setBackground(darkBg); readyStartBtn.setForeground(Color.WHITE);

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

        ActionListener connectionAction = e -> {
            String text = inputField.getText().trim();
            String ip = ipField.getText().trim(); 

            if (text.isEmpty() || ip.isEmpty()) return; 

            if (actionBtn.getText().equals("접속")) {
                client.connect(ip, 8000, text);
                actionBtn.setText("전송");
                actionBtn.setBackground(neonBlue); 
                inputField.setText("");
                ipField.setEnabled(false); 
                readyStartBtn.setEnabled(true); 
            } else {
                // 💡 만약 현재 제시어를 직접 정하는 단계라면?
                if (isWordSettingPhase) {
                    client.sendMessage("[SET_WORD]," + text); // 서버로 정답 세팅 신호 전송
                    isWordSettingPhase = false; // 단어 설정 단계 종료
                    
                    inputField.setEnabled(false); // 💡 입력창 즉시 잠금 처리
                    inputField.setText("출제자 채팅금지");
                    frame.setTitle("실시간 캐치마인드 - ★내가 출제자★ 제시어: " + text);
                    
                    // 내 채팅창에만 비밀스럽게 입력 완료 상태를 출력
                    appendChat("🔒 제시어가 [" + text + "](으)로 설정되었습니다. 다른 사람에게 말하지 마세요!");
                } else {
                    // 평소에는 일반 실시간 채팅으로 전송
                    client.sendMessage("[CHAT]," + text);
                    inputField.setText("");
                }
            }
        };
        inputField.addActionListener(connectionAction);
        actionBtn.addActionListener(connectionAction);

        gbcInput.gridx = 0; gbcInput.gridy = 0; gbcInput.weightx = 0.3;
        bottomPanel.add(new JLabel("IP:"), gbcInput);
        gbcInput.gridx = 1; gbcInput.gridy = 0; gbcInput.weightx = 0.7; gbcInput.gridwidth = 2;
        bottomPanel.add(ipField, gbcInput);
        
        gbcInput.gridx = 0; gbcInput.gridy = 1; gbcInput.weightx = 0.3; gbcInput.gridwidth = 1;
        bottomPanel.add(new JLabel("닉네임/채팅:"), gbcInput);
        gbcInput.gridx = 1; gbcInput.gridy = 1; gbcInput.weightx = 0.7;
        bottomPanel.add(inputField, gbcInput);
        
        gbcInput.gridx = 0; gbcInput.gridy = 2; gbcInput.weightx = 0.5;
        bottomPanel.add(actionBtn, gbcInput);
        gbcInput.gridx = 1; gbcInput.gridy = 2; gbcInput.weightx = 0.5;
        bottomPanel.add(readyStartBtn, gbcInput);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 0.15; 
        rightPanel.add(bottomPanel, gbc);

        frame.add(rightPanel, BorderLayout.EAST); 
        frame.setVisible(true);
    }

    private JButton createColorCircleBtn(Color color, String name, String labelName) {
        JButton btn = new JButton();
        btn.setBackground(color);
        btn.setPreferredSize(new Dimension(24, 24)); 
        btn.setToolTipText(labelName); 
        btn.addActionListener(e -> {
            currentColor = name;
            isEraser = false; 
            strokeValueLabel.setText("붓 두께: " + currentThickness + " ");
        });
        return btn;
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
        isWordSettingPhase = false; // 플래그 초기화
        readyStartBtn.setText("준비 (0/0)");
        timerLabel.setText("대기 중.. ");
        frame.setTitle("실시간 캐치마인드 - 대기실");
        
        inputField.setEnabled(true);
        inputField.setText("");
    }

    public void updateTimerLabel(String seconds) {
        timerLabel.setText("남은 시간: " + seconds + "초 ");
    }

    public void showFinalScoreDialog(String winner, String rankings) {
        timerLabel.setText("게임 종료 ");
        JOptionPane.showMessageDialog(frame, 
            "🏆 최종 우승자: [" + winner + "] 님!! 🏆\n\n📊 [최종 등수 나열]\n" + rankings, 
            "게임 결과 발표", 
            JOptionPane.INFORMATION_MESSAGE);
        resetReadyState();
    }

    public void setGameRole(boolean drawerMode, String titleMessage) {
        this.isDrawer = drawerMode;
        frame.setTitle("실시간 캐치마인드 - " + titleMessage);
        
        if (drawerMode) {
            // 💡 출제자라면 처음에는 입력창을 열어주고 단어 입력 페이즈 플래그를 켭니다.
            isWordSettingPhase = true; 
            inputField.setEnabled(true);
            inputField.setText("");
            
            // 💡 오직 나에게만 비밀 메시지를 출력합니다.
            appendChat("💬 [SYSTEM] 정답을 정해주세요! 입력창에 단어를 적고 엔터나 전송을 누르세요. (다른 사람에게는 보이지 않습니다.)");
        } else {
            isWordSettingPhase = false;
            inputField.setEnabled(true);
            inputField.setText("");
        }
        
        clearCanvas(); 
    }

    public void drawRemoteLine(int x1, int y1, int x2, int y2, String colorName, int thickness) {
        Graphics2D g2d = canvasImage.createGraphics();
        if (g2d != null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            switch(colorName) {
                case "RED": g2d.setColor(Color.RED); break;
                case "ORANGE": g2d.setColor(new Color(255, 127, 0)); break; 
                case "YELLOW": g2d.setColor(Color.YELLOW); break;
                case "GREEN": g2d.setColor(Color.GREEN); break;
                case "BLUE": g2d.setColor(Color.BLUE); break;
                case "PURPLE": g2d.setColor(new Color(139, 0, 255)); break; 
                case "PINK": g2d.setColor(new Color(255, 182, 193)); break; 
                case "BROWN": g2d.setColor(new Color(139, 69, 19)); break;   
                case "GRAY": g2d.setColor(Color.GRAY); break;               
                case "WHITE": g2d.setColor(Color.WHITE); break;
                default: g2d.setColor(Color.BLACK); break;
            }
            
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
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            UIManager.put("Button.arc", 25);      
            UIManager.put("Component.arc", 20);   
            UIManager.put("Slider.thumbWidth", 20); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        new UiMain(); 
    }
}
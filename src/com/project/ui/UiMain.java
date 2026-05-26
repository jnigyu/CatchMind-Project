package com.project.ui; // 조원들이 지정한 패키지 경로에 맞게 자동 배치됩니다.

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

// 🔌 팀원들이 작성한 클라이언트 네트워크 코드를 정상적으로 수입합니다.
import com.project.client.ClientMain;

public class UiMain {
    // -------------------------------------------------------------
    // [멤버 변수] UI 컴포넌트 및 네트워크 상태 제어 관리
    // -------------------------------------------------------------
    private JFrame frame; 
    private JTextArea chatArea;        // 실시간 채팅 리스트 창
    private JPanel canvasPanel;        // 실제 그림이 그려지는 도화지 패널
    private ClientMain client;         // 🔌 조원들이 만든 소켓 통신 코어 엔진
    private JButton readyStartBtn;     // 🔌 대기방 방장 시작 / 유저 준비용 연동 버튼
    private JLabel timerLabel;         // 🔌 상단 실시간 타이머 라벨
    private JTextField inputField;     // 유저의 채팅 및 정답 입력창
    private JLabel strokeValueLabel;   // 현재 펜/지우개 두께 수치 표시 라벨
    
    private int lastX, lastY;          // 선 그리기를 위한 직전 마우스 좌표 캐싱
    private String currentColor = "BLACK"; // 펜 기본 색상
    private int currentThickness = 3;      // 펜 기본 두께
    private BufferedImage canvasImage;     // 해상도 왜곡 방지 가상 드로잉 버퍼 이미지
    private boolean isEraser = false;      // 지우개 모드 플래그 

    private boolean isDrawer = false;      // 🔌 내가 현재 출제자인지 맞추는 사람인지 상태값
    private boolean amIReady = false;      // 🔌 내 준비 상태 체크값
    private int savedReady = 0;            // 🔌 현재방 준비 인원수 캐싱
    private int savedTotal = 0;            // 🔌 현재방 전체 인원수 캐싱

    // [디자인 파트] 사이버 펑크 스타일 및 게이머 다크 모드 맞춤형 컬러 시트
    private final Color neonBlue = new Color(0, 180, 216);      // 테두리 네온 블루
    private final Color darkBg = new Color(30, 31, 34);         // 딥 차콜 배경색
    private final Color toolbarBg = new Color(43, 45, 49);      // 조작 툴바 배경색
    private final Color cyberGreen = new Color(0, 245, 150);    // 시스템 형광 연두색

    // [화면 비율 최적화 해상도] 창 크기를 조절해도 렌더링 좌표를 유지할 가상 해상도 비율
    private final int VIRTUAL_WIDTH = 800;
    private final int VIRTUAL_HEIGHT = 500;

    /**
     * UI 생성자 - 소켓을 다루는 클라이언트와 메인을 서로 연결 고리(바인딩) 해줍니다.
     */
    public UiMain() {
        client = new ClientMain(this); // 🔌 통신 엔진에 현재 내 UI창 주소를 넘겨 동기화
        createAndShowGUI();
    }

    /**
     * 전체 GUI 레이아웃 화면 설계 및 컴포넌트 빌드 메소드
     */
    private void createAndShowGUI() {
        // [폰트 가공] 게이밍 인터페이스에 가장 어울리는 볼드체 서체 일괄 등록
        Font gameFont = new Font("휴먼둥근헤드라인", Font.PLAIN, 14);
        Font gameBoldFont = new Font("휴먼둥근헤드라인", Font.PLAIN, 15);
        Font titleFont = new Font("휴먼둥근헤드라인", Font.PLAIN, 16);
        
        UIManager.put("Button.font", gameBoldFont); 
        UIManager.put("Label.font", gameBoldFont); 
        UIManager.put("TextField.font", gameFont);
        UIManager.put("TextArea.font", gameFont);

        // [메인 프레임 창 설정] 노트북 화면 잘림 오류 방지를 위한 최소 크기 제한 락
        frame = new JFrame("실시간 캐치마인드 - 게임 룸");
        frame.setSize(1150, 720); 
        frame.setMinimumSize(new Dimension(950, 580)); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(12, 12));

        // [그림판 버퍼 이미지 엔진 초기화]
        canvasImage = new BufferedImage(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvasImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        g2d.dispose();

        // [중앙 메인 도화지 패널] 800x500 가상 버퍼 크기를 유동적으로 스트레칭 렌더링
        canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); 
                g.drawImage(canvasImage, 0, 0, getWidth(), getHeight(), null); 
            }
        };
        canvasPanel.setLayout(new BorderLayout()); 
        canvasPanel.setBorder(BorderFactory.createLineBorder(neonBlue, 3)); 
        
        // [상단 알림판 파트] 실시간 타이머 및 라운드 시간 정보 표시
        JPanel topTimerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topTimerPanel.setOpaque(false); 
        topTimerPanel.setBorder(new EmptyBorder(10, 10, 0, 15)); 
        timerLabel = new JLabel("대기 중.. ");
        timerLabel.setFont(titleFont);
        timerLabel.setForeground(cyberGreen); 
        topTimerPanel.add(timerLabel);
        canvasPanel.add(topTimerPanel, BorderLayout.NORTH); 
        
        // [마우스 드로잉 이벤트 리스너] 내가 출제자(isDrawer)일 때만 작동하도록 연동 제어
        canvasPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isDrawer) return; // 🔌 출제자가 아니면 그림 그리기 거부
                lastX = (int) (((double) e.getX() / canvasPanel.getWidth()) * VIRTUAL_WIDTH);
                lastY = (int) (((double) e.getY() / canvasPanel.getHeight()) * VIRTUAL_HEIGHT);
            }
        });

        canvasPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isDrawer) return; // 🔌 출제자가 아니면 그림 그리기 거부

                int currentX = (int) (((double) e.getX() / canvasPanel.getWidth()) * VIRTUAL_WIDTH);
                int currentY = (int) (((double) e.getY() / canvasPanel.getHeight()) * VIRTUAL_HEIGHT);

                String colorToDraw = isEraser ? "WHITE" : currentColor;
                
                // 내 화면에 먼저 선을 긋고, 다른 사람들에게도 그리라고 서버로 좌표 전송
                drawRemoteLine(lastX, lastY, currentX, currentY, colorToDraw, currentThickness);
                client.sendMessage("[DRAW]," + lastX + "," + lastY + "," + currentX + "," + currentY + "," + colorToDraw + "," + currentThickness);

                lastX = currentX;
                lastY = currentY;
            }
        });
        
        // [하단 도화지 기능 툴바 영역] 조약돌 스타일 팔레트 및 단추 집약
        JPanel drawToolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        drawToolPanel.setBackground(toolbarBg); 
        drawToolPanel.setBorder(new EmptyBorder(6, 12, 6, 12));
        
        JLabel palLabel = new JLabel("컬러 팔레트: ");
        palLabel.setForeground(Color.LIGHT_GRAY);
        drawToolPanel.add(palLabel);
        
        // 10색 비주얼 원형 버튼 매핑
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
            if(isDrawer) client.sendMessage("[CLEAR]"); // 🔌 내가 출제자일 때만 전체 화면 지우기 명령 패킷 발송
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

        // -------------------------------------------------------------
        // [우측 메인 패널 영역] 격자 분할 방식(GridBagLayout) 레이아웃 구성
        // -------------------------------------------------------------
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(new EmptyBorder(0, 0, 10, 15)); 
        rightPanel.setPreferredSize(new Dimension(340, 0)); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; 
        
        // 1층 : 실시간 스크롤 채팅 및 서버 로그 표시창 수용 (85%)
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(darkBg); 
        chatArea.setForeground(Color.WHITE); 
        chatArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(toolbarBg, 2));
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.85; 
        rightPanel.add(scrollPane, gbc);

        // 2층 : 유저 접속 인터페이스 및 닉네임/채팅 하단 종합 조작부 (15%)
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        GridBagConstraints gbcInput = new GridBagConstraints();
        gbcInput.fill = GridBagConstraints.HORIZONTAL;
        gbcInput.insets = new Insets(4, 3, 4, 3); 
        
        JTextField ipField = new JTextField("127.0.0.1"); 
        inputField = new JTextField(); 
        JButton actionBtn = new JButton("접속");
        readyStartBtn = new JButton("준비 (0/0)"); 
        readyStartBtn.setEnabled(false); // 처음에는 방에 들어가야 하므로 비활성화
        
        ipField.setBackground(darkBg); ipField.setForeground(Color.WHITE);
        inputField.setBackground(darkBg); inputField.setForeground(Color.WHITE);
        actionBtn.setBackground(darkBg); actionBtn.setForeground(Color.WHITE);
        readyStartBtn.setBackground(darkBg); readyStartBtn.setForeground(Color.WHITE);

        // 🔌 [준비/시작 멀티 프로세스 로직 연동]
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
                client.sendMessage("[START]"); // 방장일 땐 게임 스타트 신호 송신
            }
        });

        // 🔌 [접속 및 대화 전송 리스너 액션 연동]
        ActionListener connectionAction = e -> {
            String text = inputField.getText().trim();
            String ip = ipField.getText().trim(); 

            if (text.isEmpty() || ip.isEmpty()) return; 

            if (actionBtn.getText().equals("접속")) {
                // 첫 클릭 시 입력한 텍스트를 "닉네임"으로 인식하여 소켓 서버 연결 시도
                client.connect(ip, 8000, text);
                actionBtn.setText("전송");
                actionBtn.setBackground(neonBlue); 
                inputField.setText("");
                ipField.setEnabled(false); 
                readyStartBtn.setEnabled(true); 
            } else {
                // 접속 후에는 입력한 텍스트를 "실시간 채팅 패킷"으로 간주하여 발송
                client.sendMessage("[CHAT]," + text);
                inputField.setText("");
            }
        };
        inputField.addActionListener(connectionAction);
        actionBtn.addActionListener(connectionAction);

        gbcInput.gridx = 0; gbcInput.gridy = 0; gbcInput.weightx = 0.3;
        bottomPanel.add(new JLabel("IP:"), gbcInput);
        gbcInput.gridx = 1; gbcInput.gridy = 0; gbcInput.weightx = 0.7; gbcInput.gridwidth = 2;
        bottomPanel.add(ipField, gbcInput);
        
        // ✨ [다해 님 아이디어 기획 반영] 기존 "입력:" 문구를 훨씬 직관적인 "닉네임/채팅:"으로 최종 개정 완료!
        gbcInput.gridwidth = 1; 
        gbcInput.gridx = 0; gbcInput.gridy = 1; gbcInput.weightx = 0.3;
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

    /**
     * 비주얼 컬러 매핑용 원형 미니 색상 단추 제조 함수
     */
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

    // =========================================================================
    // 🔌 조원 서버 통신 모듈 연동부 : 실전 원격 공용 인터페이스 API 과업
    // =========================================================================

    /**
     * 🔌 서버 수신 패킷에 맞춰 유저들의 실시간 준비 인원수 텍스트를 라벨 렌더링하는 API
     */
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

    /**
     * 🔌 방장 유저 권한 부여 시 단추 문구를 [시작] 모드로 자동 변환하는 API
     */
    public void changeToStartButton() { readyStartBtn.setText("시작"); }
    
    /**
     * 🔌 게임 종료 후 대기방 스펙으로 전체 UI 컴포넌트 데이터값 원상복귀 리셋 API
     */
    public void resetReadyState() {
        amIReady = false;
        isDrawer = false;
        readyStartBtn.setText("준비 (0/0)");
        timerLabel.setText("대기 중.. ");
        frame.setTitle("실시간 캐치마인드 - 대기실");
        
        inputField.setEnabled(true);
        inputField.setText("");
    }

    /**
     * 🔌 서버 타이머 초 단위를 원격으로 넘겨받아 상단 형광 라벨에 세팅해주는 API
     */
    public void updateTimerLabel(String seconds) {
        timerLabel.setText("남은 시간: " + seconds + "초 ");
    }

    /**
     * 🔌 라운드 스코어 정산 발표 메시지 모달 다이얼로그를 전면에 출력하는 API
     */
    public void showFinalScoreDialog(String winner, String rankings) {
        timerLabel.setText("게임 종료 ");
        JOptionPane.showMessageDialog(frame, 
            "🏆 최종 우승자: [" + winner + "] 님!! 🏆\n\n📊 [최종 등수 나열]\n" + rankings, 
            "게임 결과 발표", 
            JOptionPane.INFORMATION_MESSAGE);
        resetReadyState();
    }

    /**
     * 🔌 출제자 지정 여부에 따라 그림판 권한을 제어하고 입력창 타이핑을 막아 어뷰징을 필터링하는 API
     */
    public void setGameRole(boolean drawerMode, String titleMessage) {
        this.isDrawer = drawerMode;
        frame.setTitle("실시간 캐치마인드 - " + titleMessage);
        
        if (drawerMode) {
            inputField.setEnabled(false);
            inputField.setText("출제자 채팅금지"); // 출제자는 정답을 유출할 수 없게 락 처리
        } else {
            inputField.setEnabled(true);
            inputField.setText("");
        }
        
        clearCanvas(); 
    }

    /**
     * 🔌 네트워크로 전송받은 타 유저의 그림 그리기 좌표 벡터를 필터 연산하여 도화지에 뿌려주는 API
     */
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

    /**
     * 도화지 패널 흰색 강제 도포 초기화 API
     */
    public void clearCanvas() {
        Graphics2D g2d = canvasImage.createGraphics();
        g2d.setColor(Color.WHITE); 
        g2d.fillRect(0, 0, canvasImage.getWidth(), canvasImage.getHeight()); 
        g2d.dispose();
        canvasPanel.repaint(); 
    }

    /**
     * 외부 수신 챗 패킷 개행 적재 API
     */
    public void appendChat(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength()); 
    }

    public static void main(String[] args) {
        try {
            // ✨ 프로젝트 lib 폴더 내 저장된 FlatLaf 다크 스킨 모던 테마를 강제로 이식 구동합니다!
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            UIManager.put("Button.arc", 25);      
            UIManager.put("Component.arc", 20);   
            UIManager.put("Slider.thumbWidth", 20); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        new UiMain(); // 본 클래스 이름 스펙 매핑 호출
    }
}
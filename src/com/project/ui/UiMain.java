package com.project.ui;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.project.client.ClientMain;

public class UiMain {
    private JTextArea chatArea; // 글자가 쌓이는 큰 채팅창
    private ClientMain client;  // 네트워크 담당자

    public UiMain() {
        // UI가 만들어질 때 네트워크 담당자(ClientMain)도 같이 고용합니다.
        client = new ClientMain(this); 
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("캐치마인드 채팅 테스트");
        frame.setSize(400, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 1. 채팅이 쌓이는 영역 설정
        chatArea = new JTextArea();
        chatArea.setEditable(false); // 채팅창은 읽기 전용
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // 2. 하단 입력 영역 (닉네임/채팅 입력칸 + 버튼)
        JPanel bottomPanel = new JPanel();
        JTextField inputField = new JTextField(20);
        JButton actionBtn = new JButton("접속 (닉네임입력)");

        bottomPanel.add(inputField);
        bottomPanel.add(actionBtn);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // 3. 버튼 클릭 이벤트 (처음엔 접속, 그 다음부턴 채팅)
        actionBtn.addActionListener(e -> {
            String text = inputField.getText();
            if (text.isEmpty()) return;

            if (actionBtn.getText().startsWith("접속")) {
                // 버튼이 '접속' 상태일 때는 서버로 연결!
                client.connect("127.0.0.1", 8000, text);
                actionBtn.setText("전송"); // 버튼 이름을 전송으로 바꿈
                inputField.setText("");
            } else {
                // 버튼이 '전송' 상태일 때는 채팅 메시지 발사!
                client.sendMessage("[CHAT]," + text);
                inputField.setText("");
            }
        });

        frame.setVisible(true);
    }

    // 네트워크 담당자(ClientMain)가 채팅을 받았을 때 화면에 띄워주는 기능
    public void appendChat(String message) {
        chatArea.append(message + "\n");
        // 스크롤을 항상 맨 아래로 유지
        chatArea.setCaretPosition(chatArea.getDocument().getLength()); 
    }

    public static void main(String[] args) {
        new UiMain();
    }
}

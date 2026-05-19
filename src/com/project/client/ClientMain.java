package com.project.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.project.ui.UiMain; // UI 창에 글자를 띄우기 위해 불러옴

public class ClientMain {
    private Socket socket;
    private PrintWriter out;
    private UiMain ui; // 화면 조종 리모컨

    // 클라이언트가 생성될 때 UI 리모컨을 건네받습니다.
    public ClientMain(UiMain ui) {
        this.ui = ui;
    }

    // 1. 서버 접속 및 수신 귀(Thread) 열기
    public void connect(String ip, int port, String nickname) {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);

            // 접속하자마자 약속된 규약대로 내 닉네임을 서버로 발사!
            out.println("[JOIN]," + nickname);

            // 💡 핵심: 서버가 쏘는 방송을 계속 엿듣는 전담 직원(Thread) 고용
            Thread receiverThread = new Thread(() -> listenToServer());
            receiverThread.start();

        } catch (Exception e) {
            ui.appendChat("❌ 서버 접속에 실패했습니다.");
        }
    }

    // 2. 서버로 메시지 쏘기 (UI 팀이 버튼 누를 때 사용)
    public void sendMessage(String protocolMessage) {
        if (out != null) {
            out.println(protocolMessage);
        }
    }

    // 3. 서버의 방송을 엿듣고 화면에 띄우기 (무한 루프)
    private void listenToServer() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            
            while ((line = in.readLine()) != null) {
                // 서버가 보낸 메시지를 분석합니다. (예: [SYSTEM],홍길동님이 접속했습니다!)
                String[] parts = line.split(",");
                String command = parts[0];

                if (command.equals("[SYSTEM]") || command.equals("[CHAT]")) {
                    // 채팅창 화면에 글자를 추가하라고 UI에게 명령합니다.
                    ui.appendChat(parts[1]); 
                } 
                // 나중에 여기에 [DRAW]를 받았을 때 캔버스에 선을 긋는 로직이 추가될 것입니다!
            }
        } catch (Exception e) {
            ui.appendChat("❌ 서버와 연결이 끊어졌습니다.");
        }
    }
}

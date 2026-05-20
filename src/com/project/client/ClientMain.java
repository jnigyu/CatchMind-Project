package com.project.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.project.ui.UiMain; 

public class ClientMain {
    private Socket socket;
    private PrintWriter out;
    private UiMain ui; 

    public ClientMain(UiMain ui) {
        this.ui = ui;
    }

    public void connect(String ip, int port, String nickname) {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("[JOIN]," + nickname);

            Thread receiverThread = new Thread(() -> listenToServer());
            receiverThread.start();

        } catch (Exception e) {
            ui.appendChat("❌ 서버 접속에 실패했습니다.");
        }
    }

    public void sendMessage(String protocolMessage) {
        if (out != null) {
            out.println(protocolMessage);
        }
    }

    private void listenToServer() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            
            while ((line = in.readLine()) != null) {
                String[] parts = line.split(",");
                String command = parts[0];

                if (command.equals("[SYSTEM]") || command.equals("[CHAT]")) {
                    ui.appendChat(parts[1]); 
                    
                } else if (command.equals("[DRAW]")) {
                    int x1 = Integer.parseInt(parts[1]);
                    int y1 = Integer.parseInt(parts[2]);
                    int x2 = Integer.parseInt(parts[3]);
                    int y2 = Integer.parseInt(parts[4]);
                    String color = parts[5];
                    ui.drawRemoteLine(x1, y1, x2, y2, color);
                    
                } else if (command.equals("[CLEAR]")) {
                    ui.clearCanvas();
                    
                } else if (command.equals("[ALL_READY]")) {
                    ui.changeToStartButton(); // 💡 모두 준비 완료 시 시작 버튼으로 변경 명령
                    
                } else if (command.equals("[NOT_READY]")) {
                    ui.changeToReadyButton(); // 💡 한 명이라도 해제 시 다시 준비 버튼으로 변경 명령
                    
                } else if (command.equals("[RESET_READY]")) {
                    ui.resetReadyState();     // 💡 게임 종료 후 버튼 리셋 명령
                    
                } else if (command.equals("[GAME_START]")) {
                    // 💡 역할 배정 처리
                    String role = parts[1];
                    if (role.equals("DRAWER")) {
                        ui.setGameRole(true, "★내가 출제자★ 제시어: " + parts[2]);
                    } else {
                        ui.setGameRole(false, "출제자: " + parts[2] + "님 (정답을 맞추세요!)");
                    }
                }
            }
        } catch (Exception e) {
            ui.appendChat("❌ 서버와 연결이 끊어졌습니다.");
        }
    }
}
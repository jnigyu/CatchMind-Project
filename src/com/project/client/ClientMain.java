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
                    int thickness = Integer.parseInt(parts[6]); 
                    ui.drawRemoteLine(x1, y1, x2, y2, color, thickness);
                    
                } else if (command.equals("[CLEAR]")) {
                    ui.clearCanvas();
                    
                } else if (command.equals("[READY_COUNT]")) {
                    ui.updateReadyButtonText(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                    
                } else if (command.equals("[ALL_READY]")) {
                    ui.changeToStartButton(); 
                    
                } else if (command.equals("[NOT_READY]")) {
                    
                } else if (command.equals("[RESET_READY]")) {
                    ui.resetReadyState();     
                    
                } else if (command.equals("[TIMER]")) {
                    ui.updateTimerLabel(parts[1]);
                    
                } else if (command.equals("[GAME_START]")) {
                    String role = parts[1];
                    if (role.equals("DRAWER")) {
                        // 💡 출제자로 뽑혔을 때: 일단 결정 중이라고 띄우고, 직접 단어를 묻는 팝업을 켬!
                        ui.setGameRole(true, "★내가 출제자★ (제시어 결정 중...)");
                        ui.promptForWord(); 
                    } else {
                        ui.setGameRole(false, "출제자: " + parts[2] + "님 (제시어 결정 중...)");
                    }
                } else if (command.equals("[GAME_OVER]")) {
                    String winner = parts[1];
                    String ranks = parts[2];
                    ui.showFinalScoreDialog(winner, ranks);
                }
            }
        } catch (Exception e) {
            ui.appendChat("❌ 서버와 연결이 끊어졌습니다.");
        }
    }
}
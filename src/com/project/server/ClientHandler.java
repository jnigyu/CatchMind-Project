package com.project.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;       
    private ServerMain server;   
    private PrintWriter out;     
    private BufferedReader in;   
    private String nickname;     
    private boolean isReady = false; // 💡 개별 유저의 준비 상태

    public ClientHandler(Socket socket, ServerMain server) {
        this.socket = socket;
        this.server = server;
    }

    public boolean isReady() { return isReady; }
    public void setReady(boolean ready) { this.isReady = ready; }
    public String getNickname() { return nickname; }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] parts = inputLine.split(","); 
                String command = parts[0]; 

                switch (command) {
                    case "[JOIN]": 
                        this.nickname = parts[1]; 
                        server.broadcast("[SYSTEM]," + nickname + "님이 접속했습니다!");
                        server.checkAllReady(); 
                        break;
                        
                    case "[READY]": // 💡 준비 토글 신호 처리
                        this.isReady = Boolean.parseBoolean(parts[1]);
                        server.broadcast("[SYSTEM]," + nickname + "님이 " + (isReady ? "준비 완료!" : "준비를 취소했습니다."));
                        server.checkAllReady(); 
                        break;

                    case "[START]": // 💡 시작 신호 처리
                        server.broadcast("[SYSTEM],게임을 시작합니다!");
                        server.startGame();
                        break;
                        
                    case "[CHAT]": 
                        String chatMsg = parts[1]; 
                        // 게임 진행 중에만 정답 판별 작동
                        if (server.isGameStarted && chatMsg.trim().equals(server.currentAnswer)) {
                            server.broadcast("[SYSTEM],🎉 대박! [" + nickname + "] 님이 정답을 맞췄습니다! (정답: " + server.currentAnswer + ")");
                            server.isGameStarted = false; 
                            server.resetReadyStatus(); // 정답 맞추면 모두 준비 해제 및 대기 상태로 변경
                            server.broadcast("[CLEAR]"); 
                        } else {
                            server.broadcast("[CHAT]," + nickname + " : " + chatMsg);
                        }
                        break;
                        
                    case "[DRAW]": 
                        String drawData = inputLine.substring(7); 
                        server.broadcastExcept(this, "[DRAW]," + drawData);
                        break;
                        
                    case "[CLEAR]":
                        server.broadcast("[CLEAR]");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println((nickname != null ? nickname : "알 수 없는 유저") + " 연결 끊김.");
        } finally {
            if (nickname != null) {
                server.broadcast("[SYSTEM]," + nickname + "님이 퇴장했습니다!");
            }
            server.removeClient(this);
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message); 
        }
    }
}
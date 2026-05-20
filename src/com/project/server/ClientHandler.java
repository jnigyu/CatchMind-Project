package com.project.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;       
    private ServerMain server;   
    private PrintWriter out;     
    private BufferedReader in;   
    private String nickname;     
    private boolean isReady = false; 
    private int score = 0; 
    
    // 💡 출제자 여부를 기억하는 변수
    private boolean isDrawer = false; 

    public ClientHandler(Socket socket, ServerMain server) {
        this.socket = socket;
        this.server = server;
    }

    public boolean isReady() { return isReady; }
    public void setReady(boolean ready) { this.isReady = ready; }
    public String getNickname() { return nickname; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    
    // 💡 출제자 권한 세팅 메서드
    public void setDrawer(boolean drawer) { this.isDrawer = drawer; }

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
                        
                    case "[READY]": 
                        this.isReady = Boolean.parseBoolean(parts[1]);
                        server.broadcast("[SYSTEM]," + nickname + "님이 " + (isReady ? "준비 완료!" : "준비를 취소했습니다."));
                        server.checkAllReady(); 
                        break;

                    case "[START]": 
                        server.broadcast("[SYSTEM],게임을 시작합니다!");
                        server.startGame();
                        break;
                        
                    case "[CHAT]": 
                        // 💡 보안 로직: 만약 이 채팅을 보낸 사람이 출제자라면 무시하고 경고만 줌
                        if (this.isDrawer) {
                            sendMessage("[SYSTEM],🚫 출제자는 그림으로만 설명해야 합니다! (채팅 금지)");
                            break; 
                        }
                        
                        String chatMsg = parts[1]; 
                        if (server.isGameStarted && chatMsg.trim().equals(server.currentAnswer)) {
                            server.stopTimer(); 
                            this.score++; 
                            
                            server.broadcast("[SYSTEM],🎉 정답! [" + nickname + "] 님이 맞췄습니다. (정답: " + server.currentAnswer + " | 점수: " + this.score + "점)");
                            
                            if (this.score >= 3) {
                                String finalRank = server.getRankings(); 
                                server.broadcast("[SYSTEM],🏆🏆 [ " + nickname + " ] 님이 3점을 먼저 획득하여 최종 승리자가 되었습니다! 🏆🏆");
                                server.broadcast("[GAME_OVER]," + nickname + "," + finalRank); 
                                server.resetGameFull(); 
                            } else {
                                server.startGame(); // 다음 문제 및 새로운 출제자/타이머 재가동
                                server.broadcast("[CLEAR]"); 
                            }
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
        } finally {
            System.out.println("❌ [퇴장] 사람이 서버에서 나갔습니다. (닉네임: " + (nickname != null ? nickname : "익명") + ")");
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
package com.project.server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;       
    private ServerMain server;   
    private PrintWriter out;     
    private BufferedReader in;   
    private String nickname;     

    public ClientHandler(Socket socket, ServerMain server) {
        this.socket = socket;
        this.server = server;
    }

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
                        break;
                        
                    case "[CHAT]": 
                        String chatMsg = parts[1]; 
                        // 정답 확인 로직
                        if (chatMsg.trim().equals(server.currentAnswer)) {
                            server.broadcast("[SYSTEM],🎉 대박! [" + nickname + "] 님이 정답을 맞췄습니다! (정답: " + server.currentAnswer + ")");
                            server.setRandomAnswer(); // 정답 맞추면 다음 문제 출제!
                            server.broadcast("[SYSTEM],다음 문제가 출제되었습니다. 그림을 그려주세요!");
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
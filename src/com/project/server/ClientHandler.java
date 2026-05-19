package com.project.server; // 패키지 경로 삽입 완료

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;       
    private ServerMain server;   // GameServer 대신 ServerMain으로 이름 변경
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
                // 약속했던 규약대로 쉼표(,)를 기준으로 쪼갭니다!
                String[] parts = inputLine.split(","); 
                String command = parts[0]; 

                switch (command) {
                    case "[JOIN]": 
                        this.nickname = parts[1]; 
                        server.broadcast("[SYSTEM]," + nickname + "님이 접속했습니다!");
                        break;
                    case "[CHAT]": 
                        String chatMsg = parts[1]; 
                        server.broadcast("[CHAT]," + nickname + " : " + chatMsg);
                        break;
                    case "[DRAW]": 
                        // 예: [DRAW],100,150,102,155,RED -> 1번 인덱스부터 끝까지가 좌표 데이터
                        String drawData = inputLine.substring(7); // "[DRAW]," 뒷부분만 잘라냄
                        server.broadcastExcept(this, "[DRAW]," + drawData);
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

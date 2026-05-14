// com.project.client.ClientMain.java
package com.project.client;

import java.io.IOException;
import java.net.Socket;

public class ClientMain {
    public static void main(String[] args) {
        // 서버의 IP 주소. 일단 같은 컴퓨터에서 테스트할 때는 "localhost" (또는 "127.0.0.1")를 사용
        String serverIp = "127.0.0.1"; 
        int port = 8000; // 서버 팀이 열어둔 포트 번호와 무조건 같아야 함!

        try {
            System.out.println("서버에 접속을 시도합니다...");
            // Socket 객체를 생성하는 순간, 서버의 accept()를 향해 연결을 시도함
            Socket socket = new Socket(serverIp, port);
            
            System.out.println("✅ 서버 접속 성공!");
            
            // 일단 연결 테스트용이므로 접속 후 바로 소켓을 닫음
            socket.close(); 
        } catch (IOException e) {
            System.out.println("❌ 서버 접속 실패: 서버가 켜져 있는지 확인하세요.");
            e.printStackTrace();
        }
    }
}
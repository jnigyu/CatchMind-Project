// com.project.server.ServerMain.java
package com.project.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String[] args) {
        int port = 8000; // 우리가 사용할 약속된 문(Port) 번호
        
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("🚀 캐치마인드 서버가 " + port + "번 포트에서 대기 중입니다...");
            
            // 무한 루프를 돌며 클라이언트의 접속을 기다림
            while (true) {
                // accept(): 클라이언트가 접속할 때까지 여기서 코드가 멈춰서 기다림
                Socket clientSocket = serverSocket.accept(); 
                
                // 접속에 성공하면 클라이언트의 IP 주소를 출력
                System.out.println("💡 클라이언트 접속 성공! IP: " + clientSocket.getInetAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
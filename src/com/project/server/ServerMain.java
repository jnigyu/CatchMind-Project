package com.project.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerMain {
    private static final int PORT = 8000; 
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    
    // 랜덤 단어장 배열 
    private String[] wordList = {"자전거", "사과", "노트북", "강아지", "기말고사", "교수님", "에이플러스", "커피", "와이파이", "키보드"};
    public String currentAnswer; 

    // 랜덤으로 단어를 하나 뽑아주는 메서드
    public void setRandomAnswer() {
        Random rand = new Random();
        currentAnswer = wordList[rand.nextInt(wordList.length)];
        System.out.println("🤫 (서버 관리자용) 이번 문제 정답: " + currentAnswer);
    }

    public void start() {
        setRandomAnswer(); // 서버 켤 때 첫 문제 뽑기

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🚀 캐치마인드 서버가 " + PORT + " 포트에서 가동 중입니다!");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler); 
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void broadcastExcept(ClientHandler sender, String message) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("유저 퇴장. 현재 인원: " + clients.size() + "명");
    }

    public static void main(String[] args) {
        new ServerMain().start();
    }
}
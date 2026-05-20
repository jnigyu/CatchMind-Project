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
    
    private String[] wordList = {"자전거", "사과", "노트북", "강아지", "기말고사", "교수님", "에이플러스", "커피", "와이파이", "키보드"};
    public String currentAnswer; 
    public boolean isGameStarted = false; // 게임이 진행 중인지 여부

    public void setRandomAnswer() {
        Random rand = new Random();
        currentAnswer = wordList[rand.nextInt(wordList.length)];
        System.out.println("🤫 (서버 관리자용) 이번 문제 정답: " + currentAnswer);
    }

    public void start() {
        setRandomAnswer(); 

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

    // 💡 모든 접속자가 준비 버튼을 눌렀는지 검사하는 기능
    public void checkAllReady() {
        if (clients.isEmpty()) return;

        boolean allReady = true;
        for (ClientHandler client : clients) {
            if (!client.isReady()) {
                allReady = false;
                break;
            }
        }

        // 모두 준비되었다면 시작 버튼으로 바꾸라고 신호를 보냄
        if (allReady) {
            broadcast("[ALL_READY]");
        } else {
            broadcast("[NOT_READY]");
        }
    }

    // 💡 게임 시작 로직 (랜덤 출제자 선정 및 권한 부여)
    public void startGame() {
        if (clients.isEmpty()) return;
        isGameStarted = true;
        setRandomAnswer(); // 새 문제 출제

        Random rand = new Random();
        int drawerIndex = rand.nextInt(clients.size()); // 랜덤으로 출제자 인덱스 선택

        for (int i = 0; i < clients.size(); i++) {
            ClientHandler client = clients.get(i);
            if (i == drawerIndex) {
                // 출제자에게 전송 (역할, 제시어, 본인닉네임)
                client.sendMessage("[GAME_START],DRAWER," + currentAnswer + "," + client.getNickname());
            } else {
                // 구경꾼들에게 전송 (역할, 출제자닉네임)
                client.sendMessage("[GAME_START],VIEWER," + clients.get(drawerIndex).getNickname());
            }
        }
    }

    // 💡 게임이 끝나면 모든 유저의 준비 상태를 초기화
    public void resetReadyStatus() {
        for (ClientHandler client : clients) {
            client.setReady(false);
        }
        broadcast("[RESET_READY]");
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
        checkAllReady(); 
    }

    public static void main(String[] args) {
        new ServerMain().start();
    }
}
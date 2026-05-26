package com.project.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerMain {
    private static final int PORT = 8000; 
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    
    public String currentAnswer; 
    public boolean isGameStarted = false; 
    private Thread timerThread; 

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🚀 캐치마인드 서버가 " + PORT + " 포트에서 가동 중입니다!");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("💡 [접속] 새로운 사람이 서버에 접속했습니다. (IP: " + clientSocket.getInetAddress() + ")");
                
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler); 
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkAllReady() {
        if (clients.isEmpty()) return;

        int readyCount = 0;
        int totalCount = clients.size();
        for (ClientHandler client : clients) {
            if (client.isReady()) readyCount++;
        }

        broadcast("[READY_COUNT]," + readyCount + "," + totalCount);

        if (readyCount == totalCount && totalCount > 0) {
            broadcast("[ALL_READY]");
        } else {
            broadcast("[NOT_READY]");
        }
    }

    public void startGame() {
        if (clients.isEmpty()) return;
        isGameStarted = false; // 💡 단어가 입력될 때까지 아직 게임 시작 안 한 상태로 대기

        Random rand = new Random();
        int drawerIndex = rand.nextInt(clients.size()); 

        for (int i = 0; i < clients.size(); i++) {
            ClientHandler client = clients.get(i);
            if (i == drawerIndex) {
                client.setDrawer(true); 
                // 💡 단어를 아직 모르므로 닉네임만 전달
                client.sendMessage("[GAME_START],DRAWER," + client.getNickname()); 
            } else {
                client.setDrawer(false); 
                client.sendMessage("[GAME_START],VIEWER," + clients.get(drawerIndex).getNickname());
            }
        }
        // 💡 타이머는 여기서 시작하지 않고, ClientHandler에서 [SET_WORD]를 받으면 시작합니다!
    }

    public void startTimer() {
        stopTimer(); 
        timerThread = new Thread(() -> {
            try {
                for (int i = 60; i >= 0; i--) {
                    broadcast("[TIMER]," + i); 
                    Thread.sleep(1000);
                }
                broadcast("[SYSTEM],⏰ 시간 초과! 문제 풀이에 실패했습니다. (정답은 [" + currentAnswer + "]였습니다.)");
                broadcast("[CLEAR]");
                resetGameFull(); 
            } catch (InterruptedException e) {}
        });
        timerThread.start();
    }

    public void stopTimer() {
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
    }

    public String getRankings() {
        List<ClientHandler> sorted = new ArrayList<>(clients);
        sorted.sort((c1, c2) -> Integer.compare(c2.getScore(), c1.getScore()));

        StringBuilder sb = new StringBuilder();
        int rank = 1;
        for (ClientHandler c : sorted) {
            sb.append(rank).append("등: ").append(c.getNickname()).append("(").append(c.getScore()).append("점)  ");
            rank++;
        }
        return sb.toString();
    }

    public void resetGameFull() {
        stopTimer();
        isGameStarted = false;
        currentAnswer = null; // 정답 초기화
        for (ClientHandler client : clients) {
            client.setReady(false);
            client.setScore(0); 
            client.setDrawer(false); 
        }
        checkAllReady();
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
        checkAllReady(); 
    }

    public static void main(String[] args) {
        new ServerMain().start();
    }
}
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {
    // 서버가 접속을 대기할 포트 번호 (임의로 8080 지정)
    private static final int PORT = 8080;
    
    // 현재 접속한 클라이언트(유저)들을 관리하는 리스트
    private List<ClientHandler> clients = new ArrayList<>();

    // 서버 실행 메서드
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("캐치마인드 서버가 " + PORT + " 포트에서 시작되었습니다!");

            // 무한 루프를 돌며 클라이언트의 접속을 계속 기다림
            while (true) {
                // 클라이언트가 접속하면 socket을 생성하여 연결
                Socket clientSocket = serverSocket.accept();
                System.out.println("새로운 유저 접속! IP: " + clientSocket.getInetAddress());

                // 해당 유저를 전담할 스레드(ClientHandler) 생성
                ClientHandler handler = new ClientHandler(clientSocket, this);
                
                // 동기화 블록: 리스트에 안전하게 추가 (동시성 문제 방지)
                synchronized (clients) {
                    clients.add(handler);
                }

                // 스레드 실행 (이때부터 이 유저의 데이터는 handler가 전담해서 받음)
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ⭐ 핵심: 특정 유저가 보낸 메시지를 모든 유저에게 뿌려주는 기능 (Broadcast)
    public void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    // 유저가 나갔을 때 리스트에서 삭제하는 기능
    public void removeClient(ClientHandler client) {
        synchronized (clients) {
            clients.remove(client);
            System.out.println("유저 퇴장. 현재 접속자 수: " + clients.size() + "명");
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }
}

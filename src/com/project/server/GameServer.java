import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    // 포트는 충돌이 적은 5000번대로 변경
    private static final int PORT = 5555;
    
    // ⭐ 다중 스레드 환경에서 절대 꼬이지 않는 안전한 리스트 사용
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("글로벌 캐치마인드 서버가 " + PORT + " 포트에서 가동 중입니다!");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clients.add(handler); // 리스트에 유저 추가
                
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 1) 전체 유저에게 메시지 전송 (채팅, 시스템 메시지용)
    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // 2) ⭐ 보낸 사람 본인을 제외하고 전송 (그림 좌표용)
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
        new GameServer().start();
    }
}

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    // 1. 서버가 귀를 열고 기다릴 포트 번호를 5555로 고정합니다.
    private static final int PORT = 5555;
    
    // 2. 현재 접속 중인 유저(ClientHandler)들을 담아둘 리스트입니다.
    // CopyOnWriteArrayList는 여러 명이 동시에 들어오고 나가도 에러가 나지 않는 아주 튼튼한 특수 리스트입니다.
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    // 3. 서버를 가동하는 메인 메서드입니다.
    public void start() {
        // 4. ServerSocket을 만듭니다. (5555번 방의 문을 엽니다.)
        // try-with-resources 문법을 써서 서버가 꺼질 때 자동으로 소켓이 닫히게 합니다.
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("글로벌 캐치마인드 서버가 " + PORT + " 포트에서 가동 중입니다!");

            // 5. 서버는 절대 꺼지면 안 되므로 무한 루프(while(true))를 돌립니다.
            while (true) {
                // 6. 누군가 접속할 때까지 여기서 코드가 멈춰서 기다립니다(대기).
                // 누군가 접속하면, 그 사람과 통신할 수 있는 전용선(clientSocket)을 연결해 줍니다.
                Socket clientSocket = serverSocket.accept();
                
                // 7. 접속한 유저를 1:1로 전담 마크할 직원(ClientHandler)을 생성합니다.
                // 이때 유저의 전용선(clientSocket)과 서버 자신(this)을 직원에게 넘겨줍니다.
                ClientHandler handler = new ClientHandler(clientSocket, this);
                
                // 8. 방금 만든 직원을 현재 접속 중인 유저 명단(리스트)에 추가합니다.
                clients.add(handler); 
                
                // 9. 직원을 스레드(Thread)로 만들어 독립적으로 일을 시작하게(start) 합니다.
                // 이제 이 유저의 채팅과 그림은 이 직원이 알아서 처리합니다! 메인 서버는 다시 6번으로 돌아가 다음 손님을 기다립니다.
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace(); // 소켓을 여는 데 실패하면 에러를 출력합니다.
        }
    }

    // 10. 접속한 '모든' 유저에게 똑같은 메시지를 쏘는 기능(방송)입니다.
    public void broadcast(String message) {
        // 명단(clients)에 있는 모든 직원(client)을 하나씩 꺼내서
        for (ClientHandler client : clients) {
            client.sendMessage(message); // 각자의 유저에게 메시지를 보내라고 명령합니다.
        }
    }

    // 11. 메시지를 보낸 '나'를 제외하고 나머지 모두에게 쏘는 기능입니다. (그림 그릴 때 렉 방지용)
    public void broadcastExcept(ClientHandler sender, String message) {
        for (ClientHandler client : clients) {
            // 리스트에서 꺼낸 직원이 '나를 담당하는 직원(sender)'이 아닐 때만!
            if (client != sender) {
                client.sendMessage(message); // 메시지를 보냅니다.
            }
        }
    }

    // 12. 유저가 접속을 끊고 나갔을 때 명단에서 지우는 기능입니다.
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("유저 퇴장. 현재 인원: " + clients.size() + "명");
    }

    // 13. 자바 프로그램이 처음 시작되는 곳! 서버 객체를 만들고 start()를 실행합니다.
    public static void main(String[] args) {
        new GameServer().start();
    }
}

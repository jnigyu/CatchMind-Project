import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Thread로 동작하기 위해 Runnable 인터페이스 구현
public class ClientHandler implements Runnable {
    private Socket socket;
    private GameServer server;
    private PrintWriter out;
    private BufferedReader in;

    // 생성자를 통해 할당받은 유저의 소켓과 서버 객체를 저장
    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // 유저에게 데이터를 보낼 통로(out)와 받을 통로(in) 생성
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            // 유저가 데이터를 보내면 실시간으로 계속 읽어들임
            while ((inputLine = in.readLine()) != null) {
                // 받은 데이터를 서버에 넘겨서 모든 사람에게 방송(Broadcast)하도록 지시
                // 예: "[DRAW]100,200,BLACK" 또는 "[CHAT]정답은 사과!"
                System.out.println("수신된 데이터: " + inputLine);
                server.broadcast(inputLine); 
            }
        } catch (IOException e) {
            System.out.println("클라이언트 연결 끊김.");
        } finally {
            // 접속이 끊기면 자원 정리 및 서버 목록에서 제거
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 서버가 Broadcast할 때 이 메서드를 호출하여 유저에게 실제로 데이터를 쏨
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}

import java.io.*;
import java.net.Socket;


public class ClientHandler implements Runnable {
    private Socket socket;
    private GameServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname; // 유저 닉네임 추가

    public ClientHandler(Socket socket, GameServer server) {
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
                // ⭐ 프로토콜 파싱: "명령어|데이터" 구조
                String[] parts = inputLine.split("\\|");
                String command = parts[0];

                switch (command) {
                    case "LOGIN":
                        // 예: "LOGIN|정통공학도"
                        this.nickname = parts[1];
                        server.broadcast("SYS|" + nickname + "님이 게임에 참여했습니다!");
                        break;

                    case "CHAT":
                        // 예: "CHAT|사과"
                        String chatMsg = parts[1];
                        server.broadcast("CHAT|" + nickname + "|" + chatMsg);
                        break;

                    case "DRAW":
                        // 예: "DRAW|100,200,BLACK"
                        String drawData = parts[1];
                        // 그림 좌표는 렉을 줄이기 위해 본인 제외 나머지에게만 전송!
                        server.broadcastExcept(this, "DRAW|" + drawData);
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println((nickname != null ? nickname : "알 수 없는 유저") + " 연결 끊김.");
        } finally {
            // 연결 종료 시 확실하게 자원 닫기 (메모리 누수 방지)
            if (nickname != null) {
                server.broadcast("SYS|" + nickname + "님이 도망갔습니다!");
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
        if (out != null) out.println(message);
    }
}

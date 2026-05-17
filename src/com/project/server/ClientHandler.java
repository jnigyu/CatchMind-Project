import java.io.*;
import java.net.Socket;

// 스레드로 동작하게 하려고 Runnable이라는 자격증(인터페이스)을 땄습니다.
public class ClientHandler implements Runnable {
    private Socket socket;       // 내 담당 유저와 연결된 전화선
    private GameServer server;   // 내가 소속된 메인 서버 (방송을 부탁하기 위해 필요함)
    private PrintWriter out;     // 유저에게 글자를 써서 보낼 편지지(출력 스트림)
    private BufferedReader in;   // 유저가 보낸 글자를 읽을 돋보기(입력 스트림)
    private String nickname;     // 내 담당 유저의 닉네임

    // 1. 직원이 처음 생성될 때 전화선과 소속 서버를 기억해 둡니다.
    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    // 2. 스레드가 start() 되면 자동으로 실행되는 메인 업무 공간입니다.
    @Override
    public void run() {
        try {
            // 3. 전화선(socket)에 편지지(out)와 돋보기(in)를 연결합니다. (통신 준비 완료!)
            // true는 오토 플러시(버퍼가 꽉 차지 않아도 바로바로 보냄) 설정입니다.
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String inputLine;
            // 4. 유저가 데이터를 보낼 때까지 기다리다가(readLine), 데이터가 오면 inputLine에 담습니다.
            // 유저가 강제로 종료해서 데이터가 null이 되기 전까지 이 짓을 무한 반복합니다.
            while ((inputLine = in.readLine()) != null) {
                
                // 5. 프로토콜 분석! 받은 문자열을 "|" 기준으로 쪼갭니다.
                // 예: "LOGIN|홍길동" 이 들어오면 parts[0]은 "LOGIN", parts[1]은 "홍길동"이 됩니다.
                String[] parts = inputLine.split("\\|"); 
                String command = parts[0]; // 명령어 추출 ("LOGIN", "CHAT", "DRAW" 등)

                // 6. 명령어에 따라 알맞은 행동을 합니다.
                switch (command) {
                    case "LOGIN": // 유저가 처음 들어와서 닉네임을 알려줄 때
                        this.nickname = parts[1]; // 내 담당 유저의 닉네임을 저장!
                        // 서버 전체에 "홍길동님이 참여했습니다!" 라고 방송을 부탁합니다.
                        server.broadcast("SYS|" + nickname + "님이 게임에 참여했습니다!");
                        break;

                    case "CHAT": // 유저가 채팅을 쳤을 때
                        String chatMsg = parts[1]; 
                        // 서버 전체에 "홍길동: 안녕!" 이라고 방송을 부탁합니다.
                        server.broadcast("CHAT|" + nickname + "|" + chatMsg);
                        break;

                    case "DRAW": // 유저가 마우스로 그림을 그렸을 때 (좌표 전송)
                        String drawData = parts[1];
                        // 그림 좌표는 렉이 걸리지 않게 보낸 사람(this)을 제외하고 방송해달라고 부탁합니다.
                        server.broadcastExcept(this, "DRAW|" + drawData);
                        break;
                }
            }
        } catch (IOException e) {
            // 통신 중 에러가 나면 연결이 끊긴 것으로 간주합니다.
            System.out.println((nickname != null ? nickname : "알 수 없는 유저") + " 연결 끊김.");
        } finally {
            // 7. 유저가 정상적으로 나가든, 에러가 나서 튕기든 무조건 마지막에 실행되는 정리 구역입니다.
            if (nickname != null) {
                server.broadcast("SYS|" + nickname + "님이 도망갔습니다!");
            }
            // 서버 명단에서 나(직원)를 지워달라고 합니다.
            server.removeClient(this);
            // 8. 썼던 편지지, 돋보기, 전화선을 모두 예쁘게 정리해서 닫아줍니다. (메모리 누수 방지)
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 9. 메인 서버가 "얘한테 메시지 좀 보내!" 라고 할 때 쓰이는 기능입니다.
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message); // 편지지에 글자를 적어서 유저에게 휙 날려 보냅니다.
        }
    }
}

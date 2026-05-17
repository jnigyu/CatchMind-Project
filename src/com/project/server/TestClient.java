import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {
        // 1. 서버에 노크하기 (localhost는 '내 컴퓨터'를 의미, 8080은 아까 연 포트번호)
        try (Socket socket = new Socket("localhost", 8080)) {
            System.out.println("✅ 서버 접속 성공! 메시지를 입력해보세요.");
            
            // 2. 서버로 데이터를 보낼 통로(out)와 키보드 입력(scanner) 준비
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);
            
            // 3. 키보드로 치는 내용을 계속 서버로 전송
            while (true) {
                String msg = scanner.nextLine();
                out.println(msg); // 서버로 슝!
            }
        } catch (Exception e) {
            System.out.println("❌ 서버에 접속할 수 없습니다. 서버가 켜져 있는지 확인하세요.");
        }
    }
}

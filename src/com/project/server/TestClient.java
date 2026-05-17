import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {
        // 1. 내 컴퓨터(localhost)의 5555번 포트(서버가 있는 곳)로 연결을 시도합니다.
        try (Socket socket = new Socket("localhost", 5555)) {
            System.out.println("✅ 서버 접속 성공! 메시지를 입력해보세요.");
            
            // 2. 서버로 글자를 쏴줄 편지지(out)를 준비합니다.
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // 3. 내 키보드 입력을 읽어들일 스캐너(scanner)를 준비합니다.
            Scanner scanner = new Scanner(System.in);
            
            // 4. 무한 루프를 돌면서 키보드 입력을 기다립니다.
            while (true) {
                // 키보드에서 엔터를 칠 때까지 기다렸다가 입력한 한 줄을 msg에 담습니다.
                String msg = scanner.nextLine(); 
                
                // 입력한 글자(예: LOGIN|나의닉네임)를 서버로 슝! 날려 보냅니다.
                out.println(msg); 
            }
        } catch (Exception e) {
            // 서버가 꺼져있거나 포트 번호가 틀리면 이리로 와서 에러 메시지를 띄웁니다.
            System.out.println("❌ 서버에 접속할 수 없습니다. 서버가 켜져 있는지 확인하세요.");
        }
    }
}

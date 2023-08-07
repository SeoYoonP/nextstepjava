package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread { //쓰레드 클래스를 상속받으므로 여러 클라이언트의 요청을 동시에 처리가능
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        // 1단계
        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream()) {
            //inputStream을 한 줄 단위로 읽기 위해 BufferReader를 생성
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            //BufferReader.readLine() 메소드를 활용해 라인별로 HTTP 요청 정보를 읽는다
            String line = bufferedReader.readLine();
            if(line == null) {
                return;
            }
            // 2단계
            String[] splited = line.split(" ");
            String url = splited[1];
            while (!"".equals(line)) {
                log.info("header : {} ", line);
                line = bufferedReader.readLine();
            }
            // 3단계
            DataOutputStream dos = new DataOutputStream(out);
            String path = System.getProperty("user.dir") + "/webapp" + url;
//            String path = "C:/Users/PC/Desktop/projects/nextstepjava/web-application-server-gradle/webapp" + url;

            File file = new File(path);
            if (!file.exists()) {
                log.error("File not found: " + file.getAbsolutePath());
                // 404 응답 처리 등
                return;
            }
            byte[] body = Files.readAllBytes(file.toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.writeBytes("\r\n");
            System.out.println("Buffer size before flush: " + dos.size());
            dos.flush();
            System.out.println("Buffer size after flush: " + dos.size());

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}

package httpserver;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Diego Machado and Wladimir Cabral
 */
public final class HTTPServer {

    public static void main(String argv[]) throws Exception {
        // Define a porta do socket.
        int port = 8080;
        // Estabelece o socket.
        ServerSocket socket = new ServerSocket(port);
        // Processa requisicoes HTTPHandler infinitamente.
        while (true) {
            // Escuta por uma requisicao de conexao TCP 
            Socket tcpConn = socket.accept();
            // Constroi um objeto da classe HTTPHandler para processar as requisicoes HTTPHandler
            HTTPHandler request = new HTTPHandler(tcpConn);
            // Cria uma nova thread para processar cada requisicao.
            Thread thread = new Thread(request);
            // Inicia a thread.
            thread.start();
        }
    }
}

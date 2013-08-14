package httpserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 *
 * @author Diego Machado and Wladimir Cabral
 */
public class HTTPHandler implements Runnable {

    final static String quebraLinha = "\r\n";
    Socket socket;

    //Construtor!
    public HTTPHandler(Socket socket) throws Exception {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            trataRequisicao();
        } catch (Exception error) {
            System.out.println("Erro ao criar a Thread!\n" + error);
        }
    }

    private void trataRequisicao() throws Exception {

        //Cria o stream de entrada de dados do socket
        InputStream inputStream = socket.getInputStream();
        //Cria o stream de saida de dados pelo socket
        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
        //Cria o filtro de leitura dos dados vindos do socket
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        //Pega a linha de requisicao da mensagem HTTPHandler
        String requestLine = br.readLine();
        //Divide a linha lida em "tokens"
        StringTokenizer tokens = new StringTokenizer(requestLine);
        // Pula a leitura do primeiro token que deveria ser "GET"
        tokens.nextToken();
        //Passa para o proximo token que deve ser o nome do arquivo a ser baixado
        String fileName = tokens.nextToken();

        // Caso o nome do arquivo no GET for '/' (root) procurar por index.html dentro do diretorio
        String root = "/";
        if (fileName.equals(root)) {
            fileName = "index.html";
        } else {
            //Caso contrario adiciona um "." ao nome do arquivo para a requisicao ser feita dentro do diretorio corrente
            fileName = "." + fileName;
        }

        // Cria o stream de entrada da leitura do arquivo para o envio
        FileInputStream fileInput = null;
        //Se o arquivo existir, le do contrario lanca excessao FileNotFound
        boolean fileExists = true;
        try {
            fileInput = new FileInputStream(fileName);
        } catch (FileNotFoundException FNFError) {
            fileExists = false;
            System.out.println("Arquivo nao encontrado! \n" + FNFError);
        }

        // Informações de debug para o servidor (pode ser direcionado para um arquivo de log, por exemplo...)
        System.out.println("\n--------------- Conexão estabelecida! ---------------\n");
        System.out.println(requestLine);
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }

        // Constroi a mensagem de resposta
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (fileExists) {
            statusLine = "HTTP/1.0 200 OK" + quebraLinha;
            contentTypeLine = "Content-Type: " + contentType(fileName) + quebraLinha;
        } else {
            statusLine = "HTTP/1.0 404 Not Found" + quebraLinha;
            contentTypeLine = "Content-Type: text/html" + quebraLinha;
            entityBody = "<HTML>" + "<HEAD><TITLE>Page not Found</TITLE></HEAD>"
                    + "<BODY>Page Not Found</BODY></HTML>";
        }

        // Envia a status line.
        outStream.writeBytes(statusLine);
        // Envia a content type line.
        outStream.writeBytes(contentTypeLine);
        // Envia uma linha em branco para indicar o fim das linhas de cabecalho
        outStream.writeBytes(quebraLinha);
        // Envia o corpo do arquivo (da pagina que sera formada no browser).
        if (fileExists) {
            sendBytes(fileInput, outStream);
            fileInput.close();
        } else {
            outStream.writeBytes(entityBody);
        }

        // Fecha os streams e o socket.
        outStream.close();
        br.close();
        socket.close();
    }

    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
        // Constroi um vetor de 1024 bytes (ou 1Kbyte) para ser o buffer dos bytes
        // que irao para o socket.
        byte[] buffer = new byte[1024];
        int bytes = 0;
        // Copia o arquivo requisitado para o buffer de saida e entao para o socket
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if (fileName.endsWith(".ram") || fileName.endsWith(".ra")) {
            return "audio/x-pn-realaudio";
        }
        return "application/octet-stream";
    }
}

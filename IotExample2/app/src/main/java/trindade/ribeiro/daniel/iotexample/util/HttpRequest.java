package trindade.ribeiro.daniel.iotexample.util;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String requestUrl;
    private String charset;

    private HashMap<String, String> params = new HashMap<>();
    private HashMap<String, File> files = new HashMap<>();

    private String user = "";
    private String password = "";
    private String token = "";

    private int timeout = 3000;

    private String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private OutputStream outputStream;
    private PrintWriter writer;

    // Construtor da classe HttpRequest
    // requestUrl -> o endereco a ser conectado
    // method -> metodo HTTP a ser usado. Pode ser GET ou POST
    // charset -> codificacao de caracteres usada na troca de dados. Geralmente UTF-8
    public HttpRequest(String requestUrl, String method, String charset) {
        this.method = method;
        this.requestUrl = requestUrl;
        this.charset = charset;
    }

    // Adiciona um parametro a ser enviado ao servidor. Funciona com conexoes GET e POST.
    public void addParam(String key, String value) {
        this.params.put(key, value);
    }

    // Adiciona um arquivo a ser enviado ao servidor. Funciona apenas com conexoes POST.
    public void addFile(String key, File file) {
        this.files.put(key, file);
    }

    // Caso seja necessario autenticacao, seta as informacoes de user e password
    public void setBasicAuth(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public void setTokenAuth(String token) {
        this.token = token;
    }

    // Seta o tempo maximo (em milisegundos) de espera por uma resposta do servidor.
    public void setTimeout(int time) {
        this.timeout = time;
    }

    // Executa a conexao: anexa os parametros e arquivos a serem enviados ao servidor, abre uma
    // conexao com o servidor. Ao final, retorna a resposta do servidor na forma de um InputStream,
    // o qual deve ser convertido para o dado originalmente enviado.
    public InputStream execute() throws IOException {
        String urlParameters = "";

        if (!params.isEmpty()) {
            Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pair = it.next();
                urlParameters += URLEncoder.encode(pair.getKey(), this.charset);
                urlParameters += "=";
                urlParameters += URLEncoder.encode(pair.getValue(), this.charset);
                urlParameters += "&";
            }
            urlParameters = urlParameters.substring(0, urlParameters.length() - 1);
        }

        if (method.equals("GET")) {
            requestUrl = requestUrl + "?" + urlParameters;
        }

        URL url = new URL(this.requestUrl);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoInput(true);

        // Timeout for reading InputStream arbitrarily set to 3000ms.
        httpConn.setReadTimeout(3000);
        // Timeout for connection.connect() arbitrarily set to 3000ms.
        httpConn.setConnectTimeout(3000);
        // GET or POST
        httpConn.setRequestMethod(method);

        if (!this.user.isEmpty() && !this.password.isEmpty()) {
            String auth = user + ":" + password;
            byte[] encodedAuth = Base64.encode(auth.getBytes(), Base64.NO_WRAP);
            String authHeaderValue = "Basic " + new String(encodedAuth);
            httpConn.setRequestProperty("Authorization", authHeaderValue);
        }

        if (!this.token.isEmpty()) {
            String auth = this.token;
            byte[] encodedAuth = Base64.encode(auth.getBytes(), Base64.NO_WRAP);
            String authHeaderValue = "Bearer " + new String(encodedAuth);
            httpConn.setRequestProperty("Authorization", authHeaderValue);
        }

        if (method.equals("POST")) {
            httpConn.setDoOutput(true);    // indicates POST method
            if (files.isEmpty()) {
                httpConn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;
                httpConn.setRequestProperty("charset", this.charset);
                httpConn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
                outputStream = httpConn.getOutputStream();
                outputStream.write(postData);
            } else {
                boundary = "===" + System.currentTimeMillis() + "===";
                httpConn.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + boundary);
                outputStream = httpConn.getOutputStream();
                writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                        true);
                Iterator<Map.Entry<String, String>> itParams = params.entrySet().iterator();
                while (itParams.hasNext()) {
                    Map.Entry<String, String> pair = itParams.next();
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"" + pair.getKey() + "\"")
                            .append(LINE_FEED);
                    writer.append("Content-Type: text/plain; charset=" + charset).append(
                            LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.append(pair.getValue()).append(LINE_FEED);
                    writer.flush();
                }

                Iterator<Map.Entry<String, File>> itFiles = files.entrySet().iterator();
                while (itFiles.hasNext()) {
                    Map.Entry<String, File> pair = itFiles.next();
                    File file = pair.getValue();
                    String fileName = file.getName();
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"" + pair.getKey()
                                    + "\"; filename=\"" + fileName + "\"")
                            .append(LINE_FEED);
                    writer.append(
                                    "Content-Type: "
                                            + URLConnection.guessContentTypeFromName(fileName))
                            .append(LINE_FEED);
                    writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.flush();

                    FileInputStream inputStream = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                    inputStream.close();
                    writer.append(LINE_FEED);
                    writer.flush();

                }

                writer.append(LINE_FEED).flush();
                writer.append("--" + boundary + "--").append(LINE_FEED);
                writer.close();
            }
        }

        int responseCode = httpConn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }
        // Retrieve the response body as an InputStream.
        return httpConn.getInputStream();
    }

    // Fecha a conexao
    public void finish() throws IOException {
        if (httpConn != null) {
            httpConn.disconnect();
        }
    }

    // Métodos adicionais para interagir com o ESP32:

    // Método para ligar o relé 1
    public void ligarRele1() {
        HttpRequest httpRequest = new HttpRequest("http://192.168.1.100/ligar1", "POST", "UTF-8");
        try {
            InputStream response = httpRequest.execute();
            // Processar a resposta, se necessário
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpRequest.finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para ligar o relé 2
    public void ligarRele2() {
        HttpRequest httpRequest = new HttpRequest("http://192.168.1.100/ligar2", "POST", "UTF-8");
        try {
            InputStream response = httpRequest.execute();
            // Processar a resposta, se necessário
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpRequest.finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para obter o estado atual das umidades
    public String obterUmidades() {
        HttpRequest httpRequest = new HttpRequest("http://192.168.1.100/", "GET", "UTF-8");
        try {
            InputStream response = httpRequest.execute();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                httpRequest.finish();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

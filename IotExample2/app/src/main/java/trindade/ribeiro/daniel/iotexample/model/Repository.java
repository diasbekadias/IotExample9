package trindade.ribeiro.daniel.iotexample.model;

import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import trindade.ribeiro.daniel.iotexample.util.Config;
import trindade.ribeiro.daniel.iotexample.util.App;

public class Repository {

    // URL base para as requisições
    private String baseUrl;

    // Construtor que inicializa o baseUrl usando o endereço do ESP32
    public Repository() {
        String esp32Address = Config.getESP32Address(App.getContext());
        if (esp32Address == null || esp32Address.isEmpty()) {
            // Loga um erro se o endereço estiver nulo ou vazio
            Log.e("Repository", "ESP32 address is null or empty");
            this.baseUrl = null;
        } else {
            // Valida a URL e atribui a baseUrl
            this.baseUrl = validateUrl(esp32Address);
        }
        // Loga a URL base inicializada
        Log.d("Repository", "Base URL initialized: " + this.baseUrl);
    }

    // Método para definir a URL base
    public void setBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            // Loga um erro se a URL for nula ou vazia
            Log.e("Repository", "Attempted to set an empty or null base URL");
            this.baseUrl = null;
        } else {
            // Valida a URL e a atribui a baseUrl
            this.baseUrl = validateUrl(baseUrl);
            // Loga a URL base atualizada
            Log.d("Repository", "Base URL updated: " + this.baseUrl);
        }
    }

    // Método para validar a URL, adicionando "http://" se necessário
    private String validateUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "http://" + url;
        }
        return url;
    }

    // Método para ligar a irrigação
    public LiveData<Boolean> turnIrrigacaoOn() {
        return sendRequest("ligar", "PUT");
    }

    // Método para desligar a irrigação
    public LiveData<Boolean> turnIrrigacaoOff() {
        return sendRequest("desligar", "PUT");
    }

    // Método para obter o status da irrigação
    public LiveData<Boolean> getIrrigacaoStatus() {
        MutableLiveData<Boolean> status = new MutableLiveData<>();
        new GetIrrigacaoStatusTask(status).execute();
        return status;
    }

    // Método para definir a umidade
    public void setUmidade(int umidade) {
        new SetUmidadeTask().execute(umidade);
    }

    // Método para obter a umidade
    public LiveData<Integer> getUmidade() {
        MutableLiveData<Integer> umidade = new MutableLiveData<>();
        new GetUmidadeTask(umidade).execute();
        return umidade;
    }

    // Método para enviar o comando de início do sistema com a umidade escolhida
    public void enviarComandoInicioSistema(int umidadeEscolhida) {
        new EnviarComandoInicioSistemaTask().execute(umidadeEscolhida);
    }

    // Método privado para enviar uma requisição HTTP para um endpoint específico usando o método HTTP especificado
    private LiveData<Boolean> sendRequest(String endpoint, String method) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        new SendRequestTask(result, endpoint, method).execute();
        return result;
    }

    // Classe AsyncTask para obter o status da irrigação
    private class GetIrrigacaoStatusTask extends AsyncTask<Void, Void, Boolean> {
        private MutableLiveData<Boolean> status;

        // Construtor que aceita um MutableLiveData<Boolean> para armazenar o status da irrigação
        GetIrrigacaoStatusTask(MutableLiveData<Boolean> status) {
            this.status = status;
        }

        // Método executado em segundo plano para obter o status da irrigação
        @Override
        protected Boolean doInBackground(Void... voids) {
            if (baseUrl == null) {
                Log.e("Repository", "Base URL is null");
                return false;
            }
            HttpURLConnection conn = null;
            BufferedReader in = null;
            try {
                String fullUrl = baseUrl + "/status";
                Log.d("Repository", "Fetching irrigacao status from: " + fullUrl);
                URL url = new URL(fullUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getBoolean("irrigacaoStatus");
            } catch (IOException | JSONException e) {
                Log.e("Repository", "Error fetching irrigacao status", e);
                return false;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e("Repository", "Error closing BufferedReader", e);
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        // Método executado após a conclusão do método doInBackground, atualizando o valor do LiveData
        @Override
        protected void onPostExecute(Boolean result) {
            status.setValue(result);
        }
    }

    // Classe para definir a umidade
    private class SetUmidadeTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            if (baseUrl == null) {
                Log.e("Repository", "Base URL is null");
                return null;
            }
            int umidade = params[0];
            HttpURLConnection conn = null;
            OutputStreamWriter out = null;
            try {
                String fullUrl = baseUrl + "/umidade";
                Log.d("Repository", "Setting umidade to " + umidade + " at: " + fullUrl);
                URL url = new URL(fullUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setDoOutput(true);
                out = new OutputStreamWriter(conn.getOutputStream());
                out.write("umidade=" + umidade);
                out.flush();
                conn.getInputStream();
            } catch (IOException e) {
                Log.e("Repository", "Error setting umidade", e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.e("Repository", "Error closing OutputStreamWriter", e);
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }

    // Classe AsyncTask para obter a umidade
    private class GetUmidadeTask extends AsyncTask<Void, Void, Integer> {
        private MutableLiveData<Integer> umidade;

        // Construtor que aceita um MutableLiveData<Integer> para armazenar a umidade
        GetUmidadeTask(MutableLiveData<Integer> umidade) {
            this.umidade = umidade;
        }

        // Método executado em segundo plano para obter a umidade
        @Override
        protected Integer doInBackground(Void... voids) {
            if (baseUrl == null) {
                Log.e("Repository", "Base URL is null");
                return 0;
            }
            HttpURLConnection conn = null;
            BufferedReader in = null;
            try {
                String fullUrl = baseUrl + "/umidade";
                Log.d("Repository", "Fetching umidade from: " + fullUrl);
                URL url = new URL(fullUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getInt("umidade");
            } catch (IOException | JSONException e) {
                Log.e("Repository", "Error fetching umidade", e);
                return 0;
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.e("Repository", "Error closing BufferedReader", e);
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        // Método executado após a conclusão do método doInBackground, atualizando o valor do LiveData
        @Override
        protected void onPostExecute(Integer result) {
            umidade.setValue(result);
        }
    }

    // Classe AsyncTask para enviar requisições HTTP
    private class SendRequestTask extends AsyncTask<Void, Void, Boolean> {
        private MutableLiveData<Boolean> result;
        private String endpoint;
        private String method;

        // Construtor que aceita um MutableLiveData<Boolean>, o endpoint e o método HTTP para a requisição
        SendRequestTask(MutableLiveData<Boolean> result, String endpoint, String method) {
            this.result = result;
            this.endpoint = endpoint;
            this.method = method;
        }

        // Método executado em segundo plano para enviar a requisição HTTP
        @Override
        protected Boolean doInBackground(Void... voids) {
            if (baseUrl == null) {
                Log.e("Repository", "Base URL is null");
                return false;
            }
            HttpURLConnection conn = null;
            try {
                String fullUrl = baseUrl + "/" + endpoint;
                Log.d("Repository", "Sending request to: " + fullUrl);
                URL url = new URL(fullUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.getInputStream(); // Trigger the request
                return true;
            } catch (IOException e) {
                Log.e("Repository", "Error sending request", e);
                return false;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        // Método executado após a conclusão do método doInBackground, atualizando o valor do LiveData
        @Override
        protected void onPostExecute(Boolean success) {
            result.setValue(success);
        }
    }

    // Classe AsyncTask para enviar o comando de início do sistema com a umidade escolhida
    private class EnviarComandoInicioSistemaTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... params) {
            if (baseUrl == null) {
                Log.e("Repository", "Base URL is null");
                return null;
            }
            int umidadeEscolhida = params[0];
            HttpURLConnection conn = null;
            OutputStreamWriter out = null;
            try {
                String fullUrl = baseUrl + "/iniciar-sistema";
                Log.d("Repository", "Enviando comando de inicio do sistema para: " + fullUrl);
                URL url = new URL(fullUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                out = new OutputStreamWriter(conn.getOutputStream());
                out.write("umidade=" + umidadeEscolhida);
                out.flush();
                conn.getInputStream();
            } catch (IOException e) {
                Log.e("Repository", "Erro ao enviar comando de inicio do sistema", e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.e("Repository", "Erro ao fechar OutputStreamWriter", e);
                    }
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return null;
        }
    }
}

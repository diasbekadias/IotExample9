package trindade.ribeiro.daniel.iotexample.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {

    /**
     * Salva o endereço do ESP32 no espaço reservado da app
     * @param context contexto da app
     * @param address o endereço do ESP32
     */
    public static void setESP32Address(Context context, String address) {
        // Obtém a instância de SharedPreferences com o nome "configs" e modo privado
        SharedPreferences mPrefs = context.getSharedPreferences("configs", Context.MODE_PRIVATE);

        // Obtém o editor de SharedPreferences para fazer alterações
        SharedPreferences.Editor mEditor = mPrefs.edit();

        // Salva o endereço do ESP32 nas preferências com a chave "esp32address"
        mEditor.putString("esp32address", address).apply();
    }

    /**
     * Obtém o endereço do ESP32 salvo no espaço reservado da app
     * @param context contexto da app
     * @return o endereço do ESP32
     */
    public static String getESP32Address(Context context) {
        // Obtém a instância de SharedPreferences com o nome "configs" e modo privado
        SharedPreferences mPrefs = context.getSharedPreferences("configs", Context.MODE_PRIVATE);

        // Retorna o valor armazenado com a chave "esp32address", ou uma string vazia se não existir
        return mPrefs.getString("esp32address", "");
    }
}

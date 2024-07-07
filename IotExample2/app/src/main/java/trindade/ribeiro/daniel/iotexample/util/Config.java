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
        SharedPreferences mPrefs = context.getSharedPreferences("configs", Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString("esp32address", address).apply();
    }

    /**
     * Obtem o endereço do ESP32 salvo no espaço reservado da app
     * @param context contexto da app
     * @return o endereço do ESP32
     */
    public static String getESP32Address(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences("configs", Context.MODE_PRIVATE);
        return mPrefs.getString("esp32address", "");
    }
}

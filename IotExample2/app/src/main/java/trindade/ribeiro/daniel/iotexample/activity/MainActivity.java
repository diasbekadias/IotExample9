package trindade.ribeiro.daniel.iotexample.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import trindade.ribeiro.daniel.iotexample.util.Config;
import trindade.ribeiro.daniel.iotexample.model.MainActivityViewModel;
import trindade.ribeiro.daniel.iotexample.R;

public class MainActivity extends AppCompatActivity {

    // Define uma variável booleana para armazenar o status da irrigação
    boolean irrigacaoStatus = false;
    // Define uma variável para armazenar a instância do ViewModel
    MainActivityViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Habilita o modo edge-to-edge
        EdgeToEdge.enable(this);
        // Define o layout para a atividade
        setContentView(R.layout.activity_main);
        // Aplica insets da janela para lidar com barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configura a toolbar
        Toolbar toolbar = findViewById(R.id.tbMain);
        setSupportActionBar(toolbar);

        // Inicializa o ViewModel
        vm = new ViewModelProvider(this).get(MainActivityViewModel.class);

        // Atualiza o status da irrigação
        updateIrrigacaoStatus();

        // Configura o botão de irrigação e seu "clique"
        Button btnIrrigacao = findViewById(R.id.btnIrrigacao);
        btnIrrigacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Desabilita o botão para prevenir múltiplos cliques
                v.setEnabled(false);
                LiveData<Boolean> resLD;
                // Verifica o status atual da irrigação e envia o comando apropriado
                if (irrigacaoStatus) {
                    resLD = vm.turnIrrigacaoOff();
                } else {
                    resLD = vm.turnIrrigacaoOn();
                    int umidadeEscolhida = ((SeekBar) findViewById(R.id.skUmidade)).getProgress();
                    vm.enviarComandoInicioSistema(umidadeEscolhida);
                }

                // Observa o resultado do comando e atualiza a UI conforme necessário
                resLD.observe(MainActivity.this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        updateIrrigacaoStatus();
                        // Reabilita o botão após a atualização do status
                        v.setEnabled(true);
                    }
                });
            }
        });

        // Configura o TextView e o SeekBar para umidade e seus listeners
        TextView tvUmidadeRes = findViewById(R.id.tvUmidadeRes);
        SeekBar skUmidade = findViewById(R.id.skUmidade);
        skUmidade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Atualiza o TextView para exibir o progresso atual do SeekBar
                tvUmidadeRes.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Define o novo valor de umidade no ViewModel quando o usuário para de ajustar
                int newUmidade = seekBar.getProgress();
                vm.setUmidade(newUmidade);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Layout do menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_tb, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Lida com as seleções de itens do menu
        if (item.getItemId() == R.id.opConfig) {
            // Layout do diálogo de configuração
            LayoutInflater inflater = getLayoutInflater();
            View configDlgView = inflater.inflate(R.layout.config_dlg, null);
            // Obtém o EditText para o endereço ESP32 e define seu texto a partir da configuração
            EditText etESP32Address = configDlgView.findViewById(R.id.etESP32Address);
            etESP32Address.setText(Config.getESP32Address(this));

            // Constrói e exibe o diálogo de configuração
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(configDlgView);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Salva o novo endereço ESP32 na configuração quando o usuário clica em "Ok"
                    String esp32Address = etESP32Address.getText().toString();
                    Config.setESP32Address(MainActivity.this, esp32Address);
                }
            });

            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {}
            });

            builder.create().show();
            return true;
        }

        if (item.getItemId() == R.id.opUpdate) {
            // Atualiza o status da irrigação quando o usuário seleciona o item de menu de atualização
            updateIrrigacaoStatus();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void updateIrrigacaoStatus() {
        // Obtém referências para o TextView e o botão de status da irrigação
        TextView tvIrrigacaoStatusRes = findViewById(R.id.tvIrrigacaoStatusRes);
        Button btnIrrigacao = findViewById(R.id.btnIrrigacao);
        // Observa o LiveData de status da irrigação do ViewModel
        LiveData<Boolean> irrigacaoStatusLD = vm.getIrrigacaoStatus();

        irrigacaoStatusLD.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                // Atualiza a UI com base no novo status da irrigação
                irrigacaoStatus = aBoolean;
                if (irrigacaoStatus) {
                    tvIrrigacaoStatusRes.setText("Ligada");
                    btnIrrigacao.setText("Desligar");
                } else {
                    tvIrrigacaoStatusRes.setText("Desligada");
                    btnIrrigacao.setText("Ligar");
                }
            }
        });

        // Obtém referências para o TextView e o SeekBar de umidade
        TextView tvUmidadeRes = findViewById(R.id.tvUmidadeRes);
        LiveData<Integer> umidadeResLD = vm.getUmidade();

        umidadeResLD.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                // Atualiza o TextView com o novo valor de umidade
                tvUmidadeRes.setText(String.valueOf(integer));
            }
        });

        SeekBar skUmidade = findViewById(R.id.skUmidade);
        LiveData<Integer> umidadeLD = vm.getUmidade();

        umidadeLD.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                // Atualiza o progresso do SeekBar com o novo valor de umidade
                skUmidade.setProgress(integer);
            }
        });
    }
}

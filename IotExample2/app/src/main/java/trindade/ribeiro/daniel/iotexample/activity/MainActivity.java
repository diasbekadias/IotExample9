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

    boolean irrigacaoStatus = false;
    MainActivityViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.tbMain);
        setSupportActionBar(toolbar);

        vm = new ViewModelProvider(this).get(MainActivityViewModel.class);

        updateIrrigacaoStatus();

        Button btnIrrigacao = findViewById(R.id.btnIrrigacao);
        btnIrrigacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                LiveData<Boolean> resLD;
                if (irrigacaoStatus) {
                    resLD = vm.turnIrrigacaoOff();
                } else {
                    resLD = vm.turnIrrigacaoOn();
                    int umidadeEscolhida = ((SeekBar) findViewById(R.id.skUmidade)).getProgress();
                    vm.enviarComandoInicioSistema(umidadeEscolhida);
                }

                resLD.observe(MainActivity.this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        updateIrrigacaoStatus();
                        v.setEnabled(true);
                    }
                });
            }
        });

        TextView tvUmidadeRes = findViewById(R.id.tvUmidadeRes);
        SeekBar skUmidade = findViewById(R.id.skUmidade);
        skUmidade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvUmidadeRes.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int newUmidade = seekBar.getProgress();
                vm.setUmidade(newUmidade);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_tb, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opConfig) {
            LayoutInflater inflater = getLayoutInflater();
            View configDlgView = inflater.inflate(R.layout.config_dlg, null);
            EditText etESP32Address = configDlgView.findViewById(R.id.etESP32Address);
            etESP32Address.setText(Config.getESP32Address(this));

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(configDlgView);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
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
            updateIrrigacaoStatus();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void updateIrrigacaoStatus() {
        TextView tvIrrigacaoStatusRes = findViewById(R.id.tvIrrigacaoStatusRes);
        Button btnIrrigacao = findViewById(R.id.btnIrrigacao);
        LiveData<Boolean> irrigacaoStatusLD = vm.getIrrigacaoStatus();

        irrigacaoStatusLD.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
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

        TextView tvUmidadeRes = findViewById(R.id.tvUmidadeRes);
        LiveData<Integer> umidadeResLD = vm.getUmidade();

        umidadeResLD.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                tvUmidadeRes.setText(String.valueOf(integer));
            }
        });

        SeekBar skUmidade = findViewById(R.id.skUmidade);
        LiveData<Integer> umidadeLD = vm.getUmidade();

        umidadeLD.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                skUmidade.setProgress(integer);
            }
        });
    }
}

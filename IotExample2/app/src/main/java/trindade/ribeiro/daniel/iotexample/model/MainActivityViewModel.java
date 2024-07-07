package trindade.ribeiro.daniel.iotexample.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {
    private final Repository repository;

    public MainActivityViewModel() {
        this.repository = new Repository();
    }

    public LiveData<Boolean> turnIrrigacaoOn() {
        return repository.turnIrrigacaoOn();
    }

    public LiveData<Boolean> turnIrrigacaoOff() {
        return repository.turnIrrigacaoOff();
    }

    public LiveData<Boolean> getIrrigacaoStatus() {
        return repository.getIrrigacaoStatus();
    }

    public void setUmidade(int umidade) {
        repository.setUmidade(umidade);
    }

    public LiveData<Integer> getUmidade() {
        return repository.getUmidade();
    }

    public void enviarComandoInicioSistema(int umidadeEscolhida) {
        repository.enviarComandoInicioSistema(umidadeEscolhida);
    }
}

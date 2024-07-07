package trindade.ribeiro.daniel.iotexample.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {
    // Instância do repositório para gerenciar dados
    private final Repository repository;

    // Construtor que inicializa o repositório
    public MainActivityViewModel() {
        this.repository = new Repository();
    }

    // Método para ligar a irrigação
    public LiveData<Boolean> turnIrrigacaoOn() {
        return repository.turnIrrigacaoOn();
    }

    // Método para desligar a irrigação
    public LiveData<Boolean> turnIrrigacaoOff() {
        return repository.turnIrrigacaoOff();
    }

    // Método para obter o status atual da irrigação
    public LiveData<Boolean> getIrrigacaoStatus() {
        return repository.getIrrigacaoStatus();
    }

    // Método para definir o nível de umidade
    public void setUmidade(int umidade) {
        repository.setUmidade(umidade);
    }

    // Método para obter o nível atual de umidade
    public LiveData<Integer> getUmidade() {
        return repository.getUmidade();
    }

    // Método para enviar o comando de início do sistema com a umidade escolhida
    public void enviarComandoInicioSistema(int umidadeEscolhida) {
        repository.enviarComandoInicioSistema(umidadeEscolhida);
    }
}

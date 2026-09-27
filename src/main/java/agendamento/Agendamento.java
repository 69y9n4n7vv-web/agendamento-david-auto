package agendamento;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeCliente;
    private String telefone;
    private String tipoVeiculo;
    private String servico;
    private Double valor;
    private LocalDateTime dataHora;

    private String status = "PENDENTE"; // PENDENTE, CONCLUIDO, CANCELADO

    public Agendamento() {}

    public Agendamento(String nomeCliente, String telefone, String tipoVeiculo, String servico, Double valor, LocalDateTime dataHora) {
        this.nomeCliente = nomeCliente;
        this.telefone = telefone;
        this.tipoVeiculo = tipoVeiculo;
        this.servico = servico;
        this.valor = valor;
        this.dataHora = dataHora;
        this.status = "PENDENTE";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getTipoVeiculo() { return tipoVeiculo; }
    public void setTipoVeiculo(String tipoVeiculo) { this.tipoVeiculo = tipoVeiculo; }

    public String getServico() { return servico; }
    public void setServico(String servico) { this.servico = servico; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
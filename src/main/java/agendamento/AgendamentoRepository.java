package agendamento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Verifica se existe agendamento no horário IGNORANDO cancelados
    boolean existsByDataHoraAndStatusNotIgnoreCase(LocalDateTime dataHora, String statusExcluido);

    // Busca todos os agendamentos do dia
    List<Agendamento> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    // Busca agendamentos do dia IGNORANDO cancelados
    List<Agendamento> findByDataHoraBetweenAndStatusNotIgnoreCase(LocalDateTime inicio, LocalDateTime fim, String statusExcluido);
}
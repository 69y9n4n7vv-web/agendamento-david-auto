package agendamento;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AgendamentoController {

    private final AgendamentoRepository repository;

    public AgendamentoController(AgendamentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String processarLogin(@RequestParam String senha, HttpSession session, RedirectAttributes redirectAttributes) {
        if ("david123".equals(senha)) {
            session.setAttribute("adminLogado", true);
            return "redirect:/admin";
        }
        redirectAttributes.addFlashAttribute("erro", "Senha incorreta!");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/admin")
    public String admin(@RequestParam(required = false) String dataFiltro, Model model, HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        List<Agendamento> lista;
        if (dataFiltro != null && !dataFiltro.isEmpty()) {
            LocalDate data = LocalDate.parse(dataFiltro);
            LocalDateTime inicio = data.atStartOfDay();
            LocalDateTime fim = data.atTime(LocalTime.MAX);
            lista = repository.findByDataHoraBetween(inicio, fim);
            model.addAttribute("dataFiltro", dataFiltro);
        } else {
            lista = repository.findAll();
        }

        model.addAttribute("agendamentos", lista);
        return "admin";
    }

    @PostMapping("/admin/status/{id}")
    public String alterarStatus(@PathVariable Long id, @RequestParam String novoStatus, HttpSession session) {
        if (session.getAttribute("adminLogado") == null) {
            return "redirect:/login";
        }

        repository.findById(id).ifPresent(agendamento -> {
            agendamento.setStatus(novoStatus);
            repository.save(agendamento);
        });

        return "redirect:/admin";
    }

    @PostMapping("/agendar")
    public String agendar(@RequestParam String nomeCliente,
                          @RequestParam String telefone,
                          @RequestParam String tipoVeiculo,
                          @RequestParam String servico,
                          @RequestParam String data,
                          @RequestParam String hora,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        try {
            LocalDate dataAgendamento = LocalDate.parse(data);
            LocalTime horaAgendamento = LocalTime.parse(hora);
            LocalDateTime dataHoraCompleta = dataAgendamento.atTime(horaAgendamento);

            DayOfWeek diaDaSemana = dataAgendamento.getDayOfWeek();
            boolean isFimDeSemana = (diaDaSemana == DayOfWeek.SATURDAY || diaDaSemana == DayOfWeek.SUNDAY);

            if (!isFimDeSemana) {
                model.addAttribute("erro", "O Lava-Jato funciona apenas aos sábados e domingos!");
                return "index";
            }

            // Ignora agendamentos com status CANCELADO ao verificar se o horário está ocupado
            if (repository.existsByDataHoraAndStatusNotIgnoreCase(dataHoraCompleta, "CANCELADO")) {
                model.addAttribute("erro", "O horário das " + hora + " no dia selecionado já está ocupado!");
                return "index";
            }

            Double valor = 0.0;
            if ("Carro".equalsIgnoreCase(tipoVeiculo)) {
                valor = 60.0;
            } else if ("Moto".equalsIgnoreCase(tipoVeiculo)) {
                if ("Simples".equalsIgnoreCase(servico)) {
                    valor = 20.0;
                } else if ("Detalhada".equalsIgnoreCase(servico)) {
                    valor = 30.0;
                }
            }

            Agendamento agendamento = new Agendamento(
                    nomeCliente,
                    telefone,
                    tipoVeiculo,
                    servico,
                    valor,
                    dataHoraCompleta
            );

            if (agendamento.getStatus() == null || agendamento.getStatus().isEmpty()) {
                agendamento.setStatus("PENDENTE");
            }

            repository.save(agendamento);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataFormatada = dataAgendamento.format(fmt);

            redirectAttributes.addFlashAttribute("nomeCliente", nomeCliente);
            redirectAttributes.addFlashAttribute("telefone", telefone);
            redirectAttributes.addFlashAttribute("tipoVeiculo", tipoVeiculo);
            redirectAttributes.addFlashAttribute("servico", servico);
            redirectAttributes.addFlashAttribute("dataStr", dataFormatada);
            redirectAttributes.addFlashAttribute("horaStr", hora);
            redirectAttributes.addFlashAttribute("agendou", true);

            return "redirect:/sucesso";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Ocorreu um erro ao processar seu agendamento. Tente novamente.");
            return "index";
        }
    }

    @GetMapping("/horarios-ocupados")
    @ResponseBody
    public List<String> buscarHorariosOcupados(@RequestParam("data") String data) {
        List<String> horasOcupadas = new ArrayList<>();
        try {
            LocalDate dataSelecionada = LocalDate.parse(data);
            LocalDateTime inicioDia = dataSelecionada.atStartOfDay();
            LocalDateTime fimDia = dataSelecionada.atTime(LocalTime.MAX);

            // Filtra ignorando os agendamentos que estão como CANCELADO
            List<Agendamento> agendamentosValidos = repository.findByDataHoraBetweenAndStatusNotIgnoreCase(inicioDia, fimDia, "CANCELADO");

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

            for (Agendamento a : agendamentosValidos) {
                if (a.getDataHora() != null) {
                    horasOcupadas.add(a.getDataHora().format(fmt));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return horasOcupadas;
    }

    @GetMapping("/sucesso")
    public String sucesso(Model model) {
        if (!model.containsAttribute("agendou")) {
            return "redirect:/";
        }
        return "sucesso";
    }
}
package org.example.cycle.api;

import org.example.chatbot.service.AiService;
import org.example.cycle.model.Cycle;
import org.example.cycle.model.Symptome;
import org.example.cycle.service.CycleAnalysisService;
import org.example.cycle.service.CycleService;
import org.example.cycle.service.SymptomeService;
import org.example.user.model.User;
import org.example.utils.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cycle")
public class CycleAdviceController {

    @Autowired
    private AiService aiService;

    private final CycleService cycleService = new CycleService();
    private final CycleAnalysisService analysisService = new CycleAnalysisService();
    private final SymptomeService symptomeService = new SymptomeService();

    @GetMapping("/advice")
    public ResponseEntity<String> getCycleAdvice() {
        try {
            User currentUser = SessionManager.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body("{\"error\": \"Utilisateur non connecté\"}");
            }

            // Récupérer les cycles de l'utilisateur
            List<Cycle> cycles = cycleService.getCyclesByUserId(currentUser.getId());
            if (cycles == null || cycles.isEmpty()) {
                return ResponseEntity.ok("{\"phase\": \"Inconnue\", \"jourCycle\": 0, \"advice\": {\"resume\": \"Veuillez enregistrer un cycle pour recevoir des conseils.\", \"alimentation\": \"N/A\", \"sport\": \"N/A\", \"bienEtre\": \"N/A\"}}");
            }

            // Trier par date
            cycles.sort(Comparator.comparing(Cycle::getDate_debut_m));
            Cycle currentCycle = cycles.get(cycles.size() - 1);
            LocalDate debutRegles = currentCycle.getDate_debut_m().toLocalDate();
            LocalDate today = LocalDate.now();

            int avgCycle = analysisService.getAverageCycleLength(cycles);
            int avgMenstruation = analysisService.getAverageMenstruationLength(cycles);

            // Calculer la phase
            String phase = analysisService.determinePhase(debutRegles, today, avgCycle, avgMenstruation);
            int jourCycle = (int) ChronoUnit.DAYS.between(debutRegles, today) + 1;

            // Récupérer les symptômes récents (aujourd'hui)
            List<Symptome> symptomes = symptomeService.getSymptomesByCycleId(currentCycle.getCycle_id());
            String symptomesList = symptomes.stream()
                    .filter(s -> s.getDateObservation().toLocalDate().isEqual(today))
                    .map(s -> s.getType().name() + " (" + s.getIntensite().name() + ")")
                    .collect(Collectors.joining(", "));

            if (symptomesList.isEmpty()) {
                symptomesList = "Aucun symptôme signalé aujourd'hui.";
            }

            // Appeler l'IA
            String aiAdviceJson = aiService.getCycleAdvice(phase, jourCycle, symptomesList, "Historique: " + cycles.size() + " cycles enregistrés.");

            return ResponseEntity.ok(aiAdviceJson);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\": \"Erreur interne du serveur\"}");
        }
    }
}

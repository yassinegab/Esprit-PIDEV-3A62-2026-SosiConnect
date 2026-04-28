package org.example.cycle.service;

import org.example.cycle.model.Cycle;
import org.example.cycle.model.CycleAnalysis;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CycleAnalysisService {

    public static final int DEFAULT_CYCLE_LENGTH = 28;
    public static final int DEFAULT_MENSTRUATION_LENGTH = 5;

    public List<CycleAnalysis> analyzeAllUserCycles(List<Cycle> userCycles) {
        if (userCycles == null || userCycles.isEmpty()) {
            return new ArrayList<>();
        }

        userCycles.sort(Comparator.comparing(Cycle::getDate_debut_m));
        List<CycleAnalysis> analyses = new ArrayList<>();
        
        int avgCycleLength = getAverageCycleLength(userCycles);

        for (int i = 0; i < userCycles.size(); i++) {
            Cycle c = userCycles.get(i);
            CycleAnalysis analysis = new CycleAnalysis(c);
            
            // 1. Mensuration Duration
            long mensDuration = ChronoUnit.DAYS.between(c.getDate_debut_m().toLocalDate(), c.getDate_fin_m().toLocalDate()) + 1; // Un jour de saignement = 1
            analysis.setMenstruationDuration(mensDuration);
            
            // 2. Cycle Duration
            LocalDate nextPeriodStart;
            long cycleDuration = -1;
            
            if (i < userCycles.size() - 1) {
                // Ancien cycle
                Cycle nextCycle = userCycles.get(i + 1);
                cycleDuration = ChronoUnit.DAYS.between(c.getDate_debut_m().toLocalDate(), nextCycle.getDate_debut_m().toLocalDate());
                analysis.setCycleDuration(cycleDuration);
                analysis.setIrregular(cycleDuration < 21 || cycleDuration > 35);
                analysis.setStatus("Passé");
                nextPeriodStart = nextCycle.getDate_debut_m().toLocalDate();
            } else {
                // Actuel
                analysis.setCycleDuration(-1);
                analysis.setIrregular(false);
                analysis.setStatus("Actuel");
                nextPeriodStart = c.getDate_debut_m().toLocalDate().plusDays(avgCycleLength);
            }
            
            // 3. Ovulation & Fertile window (14 days backwards from next period)
            LocalDate ovulation = nextPeriodStart.minusDays(14);
            analysis.setOvulationDate(ovulation);
            analysis.setFertileWindowStart(ovulation.minusDays(5));
            analysis.setFertileWindowEnd(ovulation.plusDays(1));
            
            analyses.add(analysis);
        }
        
        return analyses;
    }

    public int getAverageCycleLength(List<Cycle> cycles) {
        if (cycles.size() < 2) return DEFAULT_CYCLE_LENGTH;
        
        cycles.sort(Comparator.comparing(Cycle::getDate_debut_m));
        long totalDays = 0;
        int count = 0;
        
        for (int i = 0; i < cycles.size() - 1; i++) {
            long duration = ChronoUnit.DAYS.between(
                cycles.get(i).getDate_debut_m().toLocalDate(), 
                cycles.get(i+1).getDate_debut_m().toLocalDate()
            );
            totalDays += duration;
            count++;
        }
        
        return count > 0 ? (int) (totalDays / count) : DEFAULT_CYCLE_LENGTH;
    }

    public int getAverageMenstruationLength(List<Cycle> cycles) {
        if (cycles.isEmpty()) return DEFAULT_MENSTRUATION_LENGTH;
        
        long totalDays = 0;
        for (Cycle c : cycles) {
            totalDays += ChronoUnit.DAYS.between(c.getDate_debut_m().toLocalDate(), c.getDate_fin_m().toLocalDate()) + 1;
        }
        return (int) (totalDays / cycles.size());
    }

    public LocalDate predictNextPeriod(List<Cycle> userCycles) {
        if (userCycles == null || userCycles.isEmpty()) return null;
        userCycles.sort(Comparator.comparing(Cycle::getDate_debut_m));
        Cycle last = userCycles.get(userCycles.size() - 1);
        int avgDuration = getAverageCycleLength(userCycles);
        return last.getDate_debut_m().toLocalDate().plusDays(avgDuration);
    }
    
    public double getRegularityRate(List<Cycle> userCycles) {
        if (userCycles.size() < 2) return 100.0;
        
        userCycles.sort(Comparator.comparing(Cycle::getDate_debut_m));
        int irregularCount = 0;
        int totalCalculable = userCycles.size() - 1;
        
        for (int i = 0; i < totalCalculable; i++) {
            long duration = ChronoUnit.DAYS.between(
                userCycles.get(i).getDate_debut_m().toLocalDate(), 
                userCycles.get(i+1).getDate_debut_m().toLocalDate()
            );
            if (duration < 21 || duration > 35) irregularCount++;
        }
        
        return 100.0 - (((double) irregularCount / totalCalculable) * 100.0);
    }

    public Cycle getCycleForDate(LocalDate date, List<Cycle> cycles) {
        for (Cycle c : cycles) {
            LocalDate start = c.getDate_debut_m().toLocalDate();
            LocalDate end = c.getDate_fin_m().toLocalDate();
            if ((date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end))) {
                return c;
            }
        }
        return null;
    }

    public String getDayState(LocalDate date, List<CycleAnalysis> analyses) {
        for (CycleAnalysis a : analyses) {
            Cycle c = a.getCycle();
            LocalDate start = c.getDate_debut_m().toLocalDate();
            LocalDate end = c.getDate_fin_m().toLocalDate();

            // 1. Solid Date Comparison for Menstruation
            if ((date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end))) {
                return "MENSTRUATION";
            }
            
            // 2. Ovulation Comparison
            if (a.getOvulationDate() != null && date.isEqual(a.getOvulationDate())) {
                return "OVULATION";
            }
            
            // 3. Fertile Window Comparison
            if (a.getFertileWindowStart() != null && a.getFertileWindowEnd() != null) {
                if ((date.isEqual(a.getFertileWindowStart()) || date.isAfter(a.getFertileWindowStart())) && 
                    (date.isEqual(a.getFertileWindowEnd()) || date.isBefore(a.getFertileWindowEnd()))) {
                    return "FERTILE";
                }
            }
        }
        return "NORMAL";
    }

    public String determinePhase(LocalDate dateDebutRegles, LocalDate currentDate, int dureeCycle, int dureeRegles) {
        if (dateDebutRegles == null || currentDate == null) {
            return "Inconnue";
        }

        long jourCycle = ChronoUnit.DAYS.between(dateDebutRegles, currentDate) + 1; // Jour 1 = premier jour des règles

        if (jourCycle <= 0 || jourCycle > dureeCycle + 10) { // Tolérance pour les retards
            return "Inconnue";
        }

        // 1. Menstruation
        if (jourCycle <= dureeRegles) {
            return "Menstruation";
        }

        // Calculs basés sur l'ovulation (en moyenne 14 jours avant la fin du cycle)
        long ovulationDay = Math.max(1, dureeCycle - 14);
        long fertileStart = Math.max(1, ovulationDay - 5);
        long fertileEnd = ovulationDay + 1;

        // 2. Ovulation (le jour même ou la veille)
        if (jourCycle == ovulationDay) {
            return "Ovulation";
        }

        // 3. Période fertile
        if (jourCycle >= fertileStart && jourCycle <= fertileEnd) {
            return "Fertile";
        }

        // 4. Folliculaire (entre les règles et la période fertile)
        if (jourCycle > dureeRegles && jourCycle < fertileStart) {
            return "Folliculaire";
        }

        // 5. Lutéale (après l'ovulation jusqu'à la fin)
        if (jourCycle > fertileEnd) {
            return "Lutéale";
        }

        return "Inconnue";
    }
}

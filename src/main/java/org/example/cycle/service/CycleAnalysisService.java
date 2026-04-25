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
}

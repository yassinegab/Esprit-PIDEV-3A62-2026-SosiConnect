package org.example.cycle.model;

import java.time.LocalDate;

public class CycleAnalysis {
    private Cycle cycle;
    private long menstruationDuration; // jours de règles
    private long cycleDuration; // durée totale du cycle (-1 si c'est le cycle actuel non terminé)
    private LocalDate ovulationDate; // Date estimée de l'ovulation (même rétroactive)
    private LocalDate fertileWindowStart;
    private LocalDate fertileWindowEnd;
    private boolean isIrregular;
    private String status; // Passé, Actuel, Prévisionnel

    public CycleAnalysis(Cycle cycle) {
        this.cycle = cycle;
    }

    public Cycle getCycle() { return cycle; }
    
    public long getMenstruationDuration() { return menstruationDuration; }
    public void setMenstruationDuration(long menstruationDuration) { this.menstruationDuration = menstruationDuration; }
    
    public long getCycleDuration() { return cycleDuration; }
    public void setCycleDuration(long cycleDuration) { this.cycleDuration = cycleDuration; }
    
    public LocalDate getOvulationDate() { return ovulationDate; }
    public void setOvulationDate(LocalDate ovulationDate) { this.ovulationDate = ovulationDate; }
    
    public LocalDate getFertileWindowStart() { return fertileWindowStart; }
    public void setFertileWindowStart(LocalDate fertileWindowStart) { this.fertileWindowStart = fertileWindowStart; }
    
    public LocalDate getFertileWindowEnd() { return fertileWindowEnd; }
    public void setFertileWindowEnd(LocalDate fertileWindowEnd) { this.fertileWindowEnd = fertileWindowEnd; }
    
    public boolean isIrregular() { return isIrregular; }
    public void setIrregular(boolean irregular) { this.isIrregular = irregular; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

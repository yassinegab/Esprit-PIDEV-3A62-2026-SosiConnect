package org.example.wellbeing.model;

import org.example.user.model.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserWellBeingData {
    private int id;
    private int workEnvironment;
    private int sleepProblems;
    private int headaches;
    private int restlessness;
    private int heartbeatPalpitations;
    private int lowAcademicConfidence;
    private int classAttendance;
    private int anxietyTension;
    private int irritability;
    private int subjectConfidence;
    private StressPrediction stressPrediction;
    private LocalDateTime createdAt;
    private List<StressPrediction> stressPredictions;
    private User user;

    public UserWellBeingData() {
        this.createdAt = LocalDateTime.now();
        this.stressPredictions = new ArrayList<>();
    }

    public UserWellBeingData(int workEnvironment, int sleepProblems, int headaches, int restlessness, int heartbeatPalpitations, int lowAcademicConfidence, int classAttendance, int anxietyTension, int irritability, int subjectConfidence, User user) {
        this.workEnvironment = workEnvironment;
        this.sleepProblems = sleepProblems;
        this.headaches = headaches;
        this.restlessness = restlessness;
        this.heartbeatPalpitations = heartbeatPalpitations;
        this.lowAcademicConfidence = lowAcademicConfidence;
        this.classAttendance = classAttendance;
        this.anxietyTension = anxietyTension;
        this.irritability = irritability;
        this.subjectConfidence = subjectConfidence;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.stressPredictions = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getWorkEnvironment() { return workEnvironment; }
    public void setWorkEnvironment(int workEnvironment) { this.workEnvironment = workEnvironment; }

    public int getSleepProblems() { return sleepProblems; }
    public void setSleepProblems(int sleepProblems) { this.sleepProblems = sleepProblems; }

    public int getHeadaches() { return headaches; }
    public void setHeadaches(int headaches) { this.headaches = headaches; }

    public int getRestlessness() { return restlessness; }
    public void setRestlessness(int restlessness) { this.restlessness = restlessness; }

    public int getHeartbeatPalpitations() { return heartbeatPalpitations; }
    public void setHeartbeatPalpitations(int heartbeatPalpitations) { this.heartbeatPalpitations = heartbeatPalpitations; }

    public int getLowAcademicConfidence() { return lowAcademicConfidence; }
    public void setLowAcademicConfidence(int lowAcademicConfidence) { this.lowAcademicConfidence = lowAcademicConfidence; }

    public int getClassAttendance() { return classAttendance; }
    public void setClassAttendance(int classAttendance) { this.classAttendance = classAttendance; }

    public int getAnxietyTension() { return anxietyTension; }
    public void setAnxietyTension(int anxietyTension) { this.anxietyTension = anxietyTension; }

    public int getIrritability() { return irritability; }
    public void setIrritability(int irritability) { this.irritability = irritability; }

    public int getSubjectConfidence() { return subjectConfidence; }
    public void setSubjectConfidence(int subjectConfidence) { this.subjectConfidence = subjectConfidence; }

    public double getStressScore() {
        if (stressPrediction != null) {
            return stressPrediction.getConfidenceScore() / 100.0;
        }
        // Fallback to simple average if no prediction exists
        double total = workEnvironment + sleepProblems + headaches + restlessness + 
                      heartbeatPalpitations + lowAcademicConfidence + classAttendance + 
                      anxietyTension + irritability + subjectConfidence;
        return (total - 10) / 40.0; // Normalized 10-50 range to 0.0-1.0
    }

    public StressPrediction getStressPrediction() { return stressPrediction; }
    public void setStressPrediction(StressPrediction stressPrediction) { this.stressPrediction = stressPrediction; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<StressPrediction> getStressPredictions() { return stressPredictions; }
    public void setStressPredictions(List<StressPrediction> stressPredictions) { this.stressPredictions = stressPredictions; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

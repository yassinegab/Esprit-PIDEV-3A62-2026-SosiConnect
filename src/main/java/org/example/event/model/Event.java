package org.example.event.model;

import java.time.LocalDate;

public class Event {
    private int id;
    private String title;
    private String description;
    private LocalDate date;
    private String type;
    private String localisation;

    public Event() {
    }

    public Event(int id, String title, String description, LocalDate date, String type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.type = type;
        this.localisation = null;
    }

    public Event(int id, String title, String description, LocalDate date, String type, String localisation) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.type = type;
        this.localisation = localisation;
    }

    public Event(String title, String description, LocalDate date, String type) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.type = type;
        this.localisation = null;
    }

    public Event(String title, String description, LocalDate date, String type, String localisation) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.type = type;
        this.localisation = localisation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", localisation='" + localisation + '\'' +
                '}';
    }
}

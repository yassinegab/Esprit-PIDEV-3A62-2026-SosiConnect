package org.example.cycle.model;

import java.sql.Date;

public class Cycle {
    private int id_cycle;
    private java.sql.Date date_debut_m;
    private java.sql.Date date_fin_m;
    private int user_id;

    public Cycle() {}

    public Cycle(int id_cycle, Date date_debut_m, Date date_fin_m, int user_id) {
        this.id_cycle = id_cycle;
        this.date_debut_m = date_debut_m;
        this.date_fin_m = date_fin_m;
        this.user_id = user_id;
    }

    public Cycle(Date  date_debut_m, Date date_fin_m, int user_id) {
        this.date_debut_m = date_debut_m;
        this.date_fin_m = date_fin_m;
        this.user_id = user_id;
    }

    public int getCycle_id() {
        return id_cycle;
    }

    public void setCycle_id(int id_cycle) {
        this.id_cycle = id_cycle;
    }

    public Date getDate_debut_m() {
        return date_debut_m;
    }

    public void setDate_debut_m(Date date_debut_m) {
        this.date_debut_m = date_debut_m;
    }

    public Date getDate_fin_m() {
        return date_fin_m;
    }

    public void setDate_fin_m(Date date_fin_m) {
        this.date_fin_m = date_fin_m;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}

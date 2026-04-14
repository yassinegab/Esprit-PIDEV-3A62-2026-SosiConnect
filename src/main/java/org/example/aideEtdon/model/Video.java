package org.example.aideEtdon.model;

public class Video {
    private int id;
    private String title;
    private String youtubeUrl;

    public Video() {}

    public Video(int id, String title, String youtubeUrl) {
        this.id = id;
        this.title = title;
        this.youtubeUrl = youtubeUrl;
    }

    public Video(String title, String youtubeUrl) {
        this.title = title;
        this.youtubeUrl = youtubeUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}

package com.omworldgame.guardianjourney;

public class Notice {
    private String id;
    private String title;
    private String contents;
    private String createdAt;

    public Notice(String id, String title, String contents, String createdAt) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public String getContents() {
        return contents;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

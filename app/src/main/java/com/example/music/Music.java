package com.example.music;
public class Music {
    private int id;
    private String title;
    private String artist;
    private long duration;
    private String url;

    public void setId(int id){
        this.id = id;
    }
    public long getId(){return this.id;}
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){return this.title;}
    public void setArtist(String artist){
        this.artist = artist;
    }
    public String getArtist(){return this.artist;}
    public void setDuration(long duration){this.duration = duration;}
    public long getDuration(){return this.duration;}
    public void setUrl(String url){this.url = url;}
    public String getUrl(){return this.url;}





}

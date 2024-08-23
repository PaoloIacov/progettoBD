package model.domain;
import java.time.LocalDateTime;


public class Message {
    private Long ID;
    private String content;
    private LocalDateTime dateTime;
    private String usernameDipendente;
    private Long answersTo;

    //Creazione di nuovi messaggi, sicuramente non rispondono a nulla
    public Message(String content, LocalDateTime dateTime, String usernameDipendente) {
        this.content = content;
        this.dateTime = dateTime;
        this.usernameDipendente = usernameDipendente;
        this.answersTo = answersTo;

    }

    //Recupero di messaggi dal database
    public Message(Long ID, String content, LocalDateTime dateTime, String usernameDipendente, Long answersTo) {
        this.ID = ID;
        this.content = content;
        this.dateTime = dateTime;
        this.usernameDipendente = usernameDipendente;
        this.answersTo = answersTo;

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}


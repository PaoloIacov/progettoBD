package model.domain;
import java.time.LocalDateTime;


public class Message {
    private Long ID;
    private String content;
    private LocalDateTime dateTime;
    private String usernameDipendente;
    private Long answersTo;
    private Long conversationID;

    //Creazione di nuovi messaggi, sicuramente non rispondono a nulla
    public Message(String content, LocalDateTime dateTime, String usernameDipendente, Long conversationID) {
        this.content = content;
        this.dateTime = dateTime;
        this.usernameDipendente = usernameDipendente;
        this.answersTo = answersTo;
        this.conversationID = conversationID;

    }

    //Recupero di messaggi dal database
    public Message(Long ID, String content, LocalDateTime dateTime, String usernameDipendente, Long answersTo, Long conversationID) {
        this.ID = ID;
        this.content = content;
        this.dateTime = dateTime;
        this.usernameDipendente = usernameDipendente;
        this.answersTo = answersTo;
        this.conversationID = conversationID;

    }

    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
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

    public String getUsernameDipendente() {
        return usernameDipendente;
    }

    public void setUsernameDipendente(String usernameDipendente) {
        this.usernameDipendente = usernameDipendente;
    }

    public Long getAnswersTo() {
        return answersTo;
    }

    public void setAnswersTo(Long answersTo) {
        this.answersTo = answersTo;
    }

    public Long getConversationID() {
        return conversationID;
    }

    public void setConversationID(Long conversationID) {
        this.conversationID = conversationID;
    }


}


package model.domain;

import model.domain.enums.ConversationState;
import model.domain.enums.ConversationType;

public class Conversation {
    private Long ID;
    private String description;
    private String projectName;
    private ConversationState state;
    private ConversationType type;

    //Costruttore utile per la creazione di una nuova conversazione
    public Conversation(String description, String projectName, ConversationState state, ConversationType type) {
        this.description = description;
        this.projectName = projectName;
        this.state = state;
        this.type = type;
    }

    //Costruttore utile per un recupero da database
    public Conversation(Long ID, String description, String projectName, ConversationState state, ConversationType type) {
        this.ID = ID;
        this.description = description;
        this.projectName = projectName;
        this.state = state;
        this.type = type;
    }

    public Long getID() {
        return ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String Description) {
        this.description = Description;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
    }

    public ConversationType getType() {
        return type;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }
}

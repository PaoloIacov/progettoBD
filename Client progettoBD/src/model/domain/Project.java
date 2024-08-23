package model.domain;

import model.domain.enums.ProjectState;

public class Project {
    private String projectName;
    private String description;
    private ProjectState state;

    public Project(String projectName, String description, ProjectState state) {
        this.projectName = projectName;
        this.description = description;
        this.state = state;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProjectState getState() {
        return state;
    }

    public void setState(ProjectState state) {
        this.state = state;
    }
}

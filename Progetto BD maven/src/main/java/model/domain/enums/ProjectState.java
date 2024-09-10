package model.domain.enums;

public enum ProjectState {
    IN_CORSO(1),
    CHIUSO(2);

    private final int id;

    ProjectState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ProjectState fromInt(int id) {
        for (ProjectState state : ProjectState.values()) {
            if (state.getId() == id) {
                return state;
            }
        }
        throw new IllegalArgumentException("ID del ProjectState non valido: " + id);
    }
}

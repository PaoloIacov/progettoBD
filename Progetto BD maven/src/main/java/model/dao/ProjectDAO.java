package model.dao;

import exception.DAOException;
import model.domain.Project;
import model.domain.enums.ProjectState;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO implements GeneralDAO<Object> {

    @Override
    public Object execute(Object... params) throws DAOException {
        String operation = (String) params[0];

        switch (operation) {
            case "addProject":
                return addProject((String) params[1], (String) params[2]);
            case "getProjectsByEmployee":
                return getProjectsByEmployee((String) params[1]);
            case "addEmployeeToProject":
                addEmployeeToProject((String) params[1], (String) params[2]);
                return null;
            case "removeEmployeeFromProject":
                removeEmployeeFromProject((String) params[1], (String) params[2]);
                return null;
            case "closeProject":
                closeProject((String) params[1]);
                return null;
            case "getAssignedProjectByManager":
                return getAssignedProjectByManager((String) params[1]);
            case "getEmployeesInProject":
                return getEmployeesInProject((String) params[1]);
            default:
                throw new DAOException("Operazione non supportata: " + operation);
        }
    }

    public Project addProject(String nome, String descrizione) throws DAOException {
        try {
            Connection connection = ConnectionFactory.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call AddProject(?, ?)}");

            callableStatement.setString(1, nome);
            callableStatement.setString(2, descrizione);
            callableStatement.execute();

            System.out.println("Progetto aggiunto con nome: " + nome);
        } catch (SQLException e) {
            throw new DAOException("Errore nell'aggiunta del nuovo progetto: " + e.getMessage(), e);
        }

        return new Project(nome, descrizione, ProjectState.IN_CORSO);
    }

    public void closeProject(String nomeProgetto) throws DAOException {
        Connection connection = null;
        CallableStatement callableStatement = null;

        try {
            connection = ConnectionFactory.getConnection();
            connection.setAutoCommit(false);

            if (!doesProjectExist(nomeProgetto, connection)) {
                throw new DAOException("Errore: Il progetto '" + nomeProgetto + "' non esiste.");
            }

            callableStatement = connection.prepareCall("{call CloseProject(?)}");
            callableStatement.setString(1, nomeProgetto);
            callableStatement.execute();

            // Commit della transazione
            connection.commit();

        } catch (SQLException e) {
            // Esegui rollback in caso di errore
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                throw new DAOException("Errore durante il rollback della transazione: " + rollbackEx.getMessage(), rollbackEx);
            }
            throw new DAOException("Errore nella chiusura del progetto: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (callableStatement != null) {
                try {
                    callableStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Metodo per verificare se il progetto esiste nel database
    private boolean doesProjectExist(String nomeProgetto, Connection connection) throws DAOException {
        String query = "SELECT COUNT(*) FROM Progetto WHERE nome = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, nomeProgetto);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0; // Ritorna true se esiste almeno una riga
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Errore durante la verifica dell'esistenza del progetto: " + e.getMessage(), e);
        }

        return false;
    }


    private void addEmployeeToProject(String usernameDipendente, String nomeProgetto) throws DAOException {
        try {
            Connection connection = ConnectionFactory.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call AddEmployeeToProject(?, ?)}");

            callableStatement.setString(1, usernameDipendente);
            callableStatement.setString(2, nomeProgetto);
            callableStatement.execute();
        } catch (SQLException e) {
            throw new DAOException("Errore durante l'aggiunta del dipendente al progetto");
        }
    }

    private void removeEmployeeFromProject(String nomeProgetto, String usernameDipendente) throws DAOException {
        try {
            Connection connection = ConnectionFactory.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call RemoveEmployeeFromProject(?, ?)}");

            callableStatement.setString(1, nomeProgetto);
            callableStatement.setString(2, usernameDipendente);
            callableStatement.execute();
        } catch (SQLException e) {
            throw new DAOException("Errore nella rimozione del lavoratore dal progetto: " + e.getMessage(), e);
        }
    }

    public List<Project> getProjectsByEmployee(String usernameDipendente) throws DAOException {
        List<Project> projects = new ArrayList<>();
        try {
            Connection connection = ConnectionFactory.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call GetProjectsByEmployee(?)}");
            callableStatement.setString(1, usernameDipendente);

            ResultSet resultSet = callableStatement.executeQuery();

            while (resultSet.next()) {
                String nome = resultSet.getString("nome");
                String descrizione = resultSet.getString("descrizione");
                int statoId = resultSet.getInt("stato");
                ProjectState stato = ProjectState.fromInt(statoId);

                projects.add(new Project(nome, descrizione, stato));
            }
        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero dei progetti del dipendente: " + e.getMessage(), e);
        }

        return projects;
    }

    private Project getAssignedProjectByManager(String projectManagerUsername) throws DAOException {
        try {
            Connection connection = ConnectionFactory.getConnection();
            CallableStatement callableStatement = connection.prepareCall("{call GetAssignedProjectByManager(?)}");
            callableStatement.setString(1, projectManagerUsername);

            ResultSet resultSet = callableStatement.executeQuery();

            if (resultSet.next()) {
                String nome = resultSet.getString("nome");
                String descrizione = resultSet.getString("descrizione");
                int statoId = resultSet.getInt("stato");
                ProjectState stato = ProjectState.fromInt(statoId);

                return new Project(nome, descrizione, stato);
            } else {
                System.out.println("Nessun progetto trovato per il project manager: " + projectManagerUsername);
                return null; // Nessun progetto trovato
            }
        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero del progetto assegnato: " + e.getMessage(), e);
        }
    }

    public List<String> getEmployeesInProject(String nomeProgetto) throws DAOException {
        List<String> employees = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection()) {
            CallableStatement callableStatement = connection.prepareCall("{call GetEmployeesInProject(?)}");
            callableStatement.setString(1, nomeProgetto);

            ResultSet resultSet = callableStatement.executeQuery();

            while (resultSet.next()) {
                String username = resultSet.getString("usernameDipendente");
                employees.add(username);
            }
        } catch (SQLException e) {
            throw new DAOException("Errore nel recupero dei dipendenti del progetto: " + e.getMessage(), e);
        }

        return employees;
    }

    public List<Project> getAllProjects() throws DAOException {
        List<Project> projects = new ArrayList<>();
        try {
            Connection connection = ConnectionFactory.getConnection();
            try (CallableStatement callableStatement = connection.prepareCall("{call ShowAllProjects()}")) {
                try (ResultSet resultSet = callableStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String nome = resultSet.getString("nome");
                        String descrizione = resultSet.getString("descrizione");
                        int statoId = resultSet.getInt("stato");
                        ProjectState stato = ProjectState.fromInt(statoId);

                        Project project = new Project(nome, descrizione, stato);
                        projects.add(project);
                    }
                }
            } catch (SQLException e) {
                throw new DAOException("Errore durante il recupero dei progetti: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new DAOException("Errore durante il recupero dei progetti: " + e.getMessage(), e);
        }

        return projects;
    }


}

package model.dao;

import model.domain.Project;
import model.domain.enums.ProjectState;
import exception.DAOException;

import java.sql.*;

public class ProjectDAO implements GeneralDAO {

    //Metodo per creare un nuovo progetto
    public Project addProject(String nome, String descrizione) throws DAOException {


        try {

           Connection connection = ConnectionSingleton.getConnection();

           CallableStatement callableStatement = connection.prepareCall("{call AddProject(?, ?)}");

            callableStatement.setString(1, nome);
            callableStatement.setString(2, descrizione);
            callableStatement.execute();

        } catch (SQLException e) {
            throw new DAOException("Errore nell'aggiunta del nuovo progetto: " + e.getMessage(), e);
        }

        //stato di default: IN_CORSO
        return new Project(nome, descrizione, ProjectState.IN_CORSO);
    }

    //Metodo per aggiungere un dipendente ad un progetto
    @Override
    public Void execute(Object... params) throws DAOException {
    String usernameDipendente = (String) params[0];
    String nomeProgetto = (String) params[1];

        try {

        Connection connection = ConnectionSingleton.getConnection();

        CallableStatement callableStatement = connection.prepareCall("{call AddWorkerToProject(?, ?)}");

        callableStatement.setString(1, usernameDipendente);
        callableStatement.setString(2, nomeProgetto);

        callableStatement.execute();

         } catch (SQLException e) {
            throw new DAOException("Errore nell'aggiunta del lavoratore al progetto: " + e.getMessage(), e);
         }

        return null;
    }

    //Metodo per chiudere un progetto, droppa anche i dipendenti e le conversazioni associate (utilizzo una transazione per garantire l'integrità dei dati)
    public void closeProject(String nomeProgetto) throws DAOException {
        Connection connection = null;
        CallableStatement callableStatement = null;

        try {

            connection = ConnectionSingleton.getConnection();

            // Disabilitare l'auto-commit per iniziare la transazione
            connection.setAutoCommit(false);

            callableStatement = connection.prepareCall("{call CloseProject(?)}");

            callableStatement.setString(1, nomeProgetto);

            callableStatement.execute();

            // Commettere la transazione se tutto va bene
            connection.commit();

        } catch (SQLException e) {
            try {
                if (connection != null) {
                    // Eseguire il rollback in caso di errore
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                throw new DAOException("Errore durante il rollback della transazione: " + rollbackEx.getMessage(), rollbackEx);
            }
            throw new DAOException("Errore nella chiusura del progetto: " + e.getMessage(), e);
        } finally {
            // Ripristinare l'auto-commit e chiudere il CallableStatement e la connessione
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

    //Metodo per rimuovere un dipendente da un progetto
    public void removeWorkerFromProject(String nomeProgetto, String usernameDipendente) throws DAOException {

        try {

            Connection connection = ConnectionSingleton.getConnection();

            CallableStatement callableStatement = connection.prepareCall("{call RemoveWorkerFromProject(?, ?)}");

            callableStatement.setString(1, nomeProgetto);
            callableStatement.setString(2, usernameDipendente);

            callableStatement.execute();

        } catch (SQLException e) {
            throw new DAOException("Errore nella rimozione del lavoratore dal progetto: " + e.getMessage(), e);
        }
    }
}

package scheduler.model;

import scheduler.db.ConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Appointment {
    String ID;
    String Patient_Name;
    String Caregiver_Name;
    String Vaccines_Name;
    Date Time;

    public Appointment() {}

    public Appointment(String ID, String patient_Name, String caregiver_Name, String vaccines_Name, Date time) {
        this.ID = ID;
        Patient_Name = patient_Name;
        Caregiver_Name = caregiver_Name;
        Vaccines_Name = vaccines_Name;
        Time = time;
    }

    public void saveToDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addAppointment = "INSERT INTO Appointment VALUES (?, ?, ? ,? ,?)";
        try {
            PreparedStatement statement = con.prepareStatement(addAppointment);
            statement.setString(1, this.ID);
            statement.setString(2, this.Patient_Name);
            statement.setString(3, this.Caregiver_Name);
            statement.setString(4, this.Vaccines_Name);
            statement.setDate(5, this.Time);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }
}

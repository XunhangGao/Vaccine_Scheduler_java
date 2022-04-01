package scheduler.model;

import scheduler.db.ConnectionManager;
import scheduler.util.Util;

import java.sql.*;
import java.util.Arrays;

public class Caregiver {
    private final String username;
    private final byte[] salt;
    private final byte[] hash;

    private Caregiver(CaregiverBuilder builder) {
        this.username = builder.username;
        this.salt = builder.salt;
        this.hash = builder.hash;
    }

    private Caregiver(CaregiverGetter getter) {
        this.username = getter.username;
        this.salt = getter.salt;
        this.hash = getter.hash;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getHash() {
        return hash;
    }

    public void saveToDB() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addCaregiver = "INSERT INTO Caregivers VALUES (? , ?, ?)";
        try {
            PreparedStatement statement = con.prepareStatement(addCaregiver);
            statement.setString(1, this.username);
            statement.setBytes(2, this.salt);
            statement.setBytes(3, this.hash);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public void uploadAvailability(Date date) throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addAvailability = "INSERT INTO Availabilities VALUES (? , ? , ?);";
        try {
            if(!uploaded(date)){
                System.out.println("One caregiver can only upload once a day!");
                return;
            }
            PreparedStatement statement = con.prepareStatement(addAvailability);
            statement.setDate(1, date);
            statement.setString(2, this.username);
            statement.setInt(3, 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public boolean uploaded(Date date) throws SQLException{
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String checkAvailability = "SELECT Username FROM Availabilities WHERE Username = ? and Time = ?;";
        try {
            PreparedStatement statement = con.prepareStatement(checkAvailability);
            statement.setString(1, this.username);
            statement.setDate(2, date);
            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.wasNull()){
                return false;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public void showAppointment() throws SQLException {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String searchAvailability = "SELECT ID, Patient_Name, Caregiver_Name, Vaccine_Name,Time From Appointment WHERE Caregiver_Name = ?";
        try {
            PreparedStatement statement = con.prepareStatement(searchAvailability);
            statement.setString(1,this.username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.wasNull()){
                System.out.println("No appointment");
                return;
            } else {
                System.out.println("The appointment:");
                while (resultSet.next()) {
                    String ID = resultSet.getString("ID");
                    String patientName = resultSet.getString("Patient_Name");
                    String caregiverName = resultSet.getString("Caregiver_Name");
                    String vaccineName = resultSet.getString("Vaccine_Name");
                    Date time = resultSet.getDate("Time");
                    System.out.println("(appointmentID: " + ID + " vaccine: " + vaccineName + " Date: " + time + " PatientName: " + patientName + ")");
                }
            }
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static class CaregiverBuilder {
        private final String username;
        private final byte[] salt;
        private final byte[] hash;

        public CaregiverBuilder(String username, byte[] salt, byte[] hash) {
            this.username = username;
            this.salt = salt;
            this.hash = hash;
        }

        public Caregiver build() {
            return new Caregiver(this);
        }
    }

    public static class CaregiverGetter {
        private final String username;
        private final String password;
        private byte[] salt;
        private byte[] hash;

        public CaregiverGetter(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public Caregiver get() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String getCaregiver = "SELECT Salt, Hash FROM Caregivers WHERE Username = ?";
            try {
                PreparedStatement statement = con.prepareStatement(getCaregiver);
                statement.setString(1, this.username);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    byte[] salt = resultSet.getBytes("Salt");
                    // we need to call Util.trim() to get rid of the paddings,
                    // try to remove the use of Util.trim() and you'll see :)
                    byte[] hash = Util.trim(resultSet.getBytes("Hash"));
                    // check if the password matches
                    byte[] calculatedHash = Util.generateHash(password, salt);
                    if (!Arrays.equals(hash, calculatedHash)) {
                        return null;
                    } else {
                        this.salt = salt;
                        this.hash = hash;
                        return new Caregiver(this);
                    }
                }
                return null;
            } catch (SQLException e) {
                throw new SQLException();
            } finally {
                cm.closeConnection();
            }
        }
    }
}

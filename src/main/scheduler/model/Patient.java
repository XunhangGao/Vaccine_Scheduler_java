package scheduler.model;

import scheduler.db.ConnectionManager;
import scheduler.util.Util;

import java.sql.*;
import java.util.Arrays;

public class Patient {
    private final String username;
    private final byte[] salt;
    private final byte[] hash;

    private Patient(Patient.PatientBuilder builder) {
        this.username = builder.username;
        this.salt = builder.salt;
        this.hash = builder.hash;
    }

    private Patient(Patient.PatientGetter getter) {
        this.username = getter.username;
        this.salt = getter.salt;
        this.hash = getter.hash;
    }

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

        String addCaregiver = "INSERT INTO Patients VALUES (? , ?, ?)";
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

    public void reserve(Date date, String caregiverName, Vaccine vaccine) throws SQLException{
        if(!reserved(date)){
            System.out.println("One patient can only reserve once a day.");
            return;
        }
        Availability.uploadAvailability(date,caregiverName);
        vaccine.decreaseAvailableDoses(1);
        String ID = createID(this.username, date, caregiverName, vaccine.getVaccineName());
        Appointment appointment = new Appointment(ID,this.username,caregiverName,vaccine.getVaccineName(),date);
        appointment.saveToDB();
        System.out.println("Reservation successful");
        System.out.println("CaregiverName: "+ caregiverName);
        System.out.println("AppointmentID: "+ ID);
    }

    public String createID(String patientName, Date date, String caregiverName, String vaccineName){
        return patientName + date + caregiverName + vaccineName;
    }

    public boolean reserved(Date date) throws SQLException{
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String checkAvailability = "SELECT Patient_Name FROM Appointment WHERE Patient_Name = ? and Time = ?";
        try {
            PreparedStatement statement = con.prepareStatement(checkAvailability);
            statement.setString(1, this.username);
            statement.setDate(2, date);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
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
        String searchAvailability = "SELECT ID, Caregiver_Name, Vaccine_Name,Time From Appointment WHERE Patient_Name = ?";
        try {
            PreparedStatement statement = con.prepareStatement(searchAvailability);
            statement.setString(1,this.username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.wasNull()){
                System.out.println("No appointment");
                return;
            }
            System.out.println("The appointment:");
            while (resultSet.next()) {
                String ID = resultSet.getString("ID");
                String caregiverName = resultSet.getString("Caregiver_Name");
                String vaccineName = resultSet.getString("Vaccine_Name");
                Date time = resultSet.getDate("Time");
                System.out.println("appointmentID: "+ ID + " vaccine: "+ vaccineName + " Date: "+ time + " Caregiver Name: " + caregiverName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }

    public static class PatientBuilder {
        private final String username;
        private final byte[] salt;
        private final byte[] hash;

        public PatientBuilder(String username, byte[] salt, byte[] hash) {
            this.username = username;
            this.salt = salt;
            this.hash = hash;
        }

        public Patient build() {
            return new Patient(this);
        }
    }

    public static class PatientGetter {
        private final String username;
        private final String password;
        private byte[] salt;
        private byte[] hash;

        public PatientGetter(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public Patient get() throws SQLException {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();

            String getPatient = "SELECT Salt, Hash FROM Patients WHERE Username = ?";
            try {
                PreparedStatement statement = con.prepareStatement(getPatient);
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
                        return new Patient(this);
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

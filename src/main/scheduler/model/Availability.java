package scheduler.model;

import scheduler.db.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;

public class Availability {


    public static ArrayList<String> getAllAvailable(Date date) throws SQLException{
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        ArrayList<String> res = new ArrayList<>();

        String searchAvailability = "SELECT Username From Availabilities WHERE Time = ? and isReserved = ?";
        try {
            PreparedStatement statement = con.prepareStatement(searchAvailability);
            statement.setDate(1, date);
            statement.setInt(2, 0);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("Username");
                res.add(name);
            }
            return res.size() == 0 ? null:res;
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }

    }
    public static String getFirstAvailable(Date date) throws SQLException{
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        String searchAvailability = "SELECT Username From Availabilities WHERE Time = ? and isReserved = ?;";
        try {
            PreparedStatement statement = con.prepareStatement(searchAvailability);
            statement.setDate(1, date);
            statement.setInt(2, 0);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.wasNull()) return null;
            while (resultSet.next()) {
                String name = resultSet.getString("Username");
                return name;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }

    }

    public static void uploadAvailability(Date date, String caregiverName) throws SQLException{
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String addAvailability = "UPDATE Availabilities SET isReserved = ? WHERE Username = ? and Time = ?";
        try {
            PreparedStatement statement = con.prepareStatement(addAvailability);
            statement.setInt(1,1);
            statement.setDate(3, date);
            statement.setString(2, caregiverName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException();
        } finally {
            cm.closeConnection();
        }
    }
}

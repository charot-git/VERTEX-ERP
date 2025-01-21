package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.Branch;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BranchDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public ObservableList<Branch> getBranchesWithNamesHead() {
        ObservableList<Branch> branches = FXCollections.observableArrayList();
        String query = "SELECT b.id, b.branch_description, b.branch_name, " +
                "COALESCE(CONCAT(u.user_fname, ' ', u.user_mname, ' ', u.user_lname), 'Unknown') AS branch_head_name, " +
                "b.branch_code, b.state_province, b.city, b.brgy, b.phone_number, b.postal_code, b.date_added, " +
                "b.isMoving, b.isReturn " +
                "FROM branches b " +
                "LEFT JOIN user u ON b.branch_head = u.user_id";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String branchDescription = resultSet.getString("branch_description");
                String branchName = resultSet.getString("branch_name");
                String branchHeadName = resultSet.getString("branch_head_name");
                String branchCode = resultSet.getString("branch_code");
                String stateProvince = resultSet.getString("state_province");
                String city = resultSet.getString("city");
                String brgy = resultSet.getString("brgy");
                String phoneNumber = resultSet.getString("phone_number");
                String postalCode = resultSet.getString("postal_code");
                Date dateAdded = resultSet.getDate("date_added");
                boolean isMoving = resultSet.getBoolean("isMoving");
                boolean isReturn = resultSet.getBoolean("isReturn");

                Branch branch = new Branch(id, branchDescription, branchName, branchHeadName, branchCode, stateProvince, city, brgy, phoneNumber, postalCode, dateAdded, isMoving, isReturn);
                branches.add(branch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return branches;
    }

    public List<Branch> getAllBranches() {
        List<Branch> branches = new ArrayList<>();

        String query = "SELECT * FROM branches";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Branch branch = new Branch();
                branch.setId(resultSet.getInt("id"));
                branch.setBranchDescription(resultSet.getString("branch_description"));
                branch.setBranchName(resultSet.getString("branch_name"));
                branch.setBranchHeadName(employeeDAO.getFullNameById(resultSet.getInt("branch_head")));
                branch.setBranchCode(resultSet.getString("branch_code"));
                branch.setStateProvince(resultSet.getString("state_province"));
                branch.setCity(resultSet.getString("city"));
                branch.setBrgy(resultSet.getString("brgy"));
                branch.setPhoneNumber(resultSet.getString("phone_number"));
                branch.setPostalCode(resultSet.getString("postal_code"));
                branch.setDateAdded(resultSet.getDate("date_added"));
                branches.add(branch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
        return branches;
    }

    public int getBranchIdByName(String branchName) {
        String query = "SELECT id FROM branches WHERE branch_name = ?";
        int branchId = -1; // Default value if branch is not found

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, branchName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    branchId = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL exceptions here
        }

        return branchId;
    }

    public ObservableList<String> getAllVehicleBranches() {
        ObservableList<String> branchNames = FXCollections.observableArrayList();
        String query = "SELECT branch_name FROM branches WHERE isMoving = 1 AND isReturn = 0";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String branchName = resultSet.getString("branch_name");
                branchNames.add(branchName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
        return branchNames;
    }


    public ObservableList<String> getAllNonMovingBranchNames() {
        ObservableList<String> branchNames = FXCollections.observableArrayList();
        String query = "SELECT branch_name FROM branches WHERE isMoving = 0 OR isMoving IS NULL"; // Selecting branch names where isMoving is 0 or NULL

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String branchName = resultSet.getString("branch_name");
                branchNames.add(branchName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
        return branchNames;
    }

    public ObservableList<String> getAllBranchNames() {
        ObservableList<String> branchNames = FXCollections.observableArrayList();

        String query = "SELECT branch_name FROM branches";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String branchName = resultSet.getString("branch_name");
                branchNames.add(branchName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
        return branchNames;
    }

    public String getBranchDescriptionById(int branchId) {
        String branchName = null;
        String query = "SELECT branch_description FROM branches WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, branchId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    branchName = resultSet.getString("branch_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
        return branchName;
    }

    public String getBranchNameById(int branchId) {
        String branchName = null;
        String query = "SELECT branch_name FROM branches WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, branchId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    branchName = resultSet.getString("branch_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
        return branchName;
    }

    public Branch getBranchById(int id) {
        Branch branch = null;
        String query = "SELECT * FROM branches WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    branch = new Branch(
                            resultSet.getInt("id"),
                            resultSet.getString("branch_description"),
                            resultSet.getString("branch_name"),
                            resultSet.getInt("branch_head"),
                            resultSet.getString("branch_code"),
                            resultSet.getString("state_province"),
                            resultSet.getString("city"),
                            resultSet.getString("brgy"),
                            resultSet.getString("phone_number"),
                            resultSet.getString("postal_code"),
                            resultSet.getDate("date_added"), resultSet.getBoolean("isMoving")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return branch;
    }

    public Branch getBranchByName(String name) {
        Branch branch = null;
        String query = "SELECT * FROM branches WHERE branch_name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, name);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    branch = new Branch(
                            resultSet.getInt("id"),
                            resultSet.getString("branch_description"),
                            resultSet.getString("branch_name"),
                            resultSet.getInt("branch_head"),
                            resultSet.getString("branch_code"),
                            resultSet.getString("state_province"),
                            resultSet.getString("city"),
                            resultSet.getString("brgy"),
                            resultSet.getString("phone_number"),
                            resultSet.getString("postal_code"),
                            resultSet.getDate("date_added"), resultSet.getBoolean("isMoving")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return branch;
    }

    EmployeeDAO employeeDAO = new EmployeeDAO();

    public boolean updateBranch(Branch branch) {
        String updateQuery = "UPDATE branches SET branch_description = ?, branch_name = ?, branch_head = ?, branch_code = ?, state_province = ?, city = ?, brgy = ?, phone_number = ?, postal_code = ?, date_added = ?, isMoving = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            preparedStatement.setString(1, branch.getBranchDescription());
            preparedStatement.setString(2, branch.getBranchName());
            preparedStatement.setInt(3, branch.getBranchHeadId());
            preparedStatement.setString(4, branch.getBranchCode());
            preparedStatement.setString(5, branch.getStateProvince());
            preparedStatement.setString(6, branch.getCity());
            preparedStatement.setString(7, branch.getBrgy());
            preparedStatement.setString(8, branch.getPhoneNumber());
            preparedStatement.setString(9, branch.getPostalCode());
            preparedStatement.setDate(10, branch.getDateAdded());
            preparedStatement.setBoolean(11, branch.isMoving()); // Add this line for isMoving
            preparedStatement.setInt(12, branch.getId());

            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Branch> getAllNonMovingNonReturnBranches() {
        List<Branch> branches = new ArrayList<>();
        String query = "SELECT * FROM branches WHERE isMoving = 0 AND isReturn = 0";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Branch branch = new Branch();
                branch.setId(resultSet.getInt("id"));
                branch.setBranchDescription(resultSet.getString("branch_description"));
                branch.setBranchName(resultSet.getString("branch_name"));
                branch.setBranchHeadName(employeeDAO.getFullNameById(resultSet.getInt("branch_head")));
                branch.setBranchCode(resultSet.getString("branch_code"));
                branch.setStateProvince(resultSet.getString("state_province"));
                branch.setCity(resultSet.getString("city"));
                branch.setBrgy(resultSet.getString("brgy"));
                branch.setPhoneNumber(resultSet.getString("phone_number"));
                branch.setPostalCode(resultSet.getString("postal_code"));
                branch.setDateAdded(resultSet.getDate("date_added"));
                branch.setMoving(resultSet.getBoolean("isMoving"));
                branch.setReturn(resultSet.getBoolean("isReturn"));

                branches.add(branch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
        return branches;
    }

    public String getBranchNameByCode(int branchCode) {
        String branchName = null;
        String query = "SELECT branch_name FROM branches WHERE branch_code = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, branchCode);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                branchName = resultSet.getString("branch_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
        return branchName;
    }

    public String getBranchCodeById(int id) {
        String branchCode = null;
        String query = "SELECT branch_code FROM branches WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                branchCode = resultSet.getString("branch_code");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
        return branchCode;
    }
}

package com.vertex.vos.Utilities;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.vertex.vos.Constructors.Branch;
import com.zaxxer.hikari.HikariDataSource;

public class BranchDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

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
                branch.setBranchHead(resultSet.getString("branch_head"));
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

    public List<String> getAllBranchNames() {
        List<String> branchNames = new ArrayList<>();

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

    public Branch getBranchById(int branchId) {
        Branch branch = null;
        String query = "SELECT * FROM branches WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, branchId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    branch = new Branch();
                    branch.setId(resultSet.getInt("id"));
                    branch.setBranchDescription(resultSet.getString("branch_description"));
                    branch.setBranchName(resultSet.getString("branch_name"));
                    branch.setBranchHead(resultSet.getString("branch_head"));
                    branch.setBranchCode(resultSet.getString("branch_code"));
                    branch.setStateProvince(resultSet.getString("state_province"));
                    branch.setCity(resultSet.getString("city"));
                    branch.setBrgy(resultSet.getString("brgy"));
                    branch.setPhoneNumber(resultSet.getString("phone_number"));
                    branch.setPostalCode(resultSet.getString("postal_code"));
                    branch.setDateAdded(resultSet.getDate("date_added"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
        }
        return branch;
    }
}

package com.vertex.vos.Utilities;

import com.vertex.vos.Constructors.Company;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class CompanyDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public int getCompanyIdByName(String companyName) {
        int companyId = -1; // Default value if not found

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT company_id FROM company WHERE company_name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, companyName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        companyId = resultSet.getInt("company_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        return companyId;
    }

    public String getCompanyNameById(int companyId) {
        String companyName = null; // Default value if not found

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT company_name FROM company WHERE company_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, companyId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        companyName = resultSet.getString("company_name");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        return companyName;
    }
    public boolean updateCompany(Company company) {
        String sql = "UPDATE company SET " +
                "company_name = ?, " +
                "company_type = ?, " +
                "company_code = ?, " +
                "company_firstAddress = ?, " +
                "company_secondAddress = ?, " +
                "company_registrationNumber = ?, " +
                "company_tin = ?, " +
                "company_dateAdmitted = ?, " +
                "company_contact = ?, " +
                "company_email = ?, " +
                "company_department = ?, " +
                "company_tags = ?, " +
                "company_logo = ? " +  // Include company_logo column for updating image URL
                "WHERE company_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            // Set values for the prepared statement
            preparedStatement.setString(1, company.getCompanyName());
            preparedStatement.setString(2, company.getCompanyType());
            preparedStatement.setString(3, company.getCompanyCode());
            preparedStatement.setString(4, company.getCompanyFirstAddress());
            preparedStatement.setString(5, company.getCompanySecondAddress());
            preparedStatement.setString(6, company.getCompanyRegistrationNumber());
            preparedStatement.setString(7, company.getCompanyTIN());
            preparedStatement.setDate(8, company.getCompanyDateAdmitted());
            preparedStatement.setString(9, company.getCompanyContact());
            preparedStatement.setString(10, company.getCompanyEmail());
            preparedStatement.setString(11, company.getCompanyDepartment());
            preparedStatement.setString(12, company.getCompanyTags());
            preparedStatement.setString(13, company.getCompanyLogo());
            preparedStatement.setInt(14, company.getCompanyId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Company updated successfully!");
                return true;
            } else {
                System.out.println("Failed to update company.");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean addCompany(Company company) {
        String sql = "INSERT INTO company (company_name, company_type, company_code, " +
                "company_firstAddress, company_secondAddress, company_registrationNumber, " +
                "company_tin, company_dateAdmitted, company_contact, company_email, " +
                "company_department, company_tags) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            // Set values for the prepared statement
            preparedStatement.setString(1, company.getCompanyName());
            preparedStatement.setString(2, company.getCompanyType());
            preparedStatement.setString(3, company.getCompanyCode());
            preparedStatement.setString(4, company.getCompanyFirstAddress());
            preparedStatement.setString(5, company.getCompanySecondAddress());
            preparedStatement.setString(6, company.getCompanyRegistrationNumber());
            preparedStatement.setString(7, company.getCompanyTIN());
            preparedStatement.setDate(8, company.getCompanyDateAdmitted());
            preparedStatement.setString(9, company.getCompanyContact());
            preparedStatement.setString(10, company.getCompanyEmail());
            preparedStatement.setString(11, company.getCompanyDepartment());
            preparedStatement.setString(12, company.getCompanyTags());
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Company added successfully!");
                return true;
            } else {
                System.out.println("Failed to add company.");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public ObservableList<String> getAllCompanyNames() {
        ObservableList<String> companyNames = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT company_name FROM company";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String companyName = resultSet.getString("company_name");
                        companyNames.add(companyName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        return companyNames;
    }

    public ObservableList<Company> getAllCompanies() {
        ObservableList<Company> companies = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM company";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Company company = new Company();
                        company.setCompanyId(resultSet.getInt("company_id"));
                        company.setCompanyName(resultSet.getString("company_name"));
                        company.setCompanyType(resultSet.getString("company_type"));
                        company.setCompanyCode(resultSet.getString("company_code"));
                        company.setCompanyFirstAddress(resultSet.getString("company_firstAddress"));
                        company.setCompanySecondAddress(resultSet.getString("company_secondAddress"));
                        company.setCompanyRegistrationNumber(resultSet.getString("company_registrationNumber"));
                        company.setCompanyTIN(resultSet.getString("company_tin"));
                        company.setCompanyDateAdmitted(resultSet.getDate("company_dateAdmitted"));
                        company.setCompanyContact(resultSet.getString("company_contact"));
                        company.setCompanyEmail(resultSet.getString("company_email"));
                        company.setCompanyDepartment(resultSet.getString("company_department"));
                        company.setCompanyTags(resultSet.getString("company_tags"));

                        companies.add(company);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }

        return companies;
    }

    public Company getCompanyById(int companyId) {
        Company company = null;

        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT * FROM company WHERE company_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, companyId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        company = new Company();
                        company.setCompanyId(resultSet.getInt("company_id"));
                        company.setCompanyName(resultSet.getString("company_name"));
                        company.setCompanyType(resultSet.getString("company_type"));
                        company.setCompanyCode(resultSet.getString("company_code"));
                        company.setCompanyFirstAddress(resultSet.getString("company_firstAddress"));
                        company.setCompanySecondAddress(resultSet.getString("company_secondAddress"));
                        company.setCompanyRegistrationNumber(resultSet.getString("company_registrationNumber"));
                        company.setCompanyTIN(resultSet.getString("company_tin"));
                        company.setCompanyDateAdmitted(resultSet.getDate("company_dateAdmitted"));
                        company.setCompanyContact(resultSet.getString("company_contact"));
                        company.setCompanyEmail(resultSet.getString("company_email"));
                        company.setCompanyDepartment(resultSet.getString("company_department"));
                        company.setCompanyTags(resultSet.getString("company_tags"));
                        company.setCompanyLogo(resultSet.getString("company_logo"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
        return company;
    }

}

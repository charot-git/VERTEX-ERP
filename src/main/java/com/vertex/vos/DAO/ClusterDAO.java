package com.vertex.vos.DAO;

import com.vertex.vos.Objects.Cluster;
import com.vertex.vos.Objects.AreaPerCluster;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClusterDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    public Cluster getClusterById(int id) {
        Cluster cluster = null;
        String query = "SELECT * FROM cluster WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                cluster = new Cluster();
                cluster.setId(resultSet.getInt("id"));
                cluster.setClusterName(resultSet.getString("cluster_name"));
                cluster.setMinimumAmount(resultSet.getDouble("minimum_amount"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cluster;
    }

    public List<Cluster> getAllClusters() {
        List<Cluster> clusters = new ArrayList<>();
        String query = "SELECT * FROM cluster";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Cluster cluster = new Cluster();
                cluster.setId(resultSet.getInt("id"));
                cluster.setClusterName(resultSet.getString("cluster_name"));
                cluster.setMinimumAmount(resultSet.getDouble("minimum_amount"));
                clusters.add(cluster);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clusters;
    }

    public boolean addCluster(Cluster cluster) {
        String query = "INSERT INTO cluster (cluster_name, minimum_amount) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, cluster.getClusterName());
            preparedStatement.setDouble(2, cluster.getMinimumAmount());
            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateCluster(Cluster cluster) {
        String query = "UPDATE cluster SET cluster_name = ?, minimum_amount = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, cluster.getClusterName());
            preparedStatement.setDouble(2, cluster.getMinimumAmount());
            preparedStatement.setInt(3, cluster.getId());
            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteCluster(int id) {
        String query = "DELETE FROM cluster WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);
            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<AreaPerCluster> getAreasByClusterId(int clusterId) {
        List<AreaPerCluster> areas = new ArrayList<>();
        String query = "SELECT * FROM area_per_cluster WHERE cluster_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, clusterId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                AreaPerCluster area = new AreaPerCluster();
                area.setId(resultSet.getInt("id"));
                area.setClusterId(resultSet.getInt("cluster_id"));
                area.setProvince(resultSet.getString("province"));
                area.setCity(resultSet.getString("city"));
                area.setBaranggay(resultSet.getString("baranggay"));
                areas.add(area);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return areas;
    }

    public boolean addAreaToCluster(AreaPerCluster area) {
        String query = "INSERT INTO area_per_cluster (cluster_id, province, city, baranggay) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, area.getClusterId());
            stmt.setString(2, area.getProvince());
            stmt.setString(3, area.getCity());
            stmt.setString(4, area.getBaranggay());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Returns true if insertion was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Returns false if an error occurs
        }
    }

    public boolean deleteAreaFromCluster(int id) {
        String query = "DELETE FROM area_per_cluster WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Returns true if deletion was successful
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Returns false if an error occurs
        }
    }
}

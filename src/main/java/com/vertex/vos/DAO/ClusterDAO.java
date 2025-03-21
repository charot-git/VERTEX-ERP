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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

    // Caching data
    private static final Map<Integer, Cluster> clusterCache = new HashMap<>();
    private static final Map<Integer, List<AreaPerCluster>> areaCache = new HashMap<>();

    public ClusterDAO() {
        preloadClustersAndAreas();
    }
    private void preloadClustersAndAreas() {
        preloadClusters();
        preloadAreas();
    }

    private void preloadClusters() {
        String query = "SELECT * FROM cluster";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Cluster cluster = new Cluster();
                cluster.setId(resultSet.getInt("id"));
                cluster.setClusterName(resultSet.getString("cluster_name"));
                cluster.setMinimumAmount(resultSet.getDouble("minimum_amount"));
                clusterCache.put(cluster.getId(), cluster);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void preloadAreas() {
        String query = "SELECT * FROM area_per_cluster";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                AreaPerCluster area = new AreaPerCluster();
                area.setId(resultSet.getInt("id"));
                area.setClusterId(resultSet.getInt("cluster_id"));
                area.setProvince(resultSet.getString("province"));
                area.setCity(resultSet.getString("city"));
                area.setBaranggay(resultSet.getString("baranggay"));

                areaCache.computeIfAbsent(area.getClusterId(), k -> new ArrayList<>()).add(area);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Cluster getClusterById(int id) {
        if (clusterCache.containsKey(id)) {
            return clusterCache.get(id);
        }

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

                clusterCache.put(id, cluster);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cluster;
    }

    public List<Cluster> getAllClusters() {
        if (!clusterCache.isEmpty()) {
            return new ArrayList<>(clusterCache.values());
        }

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
                clusterCache.put(cluster.getId(), cluster);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clusters;
    }

    public boolean addCluster(Cluster cluster) {
        String query = "INSERT INTO cluster (cluster_name, minimum_amount) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, cluster.getClusterName());
            preparedStatement.setDouble(2, cluster.getMinimumAmount());
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    cluster.setId(generatedKeys.getInt(1));
                    clusterCache.put(cluster.getId(), cluster); // Update cache
                }
                return true;
            }

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

            if (rowsAffected > 0) {
                clusterCache.put(cluster.getId(), cluster); // Update cache
                return true;
            }

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

            if (rowsAffected > 0) {
                clusterCache.remove(id); // Remove from cache
                areaCache.remove(id);    // Remove related areas from cache
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<AreaPerCluster> getAreasByClusterId(int clusterId) {
        if (areaCache.containsKey(clusterId)) {
            return areaCache.get(clusterId);
        }

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

            areaCache.put(clusterId, areas);

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

            if (rowsAffected > 0) {
                areaCache.remove(area.getClusterId()); // Invalidate area cache for this cluster
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteAreaFromCluster(int id) {
        String query = "DELETE FROM area_per_cluster WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Remove the area from cache if it exists
                areaCache.values().forEach(areaList -> areaList.removeIf(area -> area.getId() == id));
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public Cluster getClusterByArea(String province, String city, String baranggay) {
        for (Map.Entry<Integer, List<AreaPerCluster>> entry : areaCache.entrySet()) {
            for (AreaPerCluster area : entry.getValue()) {
                if (area.getProvince().equalsIgnoreCase(province) &&
                        area.getCity().equalsIgnoreCase(city) &&
                        (area.getBaranggay() == null || area.getBaranggay().equalsIgnoreCase(baranggay) || baranggay == null || baranggay.isEmpty())) {

                    return clusterCache.get(area.getClusterId());
                }
            }
        }
        return null; // Return null if not found
    }



    public void clearCaches() {
        clusterCache.clear();
        areaCache.clear();
    }
}

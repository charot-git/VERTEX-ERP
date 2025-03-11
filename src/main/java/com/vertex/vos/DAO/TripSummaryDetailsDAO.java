package com.vertex.vos.DAO;

import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;

public class TripSummaryDetailsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
}

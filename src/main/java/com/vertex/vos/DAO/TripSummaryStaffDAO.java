package com.vertex.vos.DAO;

import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.zaxxer.hikari.HikariDataSource;

public class TripSummaryStaffDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
}

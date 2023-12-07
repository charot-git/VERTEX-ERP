package com.vertex.vos.Utilities;

import com.zaxxer.hikari.HikariDataSource;

public class ChartOfAccountsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
}

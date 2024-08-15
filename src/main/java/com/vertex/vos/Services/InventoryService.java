package com.vertex.vos.Services;

import com.vertex.vos.Objects.Inventory;
import com.vertex.vos.Objects.ProductBreakdown;
import com.vertex.vos.Objects.ProductsInTransact;
import com.vertex.vos.Utilities.DatabaseConnectionPool;
import com.vertex.vos.Utilities.InventoryDAO;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InventoryService {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();

}

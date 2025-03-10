package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.TripSummaryStaff;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TripSummaryStaffDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
}

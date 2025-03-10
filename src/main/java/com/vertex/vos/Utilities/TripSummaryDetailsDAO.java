package com.vertex.vos.Utilities;

import com.vertex.vos.Objects.TripSummary;
import com.vertex.vos.Objects.TripSummaryDetails;
import com.vertex.vos.Objects.TripSummaryStaff;
import com.zaxxer.hikari.HikariDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class TripSummaryDetailsDAO {
    private final HikariDataSource dataSource = DatabaseConnectionPool.getDataSource();
}

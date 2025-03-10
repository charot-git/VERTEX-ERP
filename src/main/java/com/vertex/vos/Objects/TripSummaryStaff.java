package com.vertex.vos.Objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripSummaryStaff {
    private int id;
    private TripSummary tripSummary;
    private User staff;
    private TripSummaryStaffRole role;

    public enum TripSummaryStaffRole {
        Driver, Helper
    }

}

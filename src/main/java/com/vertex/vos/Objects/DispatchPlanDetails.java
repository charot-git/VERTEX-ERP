package com.vertex.vos.Objects;

import lombok.Data;

@Data
public class DispatchPlanDetails {
    private int detailId;
    private DispatchPlan dispatchPlan;
    private SalesOrder salesOrder;
}

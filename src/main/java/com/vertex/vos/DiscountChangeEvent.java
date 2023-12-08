package com.vertex.vos;

import javafx.event.Event;
import javafx.event.EventType;

public class DiscountChangeEvent extends Event {
    public static final EventType<DiscountChangeEvent> DISCOUNT_CHANGE_EVENT =
            new EventType<>(Event.ANY, "DISCOUNT_CHANGE_EVENT");

    public DiscountChangeEvent() {
        super(DISCOUNT_CHANGE_EVENT);
    }
}

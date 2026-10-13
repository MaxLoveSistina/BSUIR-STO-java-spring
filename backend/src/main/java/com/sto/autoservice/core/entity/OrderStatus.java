package com.sto.autoservice.core.entity;

/**
 * Статус заказа и допустимые переходы между статусами.
 * <pre>
 * NEW ──► IN_PROGRESS ──► COMPLETED
 *  │           │
 *  └──► CANCELLED ◄──────┘
 * </pre>
 */
public enum OrderStatus {
    NEW,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case NEW -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == COMPLETED || next == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}

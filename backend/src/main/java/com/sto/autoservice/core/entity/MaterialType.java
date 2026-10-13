package com.sto.autoservice.core.entity;

/**
 * Класс материалов (запчастей/расходников), который выбирает клиент.
 */
public enum MaterialType {
    /** Дешёвые материалы (аналоги, бюджетные). */
    ECONOMY,
    /** Дорогие материалы (оригинал, премиум). */
    PREMIUM
}

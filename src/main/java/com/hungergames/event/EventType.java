package com.hungergames.event;

public enum EventType {
    MUTTATION("Mutaciones", "&c"),
    ENVIRONMENTAL("Ambiental", "&6"),
    SUPPLY_DROP("Suministros", "&b"),
    ARENA_CHANGE("Cambio de Arena", "&d"),
    PLAYER_EFFECT("Efecto Jugador", "&e"),
    WORLD_EVENT("Evento Mundial", "&5"),
    BOSS("Jefe", "&c&l"),
    TRAP("Trampa", "&8"),
    BLESSING("Bendición", "&a");

    private final String displayName;
    private final String color;

    EventType(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
}
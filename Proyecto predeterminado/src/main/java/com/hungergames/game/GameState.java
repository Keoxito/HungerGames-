package com.hungergames.game;

public enum GameState {
    WAITING("Esperando jugadores"),
    STARTING("Iniciando..."),
    PEDESTAL_RISING("Pedestales subiendo"),
    GRACE_PERIOD("Periodo de gracia"),
    IN_PROGRESS("En juego"),
    DEATHMATCH("Combate a muerte"),
    FINISHED("Finalizado"),
    RESTARTING("Reiniciando");

    private final String displayName;

    GameState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPlaying() {
        return this == IN_PROGRESS || this == DEATHMATCH || this == GRACE_PERIOD;
    }

    public boolean canJoin() {
        return this == WAITING || this == STARTING;
    }
}
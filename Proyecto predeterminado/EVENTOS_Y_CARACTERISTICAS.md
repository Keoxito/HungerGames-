# HungerGames Plugin - Documentación Completa

## 📋 Resumen del Plugin
Plugin completo de Juegos del Hambre para Minecraft 1.21.1 (Paper/Spigot) con sistema de eventos, patrocinadores, pedestales y más.

---

## 🎮 Eventos Disponibles (10 eventos)

### 🧟 **Mutaciones** (`mutts`)
- **Tipo:** MUTTATION
- **Cooldown:** 300s | **Mín. jugadores:** 2
- Libera mutaciones genéticas (lobos, arañas, zombis, devastadores, vindicators, husks, strays, bogeymen) que persiguen a los tributos.

### 🔥 **Tormenta de Fuego** (`fire_storm`)
- **Tipo:** ENVIRONMENTAL
- **Cooldown:** 240s | **Mín. jugadores:** 2
- Llueve fuego del cielo en zonas aleatorias de la arena.

### ⚡ **Tormenta de Rayos** (`storm`)
- **Tipo:** ENVIRONMENTAL
- **Cooldown:** 180s | **Mín. jugadores:** 2
- Tormenta con rayos que golpean jugadores y zonas aleatorias.

### 🍽️ **Banquete** (`feast`)
- **Tipo:** SUPPLY_DROP
- **Cooldown:** 600s | **Mín. jugadores:** 3
- Aparece un banquete en el centro con cofres de loot épico y legendario (Netherite, Elytra, Totems, Manzanas de Oro Encantadas).

### 📦 **Drop de Suministros** (`supply_drop`)
- **Tipo:** SUPPLY_DROP
- **Cooldown:** 120s | **Mín. jugadores:** 2
- Cajas de suministros caen del cielo con items útiles (comida, armas, herramientas, perlas, XP).

### 🌍 **Cambio de Arena** (`arena_shift`)
- **Tipo:** ARENA_CHANGE
- **Cooldown:** 480s | **Mín. jugadores:** 3
- Transformaciones drásticas: Inundación de Lava, Inundación de Agua, Terremoto, Expansión del Vacío.

### 💣 **Trampas Mortales** (`traps`)
- **Tipo:** TRAP
- **Cooldown:** 180s | **Mín. jugadores:** 2
- Trampas aleatorias: Suelo Explosivo, Gas Venenoso, Trampa de Arena, Agujeros al Vacío, Lluvia de Yunque.

### ✨ **Efectos Globales** (`player_effects`)
- **Tipo:** PLAYER_EFFECT
- **Cooldown:** 120s | **Mín. jugadores:** 2
- Efectos en todos: Hambruna, Ceguera, Mareas, Resplandor, Debilidad, Regeneración, Intercambio de Posiciones.

### 🌟 **Bendición del Capitolio** (`blessing`)
- **Tipo:** BLESSING
- **Cooldown:** 300s | **Mín. jugadores:** 3
- Un tributo (prioridad a menos kills) recibe una bendición: Invencibilidad, Velocidad, Furia, Fantasma, Tanque, Arquero.

### 👑 **Jefe de la Arena** (`boss`)
- **Tipo:** BOSS
- **Cooldown:** 600s | **Mín. jugadores:** 4
- Jefes con Boss Bar: Warden, Wither, Dragón del End, Rey Devastador. Recompensas legendarias al morir.

---

## 🎁 Sistema de Patrocinio

### Item de Patrocinio (Jugadores)
- **Comando:** `/hgsponsor` o click derecho en Nether Star
- **GUI:** Selecciona item → Click en cabeza de jugador vivo
- **Límites:** 3 patrocinios por jugador, cooldown 30s
- **Efecto:** Item cae del cielo (50 bloques) con partículas y sonido al objetivo

### Item de Owner (Admins)
- **Comando:** `/hgevent` o click derecho en Command Block
- **GUI:** Panel con todos los eventos, click izquierdo para ejecutar
- **Permiso:** `hungergames.owner`

### Items Entregables por Comando
```bash
/hg giveitem sponsor [jugador]  # Da Nether Star de patrocinio
/hg giveitem owner [jugador]    # Da Command Block de eventos
```

---

## 🏗️ Sistema de Pedestales

### Funcionamiento
1. Configurar pedestales en arena: `/hgarena set <arena> pedestal`
2. Jugadores se paran encima
3. Al iniciar partida (`/hgstart`), teletransporta a pedestales
4. Comando `/hgpedestal` activa visualmente
5. **Subida automática:** 5 segundos (configurable) con efectos
6. Al llegar arriba: Quita ceguera/lentitud, inicia periodo de gracia

### Configuración
```yaml
pedestal:
  rise-speed: 0.2      # bloques/tick
  max-height: 20       # altura máxima
  rise-duration: 100   # ticks (5 seg)
```

---

## 👥 Sistema de Jugadores Vivos

### Estados
- **Vivos:** Pueden jugar, recibir sponsors, ganar
- **Espectadores (muertos):** Modo espectador, chat solo entre muertos, comandos limitados
- **Fuera de partida:** Pueden unirse con `/hg join`

### Tracking
- Kills por jugador
- Items de patrocinio recibidos
- Lista en tiempo real con `/hg list`

---

## 🏟️ Gestión de Arenas

### Creación
```
/hgarena crear <nombre>          # Crea arena en tu posición
/hgarena set <arena> lobby       # Establece lobby
/hgarena set <arena> centro      # Establece centro
/hgarena set <arena> radio <n>   # Radio de arena
/hgarena set <arena> spawn       # Añade spawn point
/hgarena set <arena> pedestal    # Añade pedestal
/hgarena set <arena> cofre       # Añade cofre inicial
/hgarena set <arena> deathmatch  # Centro deathmatch
/hgarena set <arena> dmradio <n> # Radio deathmatch
/hgarena set <arena> altura <n>  # Altura límite
/hgarena listar                  # Lista todas
/hgarena info <arena>            # Info detallada
/hgarena eliminar <arena>        # Elimina arena
```

---

## ⚙️ Configuración Principal (config.yml)

```yaml
game:
  min-players: 2
  max-players: 24
  grace-period: 60
  duration-minutes: 30
  deathmatch-delay: 25

pedestal:
  rise-speed: 0.2
  max-height: 20
  rise-duration: 100

sponsor:
  max-per-player: 3
  cooldown: 30
  drop-height: 50
  drop-speed: 1.5

events:
  auto-enabled: true
  auto-interval: 120
  min-interval: 60
  max-interval: 300

rewards:
  winner:
    - "ITEM:DIAMOND:64"
    - "ITEM:ENCHANTED_GOLDEN_APPLE:4"
    - "COMMAND:eco give %player% 10000"
```

---

## 📝 Comandos Principales

| Comando | Descripción | Permiso |
|---------|-------------|---------|
| `/hg start <arena>` | Iniciar partida | `hungergames.admin` |
| `/hg stop` | Detener partida | `hungergames.admin` |
| `/hg status` | Ver estado | `hungergames.admin` |
| `/hg join` | Unirse | `hungergames.player` |
| `/hg leave` | Salir | `hungergames.player` |
| `/hg list` | Listar jugadores | `hungergames.admin` |
| `/hg arenas` | Listar arenas | `hungergames.admin` |
| `/hg reload` | Recargar config | `hungergames.admin` |
| `/hg giveitem <tipo> [jugador]` | Dar items especiales | `hungergames.admin` |
| `/hg event <evento> [arena]` | Ejecutar evento | `hungergames.owner` |
| `/hgpedestal` | Activar pedestal | `hungergames.player` |
| `/hgsponsor [jugador] [item]` | Menú patrocinio | `hungergames.sponsor` |
| `/hgevent` | Panel eventos (Owner) | `hungergames.owner` |
| `/hglobby` | Ir al lobby | `hungergames.player` |
| `/hgarena <subcomando>` | Gestión arenas | `hungergames.admin` |

---

## 🔧 Permisos

| Permiso | Descripción | Default |
|---------|-------------|---------|
| `hungergames.admin` | Admin completo | OP |
| `hungergames.owner` | Owner (eventos) | OP |
| `hungergames.sponsor` | Puede patrocinar | TRUE |
| `hungergames.player` | Jugador básico | TRUE |

---

## 🎯 Flujo de Partida Típico

1. **Setup:** `/hgarena crear arena1` → configurar todo → `/hgarena set arena1 pedestal` (x24)
2. **Lobby:** Jugadores hacen `/hg join` → se paran en pedestales
3. **Inicio:** Admin `/hgstart arena1` → Teletransporta a pedestales → Subida 5s
4. **Grace Period:** 60s sin PvP para huir
5. **Juego:** Eventos automáticos cada 2 min + Border shrink
6. **Deathmatch:** Min 25 → Teleport al centro → Border rápido
7. **Victoria:** Último vivo gana → Recompensas → Limpieza → Lobby

---

## 📁 Estructura de Archivos

```
HungerGames/
├── plugin.yml
├── config.yml
├── messages.yml
├── arenas.yml (auto-generado)
└── src/main/java/com/hungergames/
    ├── HungerGamesPlugin.java
    ├── arena/
    │   ├── Arena.java
    │   └── ArenaManager.java
    ├── command/
    │   ├── MainCommand.java
    │   ├── PedestalCommand.java
    │   ├── SponsorCommand.java
    │   ├── EventCommand.java
    │   ├── StartCommand.java
    │   ├── StopCommand.java
    │   ├── LobbyCommand.java
    │   └── ArenaCommand.java
    ├── event/
    │   ├── EventManager.java
    │   ├── GameEvent.java
    │   ├── EventType.java
    │   └── impl/
    │       ├── MuttsEvent.java
    │       ├── FireEvent.java
    │       ├── StormEvent.java
    │       ├── FeastEvent.java
    │       ├── SupplyDropEvent.java
    │       ├── ArenaShiftEvent.java
    │       ├── TrapEvent.java
    │       ├── PlayerEffectEvent.java
    │       ├── BlessingEvent.java
    │       └── BossEvent.java
    ├── game/
    │   ├── GameManager.java
    │   └── GameState.java
    ├── listener/
    │   ├── PlayerListener.java
    │   ├── GameListener.java
    │   ├── PedestalListener.java
    │   ├── SponsorListener.java
    │   ├── EventListener.java
    │   └── EntityListener.java
    ├── pedestal/
    │   └── PedestalManager.java
    ├── sponsor/
    │   └── SponsorManager.java
    └── util/
        ├── ConfigManager.java
        └── MessageManager.java
```

---

## 🚀 Compilación

```bash
# Requiere Java 21 + Maven
mvn clean package
# Output: target/HungerGames-1.0.0.jar
```

---

## 📦 Dependencias
- Paper 1.21.1 API (provided)
- Lombok (provided, para annotations)

---

## ✨ Características Técnicas

- **Thread-safe:** ConcurrentHashMap para datos de jugadores
- **Event-driven:** Sistema de eventos modular y extensible
- **Configurable:** Todo en YAML (mensajes, config, arenas)
- **GUI-based:** Inventarios personalizados para sponsors/eventos
- **Persistent Data:** Items marcados con NBT para identificación
- **WorldBorder API:** Borde nativo de Minecraft
- **BossBar API:** Barras de vida para jefes
- **Metadata:** Entidades marcadas para tracking
- **Partículas/Sonidos:** Experiencia inmersiva completa

---

## 🔮 Eventos Futuros Sugeridos

- **Carrera de la Muerte:** Corredor con trampas
- **Caza del Tesoro:** Pistas por la arena
- **Alianza Forzada:** Empareja jugadores
- **Hambruna Total:** Comida se pudre
- **Eclipse:** Oscuridad total + mobs buff
- **Meteoritos:** Explosiones aleatorias
- **Zona Segura Móvil:** Círculo que se mueve
- **Intercambio de Inventarios:** Caos total
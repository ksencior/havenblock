package pl.ksendev.havenblock.island;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import pl.ksendev.havenblock.HavenBlock;
import pl.ksendev.havenblock.database.DatabaseManager;

public class IslandManager {
    private final HavenBlock plugin;
    private final Map<UUID, Island> islands = new HashMap<>();
    private final DatabaseManager databaseManager;
    private final IslandGenerator islandGenerator;

    private Location lobbyLocation;

    public IslandManager(HavenBlock _pl) {
        this.plugin = _pl;
        this.databaseManager = new DatabaseManager(plugin);
        
        // Pobieranie świata (zakładamy że domyślna to "world")
        World world = Bukkit.getWorld("world");
        if (world == null) {
            plugin.getLogger().severe("Świat 'world' nie istnieje!");
            this.islandGenerator = null;
        } else {
            this.islandGenerator = new IslandGenerator(plugin, world);
        }
        
        loadIslands();
    }

    public void reloadIslands() {
        islands.clear();
        loadIslands();
    }

    private void loadIslands() {
        // Ładowanie wysep z bazy danych będzie po stronie aplikacji
        // Wysepy są ładowane na żądanie (najleniwy loading)
    }

    public boolean createNewIsland(UUID uuid) {
        // Sprawdzenie czy gracz ma już wyspę
        if (databaseManager.hasIsland(uuid)) {
            return false;
        }
        
        if (islandGenerator == null) {
            plugin.getLogger().severe("IslandGenerator nie został zainicjalizowany!");
            return false;
        }
        
        Island newIsland = new Island(uuid);
        
        // Generowanie wyspy z schematu
        if (!islandGenerator.generateIsland(newIsland)) {
            plugin.getLogger().severe("Błąd przy generowaniu wyspy dla gracza " + uuid);
            return false;
        }
        
        // Zapis do bazy
        if (!databaseManager.saveIsland(newIsland)) {
            plugin.getLogger().severe("Błąd przy zapisie wyspy do bazy!");
            return false;
        }
        
        // Cache w pamięci
        islands.put(uuid, newIsland);
        return true;
    }

    public boolean deleteIsland(UUID uuid) {
        // Wczytanie wyspy
        World world = Bukkit.getWorld("world");
        Island island = databaseManager.loadIsland(uuid, world);
        
        if (island == null) {
            return false;
        }
        
        // Czyszczenie terenu wyspy
        if (islandGenerator != null) {
            islandGenerator.deleteIsland(island);
        }
        
        // Usunięcie z bazy
        if (!databaseManager.deleteIsland(uuid)) {
            return false;
        }
        
        // Usunięcie z cache
        islands.remove(uuid);
        return true;
    }

    public Island getIsland(UUID uuid) {
        // Najpierw sprawdzaj cache
        if (islands.containsKey(uuid)) {
            return islands.get(uuid);
        }
        
        World world = Bukkit.getWorld("world");
        if (world == null) {
            return null;
        }
        
        // Szukamy czy gracz jest właścicielem wyspy
        Island island = databaseManager.loadIsland(uuid, world);
        if (island != null) {
            // Ładujemy wszystkich członków wyspy
            databaseManager.loadIslandMembers(island);
            islands.put(uuid, island);
            return island;
        }
        
        // Jeśli nie jest właścicielem - szukamy czy jest członkiem jakiejś wyspy
        island = databaseManager.getIslandByMember(uuid, world);
        if (island != null) {
            // Ładujemy wszystkich członków tej wyspy
            databaseManager.loadIslandMembers(island);
            // Cache dla ułatwienia później
            islands.put(island.getOwnerUUID(), island);
            return island;
        }
        
        return null;
    }

    public void addMemberToIsland(Island island, UUID memberUUID, String role) {
        island.getIslandMembers().put(memberUUID, IslandRoles.valueOf(role));
        databaseManager.addMember(island.getOwnerUUID(), memberUUID, role);
    }

    public void kickPlayerFromIsland(Island island, UUID playerUuid) {
        if (!island.getIslandMembers().containsKey(playerUuid))
            return;
        island.removeFromIslandMembers(playerUuid);
        databaseManager.removeMember(island.getOwnerUUID(), playerUuid);
    }

    public void setLobbyLocation(Location loc) {
        lobbyLocation = loc;
    }
    public void setLobbyLocation(Player player) {
        lobbyLocation = player.getLocation();
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}

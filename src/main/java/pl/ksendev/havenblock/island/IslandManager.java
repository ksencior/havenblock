package pl.ksendev.havenblock.island;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pl.ksendev.havenblock.HavenBlock;

public class IslandManager {
    private final HavenBlock plugin;
    private final Map<UUID, Island> islands = new HashMap<>();
    private final File islandsFile;
    private final IslandGenerator islandGenerator;

    private Location lobbyLocation;

    public IslandManager(HavenBlock _pl) {
        this.plugin = _pl;
        this.islandsFile = new File(plugin.getDataFolder(), "islands.yml");
        
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
        if (!islandsFile.exists())
            return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(islandsFile);

        for (String uuid : config.getKeys(false)) {
            Island island = new Island(UUID.fromString(uuid));

            Location islandSpawn = config.getLocation(uuid + ".islandSpawn");
            if (islandSpawn != null) island.setIslandSpawn(islandSpawn);
            
            // Ładowanie nowych pól
            int gridX = config.getInt(uuid + ".gridX", 0);
            int gridZ = config.getInt(uuid + ".gridZ", 0);
            island.setGridPosition(gridX, gridZ);
            
            String schematicName = config.getString(uuid + ".schematicName", "");
            island.setSchematicName(schematicName);
            
            Location minBlock = config.getLocation(uuid + ".buildableMinBlock");
            Location maxBlock = config.getLocation(uuid + ".buildableMaxBlock");
            if (minBlock != null && maxBlock != null) {
                island.setBuildableRegion(minBlock, maxBlock);
            }
            
            islands.put(island.getOwnerUUID(), island);
        }
    }        

    public boolean createNewIsland(UUID uuid) {
        if (islands.containsKey(uuid))
            return false;
        
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
        
        islands.put(uuid, newIsland);
        saveIslands();
        return true;
    }

    public boolean deleteIsland(UUID uuid) {
        if (islands.containsKey(uuid)) {
            Island island = islands.get(uuid);
            
            // Czyszczenie terenu wyspy
            if (islandGenerator != null) {
                islandGenerator.deleteIsland(island);
            }
            
            // Usunięcie z mapy
            islands.remove(uuid);
            saveIslands();
            return true;
        }
        return false;
    }

    public Island getIsland(UUID uuid) {
        return islands.get(uuid);
    }

    public void saveIslands() {
        FileConfiguration config = new YamlConfiguration();

        try {
            for (Island island : islands.values()) {
                String uuid = island.getOwnerUUID().toString();
                
                config.set(uuid + ".islandSpawn", island.getIslandSpawn());
                config.set(uuid + ".gridX", island.getGridX());
                config.set(uuid + ".gridZ", island.getGridZ());
                config.set(uuid + ".schematicName", island.getSchematicName());
                config.set(uuid + ".buildableMinBlock", island.getBuildableMinBlock());
                config.set(uuid + ".buildableMaxBlock", island.getBuildableMaxBlock());
            }
            config.save(islandsFile);
        } catch (IOException err) {
            plugin.getLogger().severe("Nie udalo sie zapisac islands.yml: " + err.getMessage());
        }
    }

    public void setLobbyLocation(Player player) {
        lobbyLocation = player.getLocation();

        plugin.saveConfig();
    }
    public void setLobbyLocation(Location loc) {
        lobbyLocation = loc;
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }
}

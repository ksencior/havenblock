package pl.ksendev.havenblock.island;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pl.ksendev.havenblock.HavenBlock;

public class IslandManager {
    private final HavenBlock plugin;
    private final Map<UUID, Island> islands = new HashMap<>();
    private final File islandsFile;

    private Location lobbyLocation;

    public IslandManager(HavenBlock _pl) {
        this.plugin = _pl;
        this.islandsFile = new File(plugin.getDataFolder(), "islands.yml");
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
        }
    }        

    public boolean createNewIsland(UUID uuid) {
        if (islands.containsKey(uuid))
            return false;
        Island newIsland = new Island(uuid);
        islands.put(uuid, newIsland);
        saveIslands();
        return true;
    }

    public boolean deleteIsland(UUID uuid) {
        if (islands.containsKey(uuid)) {
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
                config.set(island.getOwnerUUID() + ".islandSpawn", island.getIslandSpawn());
            }
            config.save(islandsFile);
        } catch (IOException err) {
            plugin.getLogger().severe("Nie udalo sie zapisac islands.yml: " + err.getMessage());
        }
    }
}

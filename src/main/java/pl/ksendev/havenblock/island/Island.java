package pl.ksendev.havenblock.island;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Location;

public class Island implements Serializable {
    private final UUID ownerUUID;
    private Location islandSpawn;

    private Location corner1;
    private Location corner2;

    public Island(UUID uuid) {
        ownerUUID = uuid;
        islandSpawn = new Location(null, 0, 0, 0);
    }

    public void setIslandSpawn(Location loc) {this.islandSpawn = loc;}

    public UUID getOwnerUUID() {return this.ownerUUID;}
    public Location getIslandSpawn() { return this.islandSpawn; }
}

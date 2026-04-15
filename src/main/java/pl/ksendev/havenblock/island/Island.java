package pl.ksendev.havenblock.island;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

public class Island implements Serializable {
    private final UUID ownerUUID;
    private Location islandSpawn;
    
    // Grid system
    private int gridX;
    private int gridZ;
    private String schematicName;
    
    // Budowalna strefa (64x64 od środka)
    private Location buildableMinBlock;
    private Location buildableMaxBlock;

    private Map<UUID, IslandRoles> islandMembers = new HashMap<>();

    /*
        ROLE:
        Owner: Może wszystko
        Moderator: Niszczenie, budowanie, zarzadzanie spawnem, wyrzucanie graczy
        Member: Niszczenie, budowanie
        Visitor: Nic
    */

    public Island(UUID uuid) {
        this.ownerUUID = uuid;
        this.islandSpawn = new Location(null, 0, 0, 0);
        this.gridX = 0;
        this.gridZ = 0;
        this.schematicName = "";
        this.islandMembers.put(uuid, IslandRoles.Owner);
    }

    public void setIslandSpawn(Location loc) {this.islandSpawn = loc;}
    public void setGridPosition(int gridX, int gridZ) {
        this.gridX = gridX;
        this.gridZ = gridZ;
    }
    public void setSchematicName(String name) { this.schematicName = name; }
    public void setBuildableRegion(Location min, Location max) {
        this.buildableMinBlock = min;
        this.buildableMaxBlock = max;
    }
    public void addToIslandMembers(UUID uuid, IslandRoles role) {
        this.islandMembers.put(uuid, role);
    }
    public void removeFromIslandMembers(UUID uuid) {
        this.islandMembers.remove(uuid);
    }

    public UUID getOwnerUUID() {return this.ownerUUID;}
    public Location getIslandSpawn() { return this.islandSpawn; }
    public int getGridX() { return this.gridX; }
    public int getGridZ() { return this.gridZ; }
    public String getSchematicName() { return this.schematicName; }
    public Location getBuildableMinBlock() { return this.buildableMinBlock; }
    public Location getBuildableMaxBlock() { return this.buildableMaxBlock; }
    public Map<UUID, IslandRoles> getIslandMembers() { return this.islandMembers; }
}
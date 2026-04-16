package pl.ksendev.havenblock.island;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class IslandBuilder {
    private static final int ISLAND_SIZE = 256;           // Rozmiar wyspy
    private static final int PADDING = 10;                // Padding między wyspami
    private static final int GRID_SPACING = ISLAND_SIZE + PADDING; // 266
    private static final int BUILDABLE_SIZE = 32;         // Pogranicze budowania 64x64
    private static final int ISLAND_HEIGHT = 64;          // Wysokość na której się pojawiają wyspy

    private final World world;

    public IslandBuilder(World world) {
        this.world = world;
    }

    /**
     * Oblicza rzeczywistą pozycję na świecie na podstawie grid indeksu
     * @param gridX indeks siatki X
     * @param gridZ indeks siatki Z
     * @return środek wyspy w koordinatach świata
     */
    public Location calculateIslandCenter(int gridX, int gridZ) {
        int worldX = gridX * GRID_SPACING;
        int worldZ = gridZ * GRID_SPACING;
        return new Location(world, worldX + ISLAND_SIZE / 2, ISLAND_HEIGHT, worldZ + ISLAND_SIZE / 2);
    }

    /**
     * Oblicza region budowalny (64x64 od środka wyspy)
     * @param islandCenter środek wyspy
     * @return tablica [minX, maxX, minZ, maxZ]
     */
    public int[] calculateBuildableRegion(Location islandCenter) {
        int centerX = (int) islandCenter.getX();
        int centerZ = (int) islandCenter.getZ();
        
        int minX = centerX - BUILDABLE_SIZE / 2;
        int maxX = centerX + BUILDABLE_SIZE / 2;
        int minZ = centerZ - BUILDABLE_SIZE / 2;
        int maxZ = centerZ + BUILDABLE_SIZE / 2;
        
        return new int[]{minX, maxX, minZ, maxZ};
    }

    /**
     * Tworzy bazę terenu 256x256 (void - powietrze wszędzie)
     * @param gridX indeks siatki X
     * @param gridZ indeks siatki Z
     */
    public void createIslandTerrain(int gridX, int gridZ) {
        Location center = calculateIslandCenter(gridX, gridZ);
        int centerX = (int) center.getX();
        int centerZ = (int) center.getZ();
        
        int startX = centerX - ISLAND_SIZE / 2;
        int startZ = centerZ - ISLAND_SIZE / 2;
        
        // Czyszczenie całego obszaru wyspy (void)
        for (int x = startX; x < startX + ISLAND_SIZE; x++) {
            for (int z = startZ; z < startZ + ISLAND_SIZE; z++) {
                for (int y = 0; y < 256; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    /**
     * Sprawdza czy gracz jest w strefie budowania wyspy
     * @param island wyspa
     * @param location pozycja gracza
     * @return true jeśli gracz może budować
     */
    public boolean isInBuildableRegion(Island island, Location location) {
        if (!location.getWorld().equals(world)) {
            return false;
        }
        
        int[] region = calculateBuildableRegion(island.getIslandSpawn());
        int minX = region[0];
        int maxX = region[1];
        int minZ = region[2];
        int maxZ = region[3];
        
        int x = (int) location.getX();
        int z = (int) location.getZ();
        
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public int getGridSpacing() {
        return GRID_SPACING;
    }

    public int getIslandSize() {
        return ISLAND_SIZE;
    }

    public int getIslandHeight() {
        return ISLAND_HEIGHT;
    }

    public int getBuildableSize() {
        return BUILDABLE_SIZE;
    }
}

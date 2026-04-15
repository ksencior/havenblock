package pl.ksendev.havenblock.island;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import pl.ksendev.havenblock.HavenBlock;

public class IslandGenerator {
    private final HavenBlock plugin;
    private final IslandBuilder islandBuilder;
    private final SchematicManager schematicManager;
    private final World world;
    private int nextGridX = 1;
    private int nextGridZ = 0;

    public IslandGenerator(HavenBlock plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        this.islandBuilder = new IslandBuilder(world);
        this.schematicManager = new SchematicManager(plugin);
    }

    /**
     * Tworzy kompletną nową wyspę z schematu
     * @param island obiekt wyspy
     * @return true jeśli powodzenie
     */
    public boolean generateIsland(Island island) {
        // Losowy schemat
        String randomSchematic = schematicManager.getRandomSchematic();
        if (randomSchematic == null) {
            plugin.getLogger().severe("Brak dostępnych schematów!");
            return false;
        }

        // Obliczenie pozycji w gridzie
        int gridX = nextGridX;
        int gridZ = nextGridZ;
        advanceGridPosition();

        // Ustawienie pozycji w wysypie
        island.setGridPosition(gridX, gridZ);
        island.setSchematicName(randomSchematic);

        // Obliczenie środka wyspy
        Location islandCenter = islandBuilder.calculateIslandCenter(gridX, gridZ);

        // Ustawienie spawn punktu
        islandCenter.setY(islandBuilder.getIslandHeight() + 1);
        island.setIslandSpawn(islandCenter);

        // Tworzenie terenu (podstawa 256x256)
        World world = islandCenter.getWorld();
        islandBuilder.createIslandTerrain(gridX, gridZ);

        // Wklejanie schematu w środek
        boolean schematicSuccess = schematicManager.pasteSchematic(world, islandCenter, randomSchematic);
        if (!schematicSuccess) {
            plugin.getLogger().warning("Nie udało się wkleić schematu dla wyspy gracza " + island.getOwnerUUID());
            return false;
        }

        // Obliczenie regionu budowalnego
        int[] buildableRegion = islandBuilder.calculateBuildableRegion(islandCenter);
        Location minBlock = new Location(world, buildableRegion[0], islandBuilder.getIslandHeight(), buildableRegion[2]);
        Location maxBlock = new Location(world, buildableRegion[1], 255, buildableRegion[3]);
        island.setBuildableRegion(minBlock, maxBlock);

        // Szukanie spawn punktu - zlokalizowanie SEA_LANTERN w schemacie
        Location spawnLocation = findSpawnLocation(world, islandCenter, buildableRegion);
        if (spawnLocation != null) {
            island.setIslandSpawn(spawnLocation);
        }

        return true;
    }

    /**
     * Szuka bloku SEA_LANTERN w obszarze budowalnym i ustawia spawn nad nim
     * @param world świat
     * @param islandCenter środek wyspy
     * @param buildableRegion tablica [minX, maxX, minZ, maxZ]
     * @return Location spawn punktu lub null jeśli nie znaleziono
     */
    private Location findSpawnLocation(World world, Location islandCenter, int[] buildableRegion) {
        int minX = buildableRegion[0];
        int maxX = buildableRegion[1];
        int minZ = buildableRegion[2];
        int maxZ = buildableRegion[3];
        
        // Szukamy od góry w dół (od y=255 do y=0)
        for (int y = 255; y >= 0; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (world.getBlockAt(x, y, z).getType() == Material.SEA_LANTERN) {
                        // Znaleźliśmy sea_lantern - spawn o jeden blok wyżej
                        Location spawnLoc = new Location(world, x + 0.5, y + 1, z + 0.5);
                        spawnLoc.setPitch(0);
                        spawnLoc.setYaw(0);
                        plugin.getLogger().info("Znaleziony spawn punkt SEA_LANTERN: " + x + ", " + y + ", " + z);
                        return spawnLoc;
                    }
                }
            }
        }
        
        plugin.getLogger().warning("Nie znaleziono bloku SEA_LANTERN w schemacie, używam centrum wyspy");
        Location fallback = new Location(world, islandCenter.getX(), islandCenter.getY() + 1, islandCenter.getZ());
        fallback.setPitch(0);
        fallback.setYaw(0);
        return fallback;
    }

    public boolean deleteIsland(Island island) {
        int gridX = island.getGridX();
        int gridZ = island.getGridZ();
        
        // Obliczenie środka wyspy
        Location center = islandBuilder.calculateIslandCenter(gridX, gridZ);
        int centerX = (int) center.getX();
        int centerZ = (int) center.getZ();
        
        // Obliczenie granic obszaru 256x256
        int startX = centerX - islandBuilder.getIslandSize() / 2;
        int startZ = centerZ - islandBuilder.getIslandSize() / 2;
        int endX = startX + islandBuilder.getIslandSize();
        int endZ = startZ + islandBuilder.getIslandSize();
        
        // Wypełnianie całego obszaru powietrzem (od y=0 do y=256)
        for (int x = startX; x < endX; x++) {
            for (int z = startZ; z < endZ; z++) {
                for (int y = 0; y < 256; y++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
        
        plugin.getLogger().info("Wyspa gracza " + island.getOwnerUUID() + " została usunięta (obszar wyczyszczony)");
        return true;
    }

    /**
     * Przesuwamy się do następnej pozycji w gridzie
     * Format: najpierw X, potem Z (rząd po rzędzie)
     */
    private void advanceGridPosition() {
        nextGridX++;
        // Jeśli osiągnęliśmy edge (arbitralnie 10 wysep na szerokość), przejdź do następnego wiersza
        if (nextGridX >= 20) {
            nextGridX = 1;
            nextGridZ++;
        }
    }

    /**
     * Zwraca czy gracz może budować w tej lokacji
     */
    public boolean canPlayerBuild(Island island, Location location) {
        return islandBuilder.isInBuildableRegion(island, location);
    }

    /**
     * Zwraca dostępne schematy
     */
    public java.util.List<String> getAvailableSchematics() {
        return schematicManager.getAvailableSchematics();
    }

    public IslandBuilder getIslandBuilder() {
        return islandBuilder;
    }

    public SchematicManager getSchematicManager() {
        return schematicManager;
    }
}

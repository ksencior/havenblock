package pl.ksendev.havenblock.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

import pl.ksendev.havenblock.HavenBlock;
import pl.ksendev.havenblock.island.Island;
import pl.ksendev.havenblock.island.IslandRoles;

public class DatabaseManager {
    private final HavenBlock plugin;
    private final File databaseFile;
    private Connection connection;

    public DatabaseManager(HavenBlock plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "islands.db");
        initializeDatabase();
    }

    /**
     * Inicjalizuje bazę danych SQLite i tworzy tabele jeśli nie istnieją
     */
    private void initializeDatabase() {
        try {
            // Łączenie z bazą
            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            
            plugin.getLogger().info("Połączono z bazą danych: " + databaseFile.getName());

            // Tworzenie tabeli islands jeśli nie istnieje
            String createTableSQL = "CREATE TABLE IF NOT EXISTS islands (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "grid_x INTEGER NOT NULL, " +
                    "grid_z INTEGER NOT NULL, " +
                    "schematic_name TEXT NOT NULL, " +
                    "spawn_x REAL NOT NULL, " +
                    "spawn_y REAL NOT NULL, " +
                    "spawn_z REAL NOT NULL, " +
                    "spawn_world TEXT NOT NULL, " +
                    "buildable_min_x REAL, " +
                    "buildable_min_y REAL, " +
                    "buildable_min_z REAL, " +
                    "buildable_max_x REAL, " +
                    "buildable_max_y REAL, " +
                    "buildable_max_z REAL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
                plugin.getLogger().info("Tabela 'islands' zainicjalizowana");
            }

            // Tworzenie tabeli island_members (członkowie wysp)
            String createMembersTableSQL = "CREATE TABLE IF NOT EXISTS island_members (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "island_uuid TEXT NOT NULL, " +
                    "member_uuid TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'MEMBER', " +
                    "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (island_uuid) REFERENCES islands(uuid) ON DELETE CASCADE, " +
                    "UNIQUE(island_uuid, member_uuid)" +
                    ")";

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createMembersTableSQL);
                plugin.getLogger().info("Tabela 'island_members' zainicjalizowana");
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd inicjalizacji bazy danych: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Zapisuje wyspę do bazy danych
     */
    public boolean saveIsland(Island island) {
        String sql = "INSERT OR REPLACE INTO islands " +
                "(uuid, grid_x, grid_z, schematic_name, spawn_x, spawn_y, spawn_z, spawn_world, " +
                "buildable_min_x, buildable_min_y, buildable_min_z, buildable_max_x, buildable_max_y, buildable_max_z) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, island.getOwnerUUID().toString());
            pstmt.setInt(2, island.getGridX());
            pstmt.setInt(3, island.getGridZ());
            pstmt.setString(4, island.getSchematicName());

            Location spawn = island.getIslandSpawn();
            if (spawn != null) {
                pstmt.setDouble(5, spawn.getX());
                pstmt.setDouble(6, spawn.getY());
                pstmt.setDouble(7, spawn.getZ());
                pstmt.setString(8, spawn.getWorld() != null ? spawn.getWorld().getName() : "world");
            } else {
                pstmt.setDouble(5, 0);
                pstmt.setDouble(6, 64);
                pstmt.setDouble(7, 0);
                pstmt.setString(8, "world");
            }

            Location minBlock = island.getBuildableMinBlock();
            Location maxBlock = island.getBuildableMaxBlock();
            if (minBlock != null) {
                pstmt.setDouble(9, minBlock.getX());
                pstmt.setDouble(10, minBlock.getY());
                pstmt.setDouble(11, minBlock.getZ());
            } else {
                pstmt.setNull(9, java.sql.Types.REAL);
                pstmt.setNull(10, java.sql.Types.REAL);
                pstmt.setNull(11, java.sql.Types.REAL);
            }

            if (maxBlock != null) {
                pstmt.setDouble(12, maxBlock.getX());
                pstmt.setDouble(13, maxBlock.getY());
                pstmt.setDouble(14, maxBlock.getZ());
            } else {
                pstmt.setNull(12, java.sql.Types.REAL);
                pstmt.setNull(13, java.sql.Types.REAL);
                pstmt.setNull(14, java.sql.Types.REAL);
            }

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd zapisywania wyspy: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Ładuje wyspę z bazy danych
     */
    public Island loadIsland(UUID uuid, World world) {
        String sql = "SELECT * FROM islands WHERE uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Island island = new Island(uuid);
                
                island.setGridPosition(rs.getInt("grid_x"), rs.getInt("grid_z"));
                island.setSchematicName(rs.getString("schematic_name"));

                // Ładowanie spawn punktu
                Location spawn = new Location(
                        world,
                        rs.getDouble("spawn_x"),
                        rs.getDouble("spawn_y"),
                        rs.getDouble("spawn_z")
                );
                island.setIslandSpawn(spawn);

                // Ładowanie regionu budowalnego
                double minX = rs.getDouble("buildable_min_x");
                double minY = rs.getDouble("buildable_min_y");
                double minZ = rs.getDouble("buildable_min_z");
                double maxX = rs.getDouble("buildable_max_x");
                double maxY = rs.getDouble("buildable_max_y");
                double maxZ = rs.getDouble("buildable_max_z");

                if (!rs.wasNull()) {
                    Location minBlock = new Location(world, minX, minY, minZ);
                    Location maxBlock = new Location(world, maxX, maxY, maxZ);
                    island.setBuildableRegion(minBlock, maxBlock);
                }

                return island;
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd ładowania wyspy: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Usuwa wyspę z bazy danych
     */
    public boolean deleteIsland(UUID uuid) {
        String sql = "DELETE FROM islands WHERE uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd usuwania wyspy: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sprawdza czy gracz ma wyspę
     */
    public boolean hasIsland(UUID uuid) {
        String sql = "SELECT uuid FROM islands WHERE uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd sprawdzania wyspy: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Dodaje członka do wyspy
     */
    public boolean addMember(UUID islandUUID, UUID memberUUID, String role) {
        String sql = "INSERT OR REPLACE INTO island_members (island_uuid, member_uuid, role) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, islandUUID.toString());
            pstmt.setString(2, memberUUID.toString());
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd dodawania członka: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Usuwa członka z wyspy
     */
    public boolean removeMember(UUID islandUUID, UUID memberUUID) {
        String sql = "DELETE FROM island_members WHERE island_uuid = ? AND member_uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, islandUUID.toString());
            pstmt.setString(2, memberUUID.toString());
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd usuwania członka: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Pobiera wyspę na podstawie członka (gracz jest na czyjeś wysie)
     */
    public Island getIslandByMember(UUID memberUUID, World world) {
        String sql = "SELECT island_uuid FROM island_members WHERE member_uuid = ? LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, memberUUID.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                UUID islandUUID = UUID.fromString(rs.getString("island_uuid"));
                return loadIsland(islandUUID, world);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd pobierania wyspy członka: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Ładuje członków wyspy do mapy
     */
    public void loadIslandMembers(Island island) {
        String sql = "SELECT member_uuid, role FROM island_members WHERE island_uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, island.getOwnerUUID().toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                UUID memberUUID = UUID.fromString(rs.getString("member_uuid"));
                String role = rs.getString("role");
                island.getIslandMembers().put(memberUUID, IslandRoles.valueOf(role));
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd ładowania członków: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Zamyka połączenie z bazą danych
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Zamknięto połączenie z bazą danych");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Błąd zamykania bazy danych: " + e.getMessage());
        }
    }
}

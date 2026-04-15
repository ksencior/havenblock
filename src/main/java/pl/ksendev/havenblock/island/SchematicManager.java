package pl.ksendev.havenblock.island;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

import pl.ksendev.havenblock.HavenBlock;

public class SchematicManager {
    private final HavenBlock plugin;
    private final File schematicsFolder;
    private final List<String> availableSchematics;
    private final Random random;

    public SchematicManager(HavenBlock plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        this.availableSchematics = new ArrayList<>();
        this.random = new Random();

        // Tworzenie folderu jeśli nie istnieje
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }

        loadAvailableSchematics();
    }

    /**
     * Ładuje listę dostępnych schematów z folderu
     */
    private void loadAvailableSchematics() {
        availableSchematics.clear();
        File[] files = schematicsFolder.listFiles((dir, name) -> 
            name.endsWith(".schem") || name.endsWith(".schematic"));

        if (files != null) {
            for (File file : files) {
                // Usuwa rozszerzenie
                String name = file.getName().replaceAll("\\.(schem|schematic)$", "");
                availableSchematics.add(name);
                plugin.getLogger().info("Załadowany schemat: " + name);
            }
        }

        if (availableSchematics.isEmpty()) {
            plugin.getLogger().warning("Nie znaleziono żadnych schematów! Umieść .schem pliki w plugins/havenblock/schematics/");
        }
    }

    /**
     * Zwraca losowy schemat z dostępnych
     * @return nazwa schematu lub null jeśli brak schematów
     */
    public String getRandomSchematic() {
        if (availableSchematics.isEmpty()) {
            return null;
        }
        return availableSchematics.get(random.nextInt(availableSchematics.size()));
    }

    /**
     * Wkleja schemat w podaną lokację
     * @param world świat
     * @param location lokacja wklejenia (środek)
     * @param schematicName nazwa schematu bez rozszerzenia
     * @return true jeśli powodzenie
     */
    public boolean pasteSchematic(World world, Location location, String schematicName) {
        try {
            // Szukanie pliku
            File schematicFile = findSchematicFile(schematicName);
            if (schematicFile == null || !schematicFile.exists()) {
                plugin.getLogger().severe("Schemat nie znaleziony: " + schematicName);
                return false;
            }

            // Wczytanie schematu
            FileInputStream fis = new FileInputStream(schematicFile);
            Clipboard clipboard = ClipboardFormats.findByFile(schematicFile)
                    .getReader(fis)
                    .read();
            fis.close();

            // Konwersja lokacji
            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
            
            // Kompilacja sesji edycji
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                // Obliczanie offsetu - umieszczenie schematu w centrum
                int offsetX = (int) location.getX() - (clipboard.getDimensions().getX() / 2);
                int offsetZ = (int) location.getZ() - (clipboard.getDimensions().getZ() / 2);
                int offsetY = (int) location.getY();

                BlockVector3 to = BlockVector3.at(offsetX, offsetY, offsetZ);

                // Tworzenie operacji
                ClipboardHolder holder = new ClipboardHolder(clipboard);
                Operation operation = holder
                        .createPaste(editSession)
                        .to(to)
                        .ignoreAirBlocks(false)
                        .build();

                Operations.complete(operation);
            } catch (Exception e) {
                plugin.getLogger().severe(e.getMessage());
            }

            plugin.getLogger().info("Schemat wklejony: " + schematicName + " na " + location);
            return true;

        } catch (IOException e) {
            plugin.getLogger().severe("Błąd przy wczytywaniu schematu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Szuka pliku schematu (obsługuje .schem i .schematic)
     * @param schematicName nazwa bez rozszerzenia
     * @return File lub null
     */
    private File findSchematicFile(String schematicName) {
        File schem = new File(schematicsFolder, schematicName + ".schem");
        if (schem.exists()) {
            return schem;
        }

        File schematic = new File(schematicsFolder, schematicName + ".schematic");
        if (schematic.exists()) {
            return schematic;
        }

        return null;
    }

    /**
     * Zwraca listę wszystkich dostępnych schematów
     */
    public List<String> getAvailableSchematics() {
        return new ArrayList<>(availableSchematics);
    }

    /**
     * Przeładowuje listę schematów
     */
    public void reloadSchematics() {
        loadAvailableSchematics();
    }
}

package pl.ksendev.havenblock.protection;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Player;

import pl.ksendev.havenblock.HavenBlock;
import pl.ksendev.havenblock.island.Island;
import pl.ksendev.havenblock.island.IslandRoles;
import pl.ksendev.havenblock.utils.MessageUtils;

public class BuildProtectionListener implements Listener {
    private final HavenBlock plugin;

    public BuildProtectionListener(HavenBlock plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location blockLoc = event.getBlock().getLocation();

        // Sprawdzanie czy gracz ma wyspę i może budować
        if (!canPlayerBuild(player, blockLoc)) {
            event.setCancelled(true);
            MessageUtils.sendMessage(player, "<red>Nie możesz tu kopać!", true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location blockLoc = event.getBlock().getLocation();

        // Sprawdzanie czy gracz ma wyspę i może budować
        if (!canPlayerBuild(player, blockLoc)) {
            event.setCancelled(true);
            MessageUtils.sendMessage(player, "<red>Nie możesz tu stawiać!", true);
        }
    }

    /**
     * Sprawdza czy gracz może budować w podanej lokacji
     */
    private boolean canPlayerBuild(Player player, Location location) {
        // Pobieranie wyspy gracza
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        
        if (island == null) {
            return false;  // Gracz nie ma wyspy
        }

        // Sprawdzanie czy blok jest w buildable region
        if (!isInBuildableRegion(island, location)) {
            return false;  // Blok poza regionem budowania
        }

        // Sprawdzanie roli gracza na wysie
        IslandRoles role = island.getIslandMembers().get(player.getUniqueId());
        if (role == null) {
            return false;  // Gracz nie jest na liście członków
        }

        // VISITOR nie może budować, wszyscy inni mogą
        if (role == IslandRoles.Visitor) {
            return false;
        }

        return true;
    }

    /**
     * Sprawdza czy blok jest w regionie budowalnym wyspy
     */
    private boolean isInBuildableRegion(Island island, Location location) {
        Location minBlock = island.getBuildableMinBlock();
        Location maxBlock = island.getBuildableMaxBlock();

        if (minBlock == null || maxBlock == null) {
            return false;
        }

        // Sprawdzanie świata
        if (!location.getWorld().equals(minBlock.getWorld())) {
            return false;
        }

        // Sprawdzanie koordynatów XYZ
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        double minX = Math.min(minBlock.getX(), maxBlock.getX());
        double maxX = Math.max(minBlock.getX(), maxBlock.getX());
        double minY = Math.min(minBlock.getY(), maxBlock.getY());
        double maxY = Math.max(minBlock.getY(), maxBlock.getY());
        double minZ = Math.min(minBlock.getZ(), maxBlock.getZ());
        double maxZ = Math.max(minBlock.getZ(), maxBlock.getZ());

        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
}

package pl.ksendev.havenblock.gui;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import pl.ksendev.havenblock.HavenBlock;
import pl.ksendev.havenblock.island.Island;
import pl.ksendev.havenblock.utils.GuiUtils;

public class IslandGui implements Listener {
    private final HavenBlock plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final String MAIN_TITLE = "Twoja wyspa";

    public IslandGui(HavenBlock _pl) {
        this.plugin = _pl;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, Component.text(MAIN_TITLE));

        inv.setItem(1, GuiUtils.createItem(Material.IRON_DOOR, 
            "<yellow>Zarządzaj członkami", 
            List.of("<gray>Zarządzaj członkami twojej wyspy")));

        inv.setItem(4, GuiUtils.createItem(Material.GRASS_BLOCK, 
            "<yellow>Teleportuj", 
            List.of("<gray>Teleportuj się na swoją wyspę")));

        inv.setItem(7, GuiUtils.createItem(Material.REDSTONE, 
            "<yellow>Ustawienia wyspy", 
            List.of("<gray>Zarządzaj ustawieniami twojej wyspy")));

        ItemStack filler = GuiUtils.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of(" "));
        for (int i = 0; i < inv.getSize(); i++)
            if (inv.getItem(i) == null) inv.setItem(i, filler);
        
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component titleComponent = event.getView().title();
        String title = mm.serialize(titleComponent).replace("\\<", "<").replace("\\>", ">");

        boolean isMainMenu = event.getView().getTitle().contains(MAIN_TITLE);

        if (!isMainMenu) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType().name().contains("GLASS_PANE")) return;
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) return;
        if (isMainMenu) {
            if (clicked.getType() == Material.GRASS_BLOCK)
                player.teleport(island.getIslandSpawn());
        }
    }
}

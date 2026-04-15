package pl.ksendev.havenblock.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import pl.ksendev.havenblock.HavenBlock;
import pl.ksendev.havenblock.island.Island;
import pl.ksendev.havenblock.utils.MessageUtils;

public class MainCommand implements CommandExecutor {
    private final HavenBlock plugin;

    public MainCommand(HavenBlock _plugin) {
        this.plugin = _plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player))
            return true;
        

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("create")) {
            boolean success = plugin.getIslandManager().createNewIsland(player.getUniqueId());
            if (success) {
                MessageUtils.sendMessage(sender, "<green>Stworzono wyspe!", true);
                
                // Teleportowanie gracza do wyspy
                Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                if (island != null && island.getIslandSpawn() != null) {
                    player.teleport(island.getIslandSpawn());
                    MessageUtils.sendMessage(player, "<green>Teleportowano do wyspy!", true);
                }
            } else {
                MessageUtils.sendMessage(sender, "<red>Masz juz wyspe!", true);
            }
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("delete")) {
            boolean success = plugin.getIslandManager().deleteIsland(player.getUniqueId());
            if (success) {
                MessageUtils.sendMessage(sender, "<green>Usunieto wyspe!", true);
                player.teleport(plugin.getIslandManager().getLobbyLocation());
            }
            else
                MessageUtils.sendMessage(sender, "<red>Nie masz wyspy!", true);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("home")) {
            Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
            if (island == null) {
                MessageUtils.sendMessage(player, "<red>Nie masz wyspy! Uzyj /hb create", true);
                return true;
            }
            if (island.getIslandSpawn() == null) {
                MessageUtils.sendMessage(player, "<red>Wyspa nie ma spawn punktu!", true);
                return true;
            }
            player.teleport(island.getIslandSpawn());
            MessageUtils.sendMessage(player, "<green>Teleportowano do domu!", true);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.getIslandManager().reloadIslands();
            MessageUtils.sendMessage(sender, "<green>OK!", true);  
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("admin") && player.isOp()) {
            if (args[1].equalsIgnoreCase("setspawn")) {
                plugin.getIslandManager().setLobbyLocation(player);
                MessageUtils.sendMessage(player, "<green>Ustawiono nową lokalizację lobby!", true);
                return true;
            }
        }
        
        return false;
    }

    private void showHelp(Player player) {
        MessageUtils.sendMessage(player, "-------------------", false);
        MessageUtils.sendMessage(player, "/hb create - Tworzy wyspe", false);
        MessageUtils.sendMessage(player, "/hb home - Teleportuje do wyspy", false);
        MessageUtils.sendMessage(player, "/hb delete - Usuwa wyspe", false);
        MessageUtils.sendMessage(player, "/hb invite - Zaprasza gracza do wspoltworzenia wyspy", false);
        MessageUtils.sendMessage(player, "/hb kick - Wyrzuca gracza z wyspy", false);
        MessageUtils.sendMessage(player, "-------------------", false);
    }
}

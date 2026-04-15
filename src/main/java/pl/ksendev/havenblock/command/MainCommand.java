package pl.ksendev.havenblock.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import pl.ksendev.havenblock.HavenBlock;
import pl.ksendev.havenblock.island.Island;
import pl.ksendev.havenblock.island.IslandRoles;
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

        if (args.length == 1 && args[0].equalsIgnoreCase("setspawn")) {
            Location playerLocation = player.getLocation();
            Island playerIsland = plugin.getIslandManager().getIsland(player.getUniqueId());
            if (playerIsland == null) {
                MessageUtils.sendMessage(player, "<red>Nie masz wyspy! Uzyj /hb create", true);
                return true;
            }
            playerIsland.setIslandSpawn(playerLocation);
            MessageUtils.sendMessage(player, "<green>Ustawiono nowy spawn wyspy.", true);
            return true;

        }

        if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
            Island playerIsland = plugin.getIslandManager().getIsland(player.getUniqueId());
            if (playerIsland == null) {
                MessageUtils.sendMessage(player, "<red>Nie masz wyspy! Uzyj /hb create", true);
                return true;
            }
            if (playerIsland.getIslandMembers().get(player.getUniqueId()) != IslandRoles.Owner) {
                MessageUtils.sendMessage(player, "<red>Nie masz permisji, by zapraszać innych graczy", true);
                return true;
            }
            OfflinePlayer invitedOfflinePlayer = Bukkit.getOfflinePlayer(args[1]);
            if (!invitedOfflinePlayer.isOnline()) {
                MessageUtils.sendMessage(player, "<red>Gracz o nicku <yellow>" + args[1] + "</yellow> nie jest online!", true);
                return true;
            }
            Player invitedPlayer = Bukkit.getPlayer(invitedOfflinePlayer.getUniqueId());
            if (invitedPlayer == player) {
                MessageUtils.sendMessage(player, "<red>Nie możesz zaprosić siebie!", true);
                return true;
            }
            if (plugin.getIslandManager().getIsland(invitedPlayer.getUniqueId()) != null) {
                MessageUtils.sendMessage(player, "<red>Gracz <yellow>" + invitedPlayer.getName() + "</yellow> ma już wyspę!", true);
                return true;
            }
            plugin.getInvitationManager().sendInvite(player, invitedPlayer);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("accept")) {
            plugin.getInvitationManager().acceptInvite(player);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("deny")) {
            plugin.getInvitationManager().denyInvite(player);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("kick")) {
            Island playerIsland = plugin.getIslandManager().getIsland(player.getUniqueId());
            if (playerIsland == null) {
                MessageUtils.sendMessage(player, "<red>Nie masz wyspy! Uzyj /hb create", true);
                return true;
            }
            if (playerIsland.getIslandMembers().get(player.getUniqueId()) != IslandRoles.Owner) {
                MessageUtils.sendMessage(player, "<red>Nie masz permisji, by wyrzucać graczy", true);
                return true;
            }
            OfflinePlayer kickingOfflinePlayer = Bukkit.getOfflinePlayer(args[1]);
            Player kickingPlayer = Bukkit.getPlayer(kickingOfflinePlayer.getUniqueId());
            if (kickingPlayer == player) {
                MessageUtils.sendMessage(player, "<red>Nie możesz wyrzucić samego siebie!", true);
                return true;
            }
            if (!playerIsland.getIslandMembers().containsKey(kickingPlayer.getUniqueId())) {
                MessageUtils.sendMessage(player, "<red>Ten gracz nie jest dodany do twojej wyspy!", true);
                return true;
            }
            plugin.getIslandManager().kickPlayerFromIsland(playerIsland, kickingPlayer.getUniqueId());
            MessageUtils.sendMessage(player, "<green>Wyrzucono gracza <yellow>" + kickingPlayer.getName() + "</yellow> z wyspy", true);
            MessageUtils.sendMessage(kickingPlayer, "<yellow>Zostałeś wyrzucony z wyspy.", true);
            kickingPlayer.teleport(plugin.getIslandManager().getLobbyLocation());
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
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
        showHelp(player);
        return true;
    }

    private void showHelp(Player player) {
        MessageUtils.sendMessage(player, "<dark_gray>-------------------", false);
        MessageUtils.sendMessage(player, "<yellow>/hb create <gray>- Tworzy wyspe", false);
        MessageUtils.sendMessage(player, "<yellow>/hb home <gray>- Teleportuje do wyspy", false);
        MessageUtils.sendMessage(player, "<yellow>/hb delete <gray>- Usuwa wyspe", false);
        MessageUtils.sendMessage(player, "<yellow>/hb invite [nick] <gray>- Zaprasza gracza do wspoltworzenia wyspy", false);
        MessageUtils.sendMessage(player, "<yellow>/hb kick [nick] <gray>- Wyrzuca gracza z wyspy", false);
        MessageUtils.sendMessage(player, "<dark_gray>-------------------", false);
    }
}

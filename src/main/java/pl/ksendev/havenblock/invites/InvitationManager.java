package pl.ksendev.havenblock.invites;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import pl.ksendev.havenblock.HavenBlock;
import pl.ksendev.havenblock.island.Island;
import pl.ksendev.havenblock.island.IslandRoles;
import pl.ksendev.havenblock.utils.MessageUtils;

public class InvitationManager {
    
    private HavenBlock plugin;
    private Map<Player, Player> invitations = new HashMap<>();
    public InvitationManager(HavenBlock _pl) {
        this.plugin = _pl;
    }

    public void sendInvite(Player sender, Player reciever) {
        invitations.put(reciever, sender);
        MessageUtils.sendMessage(reciever, "", false);
        MessageUtils.sendMessage(reciever, "<aqua>Gracz <yellow>" + sender.getName() + "</yellow> zaprasza cię do współtworzenia wyspy.", true);
        MessageUtils.sendMessage(reciever, "<yellow>/hb accept <aqua>- aby zaakceptować", false);
        MessageUtils.sendMessage(reciever, "<yellow>/hb deny <aqua>- aby odrzucić", false);
        MessageUtils.sendMessage(reciever, "<dark_gray>Zaproszenie wygasa za 15 sekund.", false);
        MessageUtils.sendMessage(reciever, "", false);

        MessageUtils.sendMessage(sender, "<green>Wysłano zaproszenie do gracza <yellow>" + reciever.getName(), true);
    }

    public void acceptInvite(Player invitedPlayer) {
        if (!invitations.containsKey(invitedPlayer)) {
            MessageUtils.sendMessage(invitedPlayer, "<red>Nie masz żadnych zaproszeń.", true);
            return;
        }

        Player islandOwner = invitations.get(invitedPlayer);
        Island island = plugin.getIslandManager().getIsland(islandOwner.getUniqueId());
        invitations.remove(invitedPlayer);
        island.addToIslandMembers(invitedPlayer.getUniqueId(), IslandRoles.Member);
        MessageUtils.sendMessage(invitedPlayer, "<green>Dołączono do wyspy gracza <yellow>" + islandOwner.getName(), true);
        if (islandOwner.isOnline())
            MessageUtils.sendMessage(islandOwner, "<green>Gracz <yellow>" + invitedPlayer.getName() + "</yellow> dołączył do twojej wyspy.", true);
    }

    public void denyInvite(Player invitedPlayer) {
        if (!invitations.containsKey(invitedPlayer)) {
            MessageUtils.sendMessage(invitedPlayer, "<red>Nie masz żadnych zaproszeń.", true);
            return;
        }
        Player islandOwner = invitations.get(invitedPlayer);
        invitations.remove(invitedPlayer);
        MessageUtils.sendMessage(invitedPlayer, "<yellow>Odrzucono prośbę o współtworzenie wyspy.", true);
        if (islandOwner.isOnline())
                MessageUtils.sendMessage(islandOwner, "<red>Gracz <yellow>" + invitedPlayer.getName() + "</yellow> odrzucił zaproszenie.", true);
    }
}

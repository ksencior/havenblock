package pl.ksendev.havenblock;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.ksendev.havenblock.command.MainCommand;
import pl.ksendev.havenblock.invites.InvitationManager;
import pl.ksendev.havenblock.island.IslandManager;

/*
TODO:
- Tworzenie wyspy [DONE]
- Dwa sposoby poslugiwania sie pluginem (GUI / Komendy)
- Tryb administratora
*/

public class HavenBlock extends JavaPlugin
{
  private static final Logger LOGGER=Logger.getLogger("havenblock");
  private File configFile;
  private IslandManager islandManager;
  private InvitationManager invitationManager;

  public void onEnable()
  {

    this.islandManager = new IslandManager(this);
    this.invitationManager = new InvitationManager(this);
    this.configFile = new File(this.getDataFolder(), "config.yml");
    loadConfig();
    PluginCommand hbCommand = getCommand("hb");
    hbCommand.setExecutor(new MainCommand(this));

    LOGGER.info("havenblock enabled");
  }

  public void onDisable()
  {
    // Zamykanie bazy danych
    if (islandManager != null && islandManager.getDatabaseManager() != null) {
      islandManager.getDatabaseManager().closeConnection();
    }
    LOGGER.info("havenblock disabled");
  }

  public void reloadConfig() {
    islandManager.reloadIslands();

    loadConfig();
  }

  public void loadConfig () {
    if (!configFile.exists())
        return;

    FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

    islandManager.setLobbyLocation(config.getLocation("spawn"));
  }

  public void saveConfig() {
    FileConfiguration config = new YamlConfiguration();
    try {
      config.set("spawn", islandManager.getLobbyLocation());

      config.save(configFile);
    } catch (IOException e) {
      LOGGER.severe(e.getMessage());
    }
  }

  public IslandManager getIslandManager() { return islandManager; }
  public InvitationManager getInvitationManager() { return invitationManager; }
}

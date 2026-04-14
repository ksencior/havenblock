package pl.ksendev.havenblock;

import java.util.logging.Logger;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import pl.ksendev.havenblock.command.MainCommand;
import pl.ksendev.havenblock.island.IslandManager;

/*
TODO:
- Tworzenie wyspy
- Dwa sposoby poslugiwania sie pluginem (GUI / Komendy)
- Tryb administratora
*/

public class HavenBlock extends JavaPlugin
{
  private static final Logger LOGGER=Logger.getLogger("havenblock");

  private IslandManager islandManager;

  public void onEnable()
  {

    this.islandManager = new IslandManager(this);

    PluginCommand hbCommand = getCommand("hb");
    hbCommand.setExecutor(new MainCommand(this));

    LOGGER.info("havenblock enabled");
  }

  public void onDisable()
  {
    LOGGER.info("havenblock disabled");
  }

  public IslandManager getIslandManager() {
    return islandManager;
  }
}

package pl.ksendev.havenblock.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class GuiUtils {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    
    public static ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            Component displayName;
            if (name.contains("<") && name.contains(">"))
                displayName = mm.deserialize(name);
            else
                displayName = legacy.deserialize(name);
            meta.displayName(displayName);

            if (lore != null) {
                meta.lore(lore.stream().map(line -> {
                    if (line.contains("<") && line.contains(">"))
                        return mm.deserialize(line);
                    else
                        return legacy.deserialize(line);
                }).collect(Collectors.toList()));
            }
            item.setItemMeta(meta);
        }

        return item;
    }
}

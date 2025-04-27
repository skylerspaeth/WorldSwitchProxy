package com.skylerspaeth.worldswitchproxy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class SpigotPlugin extends JavaPlugin implements Listener {

  @Override
  public void onEnable() {
    Bukkit.getMessenger().registerOutgoingPluginChannel(this, "worldswitchproxy:main");
    Bukkit.getPluginManager().registerEvents(this, this);
  }

  @Override
  public void onDisable() {
    Bukkit.getMessenger().unregisterOutgoingPluginChannel(this);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.getCurrentItem() != null) {

      if (ChatColor.stripColor(event.getView().getTitle()).equals("Server Selector")) {
        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item.hasItemMeta()) {
          String targetServer = ChatColor.stripColor(item.getItemMeta().getDisplayName()).replaceFirst("^● ", "");

          Player player = (Player) event.getWhoClicked();
          player.closeInventory();

          getLogger().info(String.format("Sending request to proxy to switch user '%s' to server '%s'...", player.getName(), targetServer));
          sendSwitchServerRequest(player, targetServer);
        }
      }
    }
  }

  private void sendSwitchServerRequest(Player player, String targetServer) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(out);

      dataOut.writeUTF("switchserver");
      dataOut.writeUTF(player.getUniqueId().toString());
      dataOut.writeUTF(targetServer);

      player.sendPluginMessage(this, "worldswitchproxy:main", out.toByteArray());
      getLogger().info(String.format("Sent request to proxy to switch user '%s' to server '%s'.", player.getName(), targetServer));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

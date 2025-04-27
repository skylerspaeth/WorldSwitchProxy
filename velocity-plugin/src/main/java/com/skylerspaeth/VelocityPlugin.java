package com.skylerspaeth.worldswitchproxy;

import org.slf4j.Logger;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.plugin.Plugin;

import com.google.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.Optional;

@Plugin(id = "worldswitchproxy", name = "World Switch Proxy", version = "1.0")
public class VelocityPlugin {
  private final Logger logger;

  private final ProxyServer server;
  private static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from("worldswitchproxy:main");

  @Inject
  public VelocityPlugin(ProxyServer server, Logger logger) {
    this.server = server;
    this.logger = logger;

    server.getChannelRegistrar().register(CHANNEL);
  }

  @Subscribe
  public void onPluginMessage(com.velocitypowered.api.event.connection.PluginMessageEvent event) {
    if (!event.getIdentifier().equals(CHANNEL)) return;
    if (!(event.getSource() instanceof ServerConnection)) return;

    DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));

    try {
      String subchannel = in.readUTF();
      if (!subchannel.equals("switchserver")) return;

      String uuid = in.readUTF();
      String targetServer = in.readUTF();

      logger.info(String.format("received request to send player with UUID '%s' to server '%s'", uuid, targetServer));
      Optional<Player> playerMatchingUuid = server.getPlayer(UUID.fromString(uuid));

      if (playerMatchingUuid.isPresent()) {
        Player activePlayer = playerMatchingUuid.get();
        logger.info(String.format("attempting transfer of user '%s' to server '%s'", activePlayer.getUsername(), targetServer));
        server.getServer(targetServer).ifPresent(target -> {
          activePlayer.createConnectionRequest(target).fireAndForget();
        });
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

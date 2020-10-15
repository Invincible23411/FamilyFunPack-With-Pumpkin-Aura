package family_fun_pack.modules;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.relauncher.Side;

import io.netty.buffer.ByteBuf;

import java.util.Set;
import java.util.HashSet;

import family_fun_pack.FamilyFunPack;
import family_fun_pack.network.PacketListener;

@SideOnly(Side.CLIENT)
public class StalkModule extends Module implements PacketListener {

  private static final TextFormatting ANNOUNCE_COLOR = TextFormatting.LIGHT_PURPLE;

  private Set<String> players;

  public StalkModule() {
    super("Stalk players", "See when given players connect/disconnect");
    this.players = new HashSet<String>();
  }

  protected void enable() {
    FamilyFunPack.getNetworkHandler().registerListener(EnumPacketDirection.CLIENTBOUND, this, 55);
  }

  protected void disable() {
    FamilyFunPack.getNetworkHandler().unregisterListener(EnumPacketDirection.CLIENTBOUND, this, 55);
  }

  public void addPlayer(String player) {
    this.players.add(player);
  }

  public void delPlayer(String player) {
    this.players.remove(player);
  }

  public Set<String> getPlayers() {
    return this.players;
  }

  public Packet<?> packetReceived(EnumPacketDirection direction, int id, Packet<?> packet, ByteBuf in) {
    SPacketPlayerListItem list = (SPacketPlayerListItem) packet;

    if(list.getAction() == SPacketPlayerListItem.Action.UPDATE_LATENCY) return packet;

    NetHandlerPlayClient hanlder = (NetHandlerPlayClient) FamilyFunPack.getNetworkHandler().getNetHandler();

    for(SPacketPlayerListItem.AddPlayerData entry : list.getEntries()) {

      String name = entry.getProfile().getName();
      if(name == null) {
        NetworkPlayerInfo info = hanlder.getPlayerInfo(entry.getProfile().getId());
        if(info == null) continue;
        name = info.getGameProfile().getName();
      }

      if(this.players.contains(name)) {
        switch(list.getAction()) {
          case ADD_PLAYER:
            {
              if(entry.getDisplayName() == null)
                FamilyFunPack.printMessage(StalkModule.ANNOUNCE_COLOR + "Player " + name + " joined [" + entry.getGameMode().getName() + "]");
              else FamilyFunPack.printMessage(StalkModule.ANNOUNCE_COLOR + "Player " + name + " joined under the name \"" + entry.getDisplayName().toString() + "\" [" + entry.getGameMode().getName() + "]");
            }
            break;
          case REMOVE_PLAYER:
            {
              FamilyFunPack.printMessage(StalkModule.ANNOUNCE_COLOR + "Player " + name + " disconnected");
            }
            break;
          case UPDATE_GAME_MODE:
            FamilyFunPack.printMessage(StalkModule.ANNOUNCE_COLOR + "Player " + name + " changed their game mode to " + entry.getGameMode().getName());
            break;
          case UPDATE_DISPLAY_NAME:
            if(entry.getDisplayName() == null)
              FamilyFunPack.printMessage(StalkModule.ANNOUNCE_COLOR + "Player " + name + " removed their custom display name");
            else FamilyFunPack.printMessage(StalkModule.ANNOUNCE_COLOR + "Player " + name + " changed their display name to \"" + entry.getDisplayName().toString() + "\"");
            break;
        }
      }

    }

    return packet;
  }

  public void save(Configuration configuration) {
    super.save(configuration);
    configuration.get(this.name, "players", new String[0]).set((String[])(this.players.toArray()));
  }

  public void load(Configuration configuration) {
    String[] array = configuration.get(this.name, "players", new String[0]).getStringList();
    for(String i : array) this.players.add(i);
    super.load(configuration);
  }
}

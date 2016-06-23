package com.mcf.davidee.nbtedit.packets;

import static com.mcf.davidee.nbtedit.NBTEdit.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.NBTHelper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.world.GameType;

public class EntityNBTPacket extends AbstractPacket {

	protected int entityID;
	protected NBTTagCompound tag;

	public EntityNBTPacket() {

	}

	public EntityNBTPacket(int entityID, NBTTagCompound tag) {
		this.entityID = entityID;
		this.tag = tag;
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {
		ByteBufOutputStream bos = new ByteBufOutputStream(buffer);
		bos.writeInt(entityID);
		NBTHelper.nbtWrite(tag, bos);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) throws IOException {
		ByteBufInputStream bis = new ByteBufInputStream(buffer);
		entityID = bis.readInt();
		DataInputStream dis = new DataInputStream(bis);
		tag = NBTHelper.nbtRead(dis);
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		NBTEdit.proxy.openEditGUI(entityID, tag);
	}

	// Fairly hacky. Consider swapping to an event driven system, where classes can register to
	// receive entity edit events and provide feedback/send packets as necessary.
	@Override
	public void handleServerSide(EntityPlayerMP player) {
		Entity e = player.worldObj.getEntityByID(entityID);
		if (e != null) {
			try {
				GameType preGameType = player.interactionManager.getGameType();
				e.readFromNBT(tag);
				NBTEdit.log(Level.FINE, player.getName() + " edited a tag -- Entity ID #" + entityID);
				NBTEdit.logTag(tag);
				if (e == player) { //Update player info
					player.sendContainerToPlayer(player.inventoryContainer);
					GameType type = player.interactionManager.getGameType();
					if (preGameType != type)
						player.setGameType(type);
					player.connection.sendPacket(new SPacketUpdateHealth(player.getHealth(), player.getFoodStats().getFoodLevel(), player.getFoodStats().getSaturationLevel()));
					player.connection.sendPacket(new SPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));
					player.sendPlayerAbilities();
				}
				sendMessageToPlayer(player, "Your changes have been saved");
			}
			catch(Throwable t) {
				sendMessageToPlayer(player, SECTION_SIGN + "cSave Failed - Invalid NBT format for Entity");
				NBTEdit.log(Level.WARNING, player.getName() + " edited a tag and caused an exception");
				NBTEdit.logTag(tag);
				NBTEdit.throwing("EntityNBTPacket", "handleServerSide", t);
			}
		}
	}

}

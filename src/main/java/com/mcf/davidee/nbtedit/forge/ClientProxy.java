package com.mcf.davidee.nbtedit.forge;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL11;

import com.mcf.davidee.nbtedit.NBTEdit;
import com.mcf.davidee.nbtedit.gui.GuiEditNBTTree;
import com.mcf.davidee.nbtedit.nbt.SaveStates;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerInformation(){
		MinecraftForge.EVENT_BUS.register(this);
		SaveStates save = NBTEdit.getSaveStates();
		save.load();
		save.save();
	}

	@Override
	public File getMinecraftDirectory(){
		return FMLClientHandler.instance().getClient().mcDataDir;
	}

	@Override
	public void openEditGUI(final int entityID, final NBTTagCompound tag) {
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				Minecraft.getMinecraft().displayGuiScreen(new GuiEditNBTTree(entityID, tag));
			}
		});
	}

	@Override
	public void openEditGUI(final BlockPos pos, final NBTTagCompound tag) {
		Minecraft.getMinecraft().addScheduledTask(new Runnable() {
			@Override
			public void run() {
				Minecraft.getMinecraft().displayGuiScreen(new GuiEditNBTTree(pos, tag));
			}
		});
	}

	@SubscribeEvent
	public void renderWorldLast(RenderWorldLastEvent event){
		GuiScreen curScreen = Minecraft.getMinecraft().currentScreen;
		if (curScreen instanceof GuiEditNBTTree){
			GuiEditNBTTree screen = (GuiEditNBTTree)curScreen;
			Entity e = screen.getEntity();

			if (e != null && e.isEntityAlive())
				drawBoundingBox(event.getContext(), event.getPartialTicks(), e.getEntityBoundingBox());
			else if (screen.isTileEntity()){
				int x = screen.getBlockX();
				int y = screen.y;
				int z = screen.z;
				World world = Minecraft.getMinecraft().theWorld;
				BlockPos pos = new BlockPos(x, y, z);
				IBlockState blockState = world.getBlockState(pos);

				if (blockState != null) {
					Block b = blockState.getBlock();
					if (b != null)
					{
						// TODO b.setBlockBoundsBasedOnState(world, pos); // kanomiya Removed: Necessary?
						drawBoundingBox(event.getContext(), event.getPartialTicks(), blockState.getSelectedBoundingBox(world, pos));
					}
				}
			}
		}
	}

	private void drawBoundingBox(RenderGlobal r, float f, AxisAlignedBB aabb) {
		if (aabb == null)
			return;

		Entity player = Minecraft.getMinecraft().getRenderViewEntity();

		double var8 = player.lastTickPosX + (player.posX - player.lastTickPosX) * f;
		double var10 = player.lastTickPosY + (player.posY - player.lastTickPosY) * f;
		double var12 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * f;

		aabb = aabb.addCoord(-var8, -var10, -var12);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1.0F, 0.0F, 0.0F, .5F);
		GL11.glLineWidth(3.5F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexBuffer = tessellator.getBuffer();

		vertexBuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
		vertexBuffer.pos(aabb.minX, aabb.minY, aabb.minZ);
		vertexBuffer.pos(aabb.maxX, aabb.minY, aabb.minZ);
		vertexBuffer.pos(aabb.maxX, aabb.minY, aabb.maxZ);
		vertexBuffer.pos(aabb.minX, aabb.minY, aabb.maxZ);
		vertexBuffer.pos(aabb.minX, aabb.minY, aabb.minZ);
		tessellator.draw();
		vertexBuffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
		vertexBuffer.pos(aabb.minX, aabb.maxY, aabb.minZ);
		vertexBuffer.pos(aabb.maxX, aabb.maxY, aabb.minZ);
		vertexBuffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ);
		vertexBuffer.pos(aabb.minX, aabb.maxY, aabb.maxZ);
		vertexBuffer.pos(aabb.minX, aabb.maxY, aabb.minZ);
		tessellator.draw();
		vertexBuffer.begin(1, DefaultVertexFormats.POSITION_COLOR);
		vertexBuffer.pos(aabb.minX, aabb.minY, aabb.minZ);
		vertexBuffer.pos(aabb.minX, aabb.maxY, aabb.minZ);
		vertexBuffer.pos(aabb.maxX, aabb.minY, aabb.minZ);
		vertexBuffer.pos(aabb.maxX, aabb.maxY, aabb.minZ);
		vertexBuffer.pos(aabb.maxX, aabb.minY, aabb.maxZ);
		vertexBuffer.pos(aabb.maxX, aabb.maxY, aabb.maxZ);
		vertexBuffer.pos(aabb.minX, aabb.minY, aabb.maxZ);
		vertexBuffer.pos(aabb.minX, aabb.maxY, aabb.maxZ);
		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

	}
}

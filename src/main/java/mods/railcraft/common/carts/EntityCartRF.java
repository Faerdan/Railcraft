/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.carts;

import buildcraft.api.core.BCLog;
import mods.railcraft.api.carts.ICartContentsTextureProvider;
import mods.railcraft.api.electricity.IElectricMinecart;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public final class EntityCartRF extends CartBase implements IElectricMinecart /*, ICartContentsTextureProvider */ {
    private static final int ENERGY_CAP = 300000;

    private final ChargeHandler chargeHandler = new ChargeHandler(this, ChargeHandler.Type.STORAGE, ENERGY_CAP);

    public EntityCartRF(World world) {
        super(world);
    }

    public EntityCartRF(World world, double d, double d1, double d2) {
        this(world);
        setPosition(d, d1 + (double) yOffset, d2);
        motionX = 0.0D;
        motionY = 0.0D;
        motionZ = 0.0D;
        prevPosX = d;
        prevPosY = d1;
        prevPosZ = d2;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    public double getEnergy() {
        return chargeHandler.getCharge();
    }

    public double getMaxEnergy() {
        return chargeHandler.getCapacity();
    }

    @Override
    public List<ItemStack> getItemsDropped() {
        List<ItemStack> items = new ArrayList<ItemStack>();
        items.add(getCartItem());
        return items;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (Game.isNotHost(worldObj))
            return;
        chargeHandler.tick();
        //BCLog.logger.info(String.format("TileElectricFeeder.onUpdate: Charge %s / %s", chargeHandler.getCharge(), chargeHandler.getCapacity()));
    }

    @Override
    protected void func_145821_a(int trackX, int trackY, int trackZ, double maxSpeed, double slopeAdjustment, Block trackBlock, int trackMeta) {
        super.func_145821_a(trackX, trackY, trackZ, maxSpeed, slopeAdjustment, trackBlock, trackMeta);
        if (Game.isNotHost(worldObj))
            return;
        chargeHandler.tickOnTrack(trackX, trackY, trackZ);

        //BCLog.logger.info(String.format("TileElectricFeeder.func_145821_a: Charge %s / %s", chargeHandler.getCharge(), chargeHandler.getCapacity()));
    }

    @Override
    public boolean doInteract(EntityPlayer player) {
        if (Game.isHost(worldObj))
            GuiHandler.openGui(EnumGui.CART_RF, player, worldObj, this);
        return true;
    }

    @Override
    public boolean canBeRidden() {
        return false;
    }

    /*@Override
    public final float getMaxCartSpeedOnRail() {
        int numLocomotives = Train.getTrain(this).getNumRunningLocomotives();
        if (numLocomotives == 0)
            return super.getMaxCartSpeedOnRail();
        return Math.min(1.2F, 0.08F + (numLocomotives - 1) * 0.075F);
    }*/

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        chargeHandler.readFromNBT(nbt);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        chargeHandler.writeToNBT(nbt);
    }

//    @Override
//    public Block func_145820_n() {
//        return Blocks.redstone_block;
//    }

    @Override
    public double getDrag() {
        return CartConstants.STANDARD_DRAG;
    }

    /*@Override
    public IIcon getBlockTextureOnSide(int side) {
        return null;
    }*/

    @Override
    public ChargeHandler getChargeHandler() {
        return chargeHandler;
    }
}

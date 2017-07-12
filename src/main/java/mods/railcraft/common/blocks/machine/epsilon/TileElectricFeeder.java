/* 
 * Copyright (c) CovertJaguar, 2014 http://railcraft.info
 * 
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.blocks.machine.epsilon;

import Reika.RotaryCraft.API.Power.IShaftPowerInputCaller;
import Reika.RotaryCraft.API.Power.ShaftPowerInputManager;
import buildcraft.api.core.BCLog;
import mods.railcraft.api.electricity.IElectricGrid;
import mods.railcraft.common.plugins.forge.PowerPlugin;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import mods.railcraft.common.blocks.machine.IEnumMachine;
import mods.railcraft.common.blocks.machine.TileMachineBase;
import mods.railcraft.common.plugins.ic2.IC2Plugin;
import mods.railcraft.common.plugins.ic2.ISinkDelegate;
import mods.railcraft.common.plugins.ic2.TileIC2MultiEmitterDelegate;
import mods.railcraft.common.plugins.ic2.TileIC2SinkDelegate;
import mods.railcraft.common.util.misc.Game;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TileElectricFeeder extends TileMachineBase implements IShaftPowerInputCaller, IElectricGrid {

    protected ShaftPowerInputManager shaftPowerInputManager;
    private final ChargeHandler chargeHandler = new ChargeHandler(this, ChargeHandler.ConnectType.BLOCK, 1);

    public TileElectricFeeder()
    {
        shaftPowerInputManager = new ShaftPowerInputManager(this, "rail electric feeder", 1);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (Game.isNotHost(getWorld()))
            return;

        if (shaftPowerInputManager.isStagePowered(0)) {
            chargeHandler.addCharge(5D * ((double)shaftPowerInputManager.getPower() / 16384D));

            BCLog.logger.info(String.format("TileElectricFeeder: Add charge %s for power %s (total %s)", (5D * ((double)shaftPowerInputManager.getPower() / 16384D)), shaftPowerInputManager.getPower(), chargeHandler.getCharge()));
            /*double capacity = chargeHandler.getCapacity();
            try {
                chargeHandler.setCharge(capacity);
            } catch (Throwable err) {
                chargeHandler.addCharge(capacity - chargeHandler.getCharge());
                Game.logErrorAPI("Railcraft", err, IElectricGrid.class);
            }*/
        }
        chargeHandler.tick();
    }

    @Override
    public ChargeHandler getChargeHandler() {
        return chargeHandler;
    }

    @Override
    public TileEntity getTile() {
        return this;
    }

    @Override
    public IEnumMachine getMachineType() {
        return EnumMachineEpsilon.ELECTRIC_FEEDER;
    }

    @Override
    public IIcon getIcon(int side) {
        return getMachineType().getTexture(shaftPowerInputManager.isStagePowered(0) ? 0 : 6);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        chargeHandler.readFromNBT(data);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        chargeHandler.writeToNBT(data);
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public void writePacketData(DataOutputStream data) throws IOException {
        super.writePacketData(data);
        BCLog.logger.info(String.format("TileElectricFeeder.writePacketData %s %s %s", shaftPowerInputManager.getTorque(), shaftPowerInputManager.getOmega(), shaftPowerInputManager.getPower()));
        data.writeBoolean(shaftPowerInputManager.getPower() > 0);
        if (shaftPowerInputManager.getPower() > 0) {
            data.writeInt(shaftPowerInputManager.getTorque());
            data.writeInt(shaftPowerInputManager.getOmega());
        }
    }

    @Override
    public void readPacketData(DataInputStream data) throws IOException {
        super.readPacketData(data);
        boolean wasPowered = shaftPowerInputManager.isStagePowered(0);
        if (data.readBoolean())
        {
            shaftPowerInputManager.setState(data.readInt(), data.readInt());
        }
        else
        {
            shaftPowerInputManager.setState(0, 0);
        }
        if (shaftPowerInputManager.isStagePowered(0) != wasPowered)
        {
            BCLog.logger.info(String.format("TileElectricFeeder.readPacketData markBlockForUpdate"));
            markBlockForUpdate();
        }
        BCLog.logger.info(String.format("TileElectricFeeder.readPacketData %s %s %s", shaftPowerInputManager.getTorque(), shaftPowerInputManager.getOmega(), shaftPowerInputManager.getPower()));
    }

	/* Rotary Power */

    @Override
    public void onPowerChange(ShaftPowerInputManager shaftPowerInputManager) {
        this.sendUpdateToClient();
    }

    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public boolean addPower(int addTorque, int addOmega, long addPower, ForgeDirection inputDirection) {
        return shaftPowerInputManager != null && shaftPowerInputManager.addPower(addTorque, addOmega, addPower, inputDirection);
    }

    @Override
    public int getStageCount() {
        return shaftPowerInputManager != null ? shaftPowerInputManager.getStageCount() : 0;
    }

    @Override
    public void setIORenderAlpha(int i) {
        if (shaftPowerInputManager != null) shaftPowerInputManager.setIORenderAlpha(i);
    }

    @Override
    public boolean canReadFrom(ForgeDirection forgeDirection) {
        return true;
    }

    @Override
    public boolean isReceiving() {
        return shaftPowerInputManager != null && shaftPowerInputManager.isReceiving();
    }

    @Override
    public int getMinTorque(int stageIndex) {
        return shaftPowerInputManager != null ? shaftPowerInputManager.getMinTorque(stageIndex) : 1;
    }

    @Override
    public int getMinOmega(int stageIndex) {
        return shaftPowerInputManager != null ? shaftPowerInputManager.getMinOmega(stageIndex) : 1;
    }

    @Override
    public long getMinPower(int stageIndex) {
        return shaftPowerInputManager != null ? shaftPowerInputManager.getMinPower(stageIndex) : 1;
    }

    @Override
    public long getPower() {
        return shaftPowerInputManager != null ? shaftPowerInputManager.getPower() : 0;
    }

    @Override
    public int getOmega() {
        return shaftPowerInputManager != null ? shaftPowerInputManager.getOmega() : 0;
    }

    @Override
    public int getTorque() {
        return shaftPowerInputManager != null ? shaftPowerInputManager.getTorque() : 0;
    }

    @Override
    public String getName() {
        return shaftPowerInputManager != null ? shaftPowerInputManager.getName() : "[Railcraft]";
    }

    @Override
    public int getIORenderAlpha() {
        return shaftPowerInputManager != null ? shaftPowerInputManager.getIORenderAlpha() : 0;
    }

}

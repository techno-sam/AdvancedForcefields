package com.slimeist.aforce.common.network;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.tiles.ForceModifierTileEntity;
import com.slimeist.aforce.common.tiles.SimpleForceModifierTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class MessageForceModifierSync implements IMessage
{
    private BlockPos pos;
    private CompoundNBT nbt;

    //TODO get rid of NBT in packets
    public MessageForceModifierSync(ForceModifierTileEntity tile, CompoundNBT nbt)
    {
        this.pos = tile.getBlockPos();
        this.nbt = nbt;
    }

    public MessageForceModifierSync(PacketBuffer buf)
    {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.nbt = buf.readNbt();
    }

    @Override
    public void toBytes(PacketBuffer buf)
    {
        buf.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
        buf.writeNbt(this.nbt);
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> context)
    {
        NetworkEvent.Context ctx = context.get();
        if(ctx.getDirection().getReceptionSide()== LogicalSide.SERVER)
            ctx.enqueueWork(() -> {
                ServerWorld world = Objects.requireNonNull(ctx.getSender()).getLevel();
                if(world.isAreaLoaded(pos, 1))
                {
                    TileEntity tile = world.getBlockEntity(pos);
                    if(tile instanceof ForceModifierTileEntity)
                        ((ForceModifierTileEntity)tile).receiveMessageFromClient(ctx.getSender(), nbt);
                }
            });
        else
            ctx.enqueueWork(() -> {
                World world = AdvancedForcefields.proxy.getClientWorld();
                if(world!=null) // This can happen if the task is scheduled right before leaving the world
                {
                    TileEntity tile = world.getBlockEntity(pos);
                    if(tile instanceof ForceModifierTileEntity)
                        ((ForceModifierTileEntity)tile).receiveMessageFromServer(nbt);
                }
            });
    }
}
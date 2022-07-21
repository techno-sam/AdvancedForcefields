package com.slimeist.aforce.common.network;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.tiles.ForceModifierTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class MessageForceModifierSync implements IMessage
{
    private BlockPos pos;
    private CompoundTag nbt;

    //TODO get rid of NBT in packets
    public MessageForceModifierSync(ForceModifierTileEntity tile, CompoundTag nbt)
    {
        this.pos = tile.getBlockPos();
        this.nbt = nbt;
    }

    public MessageForceModifierSync(FriendlyByteBuf buf)
    {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.nbt = buf.readNbt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf)
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
                ServerLevel world = Objects.requireNonNull(ctx.getSender()).getLevel();
                if(world.isAreaLoaded(pos, 1))
                {
                    BlockEntity tile = world.getBlockEntity(pos);
                    if(tile instanceof ForceModifierTileEntity)
                        ((ForceModifierTileEntity)tile).receiveMessageFromClient(ctx.getSender(), nbt);
                }
            });
        else
            ctx.enqueueWork(() -> {
                Level world = AdvancedForcefields.proxy.getClientWorld();
                if(world!=null) // This can happen if the task is scheduled right before leaving the world
                {
                    BlockEntity tile = world.getBlockEntity(pos);
                    if(tile instanceof ForceModifierTileEntity)
                        ((ForceModifierTileEntity)tile).receiveMessageFromServer(nbt);
                }
            });
    }
}
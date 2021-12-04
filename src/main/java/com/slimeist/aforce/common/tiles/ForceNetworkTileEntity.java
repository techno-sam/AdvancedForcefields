package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.common.blocks.ForceTubeBlock;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import com.slimeist.aforce.core.util.TagUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;

public class ForceNetworkTileEntity extends ModTileEntity implements ITickableTileEntity {

    protected static final String TAG_PACKET_TYPE = "packetType"; //things such as 'DATA_SYNC', 'COLLISION_DETECTED'

    protected static final String TAG_PACKET_MESSAGE = "packetMessage"; //where the actual message goes in a packet

    protected static final String TAG_BLOCKING_MODE = "blockingMode"; //can be 1:'NONE', 2:'ALL', 3:'PLAYERS', 4:'MOBS'

    protected int blockingMode = 1;

    protected void setBlockingMode(int blockingMode) {
        this.blockingMode = blockingMode;
    }

    protected int getBlockingMode() {
        return this.blockingMode;
    }

    protected static final String TAG_COLOR = "color"; //int representing packed rgba color

    protected int color = 16711680; //(255, 0, 0)

    protected void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    protected static final String TAG_DISTANCE = "distance"; //how far we are from master in network space

    protected int distance = -1; //-1 means not connected, 0 is master, 1 is tube next to master, 2 is tube next to tube next to master, etc.

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDistance() {
        return this.distance;
    }

    protected static final String TAG_CONNECTIONS = "connections"; //which directions we are connected in

    protected byte[] connections = new byte[] {1, 1, 1, 1, 1, 1};

    private byte booleanToByte(boolean b) {
        return b ? (byte) 1 : (byte) 0;
    }

    private boolean byteToBoolean(byte b) {
        return b == 1;
    }

    protected void setConnections(byte[] connections) {
        this.connections = connections;
    }

    protected byte[] getConnections() {
        return this.connections;
    }

    protected void setConnected(Direction dir, boolean connected) {
        this.connections[dir.get3DDataValue()] = booleanToByte(connected);
    }

    public boolean isConnected(Direction dir) {
        return byteToBoolean(this.connections[dir.get3DDataValue()]);
    }

    protected static final String TAG_MASTER_POS = "masterPos";

    protected BlockPos masterPos = null;

    public boolean hasMasterPos() {
        return this.masterPos!=null;
    }

    protected void setMasterPos(BlockPos masterPos) {
        this.masterPos = masterPos;
    }

    protected BlockPos getMasterPos() {
        return this.masterPos;
    }

    protected static final String TAG_PACKET_LIST = "packetList";

    protected ArrayList<ForceNetworkPacket> packetList = new ArrayList<ForceNetworkPacket>();

    protected void setPacketList(ArrayList<ForceNetworkPacket> packetList) {
        this.packetList = packetList;
    }

    protected ArrayList<ForceNetworkPacket> getPacketList() {
        return this.packetList;
    }

    protected void addPacket(ForceNetworkPacket packet) {
        this.getPacketList().add(packet);
    }

    protected ForceNetworkPacket popPacket() {
        if (this.getPacketList().size()>0) {
            return this.getPacketList().remove(0);
        } else {
            return null;
        }
    }

    protected void loadPacketList(ListNBT list) {
        this.packetList = new ArrayList<ForceNetworkPacket>();
        for(int i = 0; i < list.size(); ++i) {
            CompoundNBT nbt = list.getCompound(i);
            this.packetList.add(new ForceNetworkPacket(nbt));
        }
    }

    protected ListNBT savePacketList() {
        ListNBT nbt = new ListNBT();
        for (ForceNetworkPacket forceNetworkPacket : this.packetList) {
            CompoundNBT compoundnbt = forceNetworkPacket.toNBT();
            nbt.add(compoundnbt);
        }
        return nbt;
    }

    protected static final String TAG_IS_DIRTY = "isDirty";

    protected boolean isDirty = false;

    protected void markAsDirty() {
        this.isDirty = true;
    }

    protected void markAsClean() {
        this.isDirty = false;
    }

    protected void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    protected boolean isDirty() {
        return this.isDirty;
    }

    protected static final String TAG_IS_LOCKED = "isLocked"; //whether we are locked in network

    protected boolean isLocked = false;

    protected void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    protected boolean isLocked() {
        return this.isLocked;
    }

    public ForceNetworkTileEntity(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
    }

    @Override
    public void tick() {
        this.handlePackets();
        if (this.isDirty()) {
            this.handleDirty();
        }
    }

    /*public void updateDistance() {
        ArrayList<Direction> dirs = new ArrayList<Direction>();

        for (Direction dir : Direction.values()) {
            BlockPos testPos = this.getBlockPos().offset(dir.getNormal());
            if (this.isConnected(testPos)) {
                if (this.getLevel().hasChunkAt(testPos)) {
                    dirs.add(dir);
                }
            }
        }
        int mydistance = this.getDistance();
        int distance = -1;
        boolean connected_to_lower = false;
        for (Direction dir: dirs) {
            BlockPos testPos = this.getBlockPos().offset(dir.getNormal());
            TileEntity testTile = this.getLevel().getBlockEntity(testPos);
            if (testTile instanceof ForceNetworkTileEntity) {
                ForceNetworkTileEntity forceTile = (ForceNetworkTileEntity) testTile;
                int newdist = forceTile.getDistance()+1;
                if ((newdist<=mydistance) && newdist>0) {
                    connected_to_lower = true;
                }
                if ((newdist<distance || distance==-1) && newdist>0) {
                    distance = newdist;
                }
            }
        }
        if (connected_to_lower) {
            this.setDistance(distance);
        } else {
            CompoundNBT data = new CompoundNBT();
            data.putString(TAG_PACKET_TYPE, "NETWORK_RELEASE");
            data.put(TAG_PACKET_MESSAGE, new CompoundNBT());
            ForceNetworkPacket release_packet = new ForceNetworkPacket(ForceNetworkDirection.TO_SERVANTS, data, true);
            this.onReceiveToServantsPacket(this.getBlockPos(), mydistance, release_packet);
        }
    }*/

    public void handleDirty() {
        CompoundNBT shareddata = new CompoundNBT();
        this.writeSyncedShared(shareddata);
        CompoundNBT data = new CompoundNBT();
        data.putString(TAG_PACKET_TYPE, "DATA_SYNC");
        data.put(TAG_PACKET_MESSAGE, shareddata);

        this.addPacket(new ForceNetworkPacket(ForceNetworkDirection.TO_MASTER, data));
        this.markAsClean();
    }

    public void onReceiveToMasterPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {}

    public void onReceiveToServantsPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {
        if (this.getLevel()!=null && packet.data.getString(TAG_PACKET_TYPE).equals("DATA_SYNC")) {
            this.loadShared(this.getLevel().getBlockState(myPos), packet.data.getCompound(TAG_PACKET_MESSAGE).copy());
            this.markDirtyFast();
        } else if (packet.data.getString(TAG_PACKET_TYPE).equals("NETWORK_RELEASE")) {
            this.onNetworkBuild(null);
            this.setLocked(false);
            BlockPos pos = this.getBlockPos();
            World world = this.getLevel();
            BlockState state = world.getBlockState(pos);
            if (state.is(AdvancedForcefieldsTags.Blocks.FORCE_TUBE)) {
                world.setBlock(pos, state.setValue(ForceTubeBlock.ENABLED, false), Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.NO_RERENDER);
            }
        }
    }

    public void handlePackets() {
        ForceNetworkPacket packet = this.popPacket();

        if (packet!=null) {
            if (!packet.active) {
                packet.active = true;
                this.addPacket(packet.copy());
            } else {
                ForceNetworkDirection direction = packet.direction;
                if (direction == ForceNetworkDirection.TO_MASTER) {
                    BlockPos myPos = this.getBlockPos();
                    int myDist = this.getDistance();

                    this.onReceiveToMasterPacket(myPos, myDist, packet.copy());

                    BlockPos lowestPos = null;
                    int lowestDist = myDist;
                    for (Direction dir : Direction.values()) {
                        BlockPos testPos = myPos.offset(dir.getNormal());
                        if (this.isConnected(testPos)) {
                            if (this.getLevel() != null) {
                                TileEntity testTile = this.getLevel().getBlockEntity(testPos);
                                if (testTile instanceof ForceNetworkTileEntity) {
                                    int testDist = ((ForceNetworkTileEntity) testTile).getDistance();
                                    if (testDist < lowestDist) {
                                        lowestDist = testDist;
                                        lowestPos = testPos;
                                    }
                                }
                            }
                        }
                    }

                    if (lowestPos != null) {
                        if (this.getLevel()!=null) {
                            TileEntity tile = this.getLevel().getBlockEntity(lowestPos);
                            if (tile instanceof ForceNetworkTileEntity) {
                                ((ForceNetworkTileEntity) tile)
                                        .addPacket(packet.copy());
                            }
                        }
                    }
                } else if (direction == ForceNetworkDirection.TO_SERVANTS) {
                    BlockPos myPos = this.getBlockPos();
                    int myDist = this.getDistance();
                /*if (this.getLevel()!=null) {
                    this.loadShared(this.getLevel().getBlockState(myPos), packet.data.getCompound(TAG_PACKET_MESSAGE).copy());
                }*/

                    for (Direction dir : Direction.values()) {
                        BlockPos testPos = myPos.offset(dir.getNormal());
                        if (this.isConnected(testPos)) {
                            if (this.getLevel() != null) {
                                TileEntity testTile = this.getLevel().getBlockEntity(testPos);
                                if (testTile instanceof ForceNetworkTileEntity) {
                                    int testDist = ((ForceNetworkTileEntity) testTile).getDistance();
                                    if (testDist > myDist) {
                                        ((ForceNetworkTileEntity) testTile)
                                                .addPacket(packet.copy());
                                    }
                                }
                            }
                        }/* else {
                        info("ForceNetworkTE at ["+myPos+"] with distance ["+myDist+"] is not connected in direction ["+dir.getName()+"]");
                    }*/
                    }
                    this.onReceiveToServantsPacket(myPos, myDist, packet.copy());
                }
            }
        }
    }

    public void onNetworkBuild(BlockPos masterPos) {
        this.distance = -1;
        //this.connections = new byte[] {1, 1, 1, 1, 1, 1};
        this.masterPos = masterPos;
        this.packetList = new ArrayList<ForceNetworkPacket>();
    }
    public boolean isConnected(BlockPos otherPos) {
        BlockPos myPos = this.getBlockPos();
        Direction direction = Direction.fromNormal(myPos.getX()-otherPos.getX(), myPos.getY()-otherPos.getY(), myPos.getZ()-otherPos.getZ());
        if (direction!=null) {
            return this.isConnected(direction);
        } else {
            return false;
        }
    }

    public boolean canConnect(BlockPos otherPos) {
        return this.canConnect(otherPos, false);
    }

    private void info(String msg) {
        AdvancedForcefields.LOGGER.info(msg);
    }

    protected boolean canConnect(BlockPos otherPos, boolean recursed) {
        boolean isOk = false;
        BlockPos myPos = this.getBlockPos();
        Direction direction = Direction.fromNormal(myPos.getX()-otherPos.getX(), myPos.getY()-otherPos.getY(), myPos.getZ()-otherPos.getZ());
        World world = this.getLevel();
        if (world!=null) {
            BlockState otherBlock = world.getBlockState(otherPos);
            if (otherBlock.is(AdvancedForcefieldsTags.Blocks.FORCE_COMPONENT)) {
                if (!this.isLocked()) {
                    isOk = true;
                } else {
                    isOk = this.isConnected(direction);
                    //info("["+this.getBlockPos()+"]->["+otherPos+"] isOk1: "+isOk);
                }
                TileEntity otherTile = world.getBlockEntity(otherPos);
                if (otherTile instanceof ForceNetworkTileEntity && !recursed) {
                    isOk = isOk && ((ForceNetworkTileEntity) otherTile).canConnect(myPos, true);
                    //info("["+this.getBlockPos()+"]->["+otherPos+"] isOk2: "+isOk);
                    isOk = isOk && this.hasMasterPos() == ((ForceNetworkTileEntity) otherTile).hasMasterPos();
                    //info("["+this.getBlockPos()+"]->["+otherPos+"] isOk3: "+isOk);
                    if (this.hasMasterPos() && ((ForceNetworkTileEntity) otherTile).hasMasterPos()) {
                        isOk = isOk && this.getMasterPos().equals(((ForceNetworkTileEntity) otherTile).getMasterPos());
                        //info("["+this.getBlockPos()+"]->["+otherPos+"] isOk4 "+isOk);
                    }
                }
            }
        }
        this.setConnected(direction, isOk);
        return isOk;
    }

    public void loadInternal(BlockState blockState, CompoundNBT nbt) {
        this.setDistance(nbt.getInt(TAG_DISTANCE));
        this.setConnections(nbt.getByteArray(TAG_CONNECTIONS));
        CompoundNBT masterPosNBT = nbt.getCompound(TAG_MASTER_POS);
        if (masterPosNBT.isEmpty()) {
            this.setMasterPos(null);
        } else {
            this.setMasterPos(TagUtil.readPos(masterPosNBT));
        }
        this.loadPacketList(nbt.getList(TAG_PACKET_LIST, Constants.NBT.TAG_COMPOUND));
        this.setDirty(nbt.getBoolean(TAG_IS_DIRTY));
    }

    public void writeSyncedInternal(CompoundNBT nbt) {
        nbt.putInt(TAG_DISTANCE, this.getDistance());
        nbt.putByteArray(TAG_CONNECTIONS, this.getConnections());
        if (this.hasMasterPos()) {
            nbt.put(TAG_MASTER_POS, TagUtil.writePos(this.getMasterPos()));
        } else {
            nbt.put(TAG_MASTER_POS, new CompoundNBT());
        }
        nbt.put(TAG_PACKET_LIST, this.savePacketList());
        nbt.putBoolean(TAG_IS_DIRTY, this.isDirty());
    }

    public void loadShared(BlockState state, CompoundNBT nbt) {
        this.setBlockingMode(nbt.getInt(TAG_BLOCKING_MODE));
        this.setColor(nbt.getInt(TAG_COLOR));
    }

    public void writeSyncedShared(CompoundNBT nbt) {
        nbt.putInt(TAG_BLOCKING_MODE, this.getBlockingMode());
        nbt.putInt(TAG_COLOR, this.getColor());
    }

    public void loadPersonal(BlockState blockState, CompoundNBT nbt) {

    }

    public void writeSyncedPersonal(CompoundNBT nbt) {

    }

    @Override
    public void load(BlockState blockState, CompoundNBT nbt) {
        super.load(blockState, nbt);
        this.loadInternal(blockState, nbt.getCompound("internal"));
        this.loadShared(blockState, nbt.getCompound("shared"));
        this.loadPersonal(blockState, nbt.getCompound("personal"));
    }

    @Override
    public void writeSynced(CompoundNBT nbt) {
        super.writeSynced(nbt);

        CompoundNBT internalNBT = nbt.getCompound("internal");
        writeSyncedInternal(internalNBT);
        nbt.put("internal", internalNBT);

        CompoundNBT sharedNBT = nbt.getCompound("shared");
        writeSyncedShared(sharedNBT);
        nbt.put("shared", sharedNBT);

        CompoundNBT personalNBT = nbt.getCompound("personal");
        writeSyncedPersonal(personalNBT);
        nbt.put("personal", personalNBT);
    }

    @Override
    public boolean shouldSyncOnUpdate() {
        //AdvancedForcefields.LOGGER.info("Returning true for shouldSyncOnUpdate");
        return true;
    }
}

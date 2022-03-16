package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.common.blocks.ForceTubeBlock;
import com.slimeist.aforce.common.tiles.helpers.ForceModifierSelector;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.BlockInit;
import com.slimeist.aforce.core.interfaces.IForceNetworkBlock;
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
import java.util.HashMap;
import java.util.List;

public class ForceNetworkTileEntity extends ModTileEntity implements ITickableTileEntity {

    protected static final String TAG_PACKET_TYPE = "packetType"; //things such as 'DATA_SYNC', 'COLLISION_DETECTED'

    protected static final String TAG_PACKET_MESSAGE = "packetMessage"; //where the actual message goes in a packet

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

    protected static final String TAG_LATEST_PACKET_GAME_TIME = "latestPacketGT"; //gametime of latest packet received, master always sends full shared data in DATA_SYNC, so this solves double packet issues

    protected long latest_packet_game_time = 0;

    protected void setLatestPacketGameTime(long gameTime) {
        if (gameTime > this.latest_packet_game_time) {
            this.latest_packet_game_time = gameTime;
        }
    }

    protected void resetLatestPacketGameTime() {
        this.latest_packet_game_time = 0;
    }

    protected long getLatestPacketGameTime() {
        return this.latest_packet_game_time;
    }

    protected HashMap<Integer, ArrayList<ForceModifierSelector>> sortedActionSelectors = new HashMap<>();

    protected static final String TAG_ACTION_SELECTORS = "actionSelectors";

    protected ArrayList<ForceModifierSelector> actionSelectors = new ArrayList<>();

    public void clearActionSelectors() {
        this.actionSelectors.clear();
        updateSortedActionSelectors();
    }

    public void clearActionSelectors(BlockPos originator) {
        //info("cas: We are "+((this instanceof ForceControllerTileEntity) ? "" : "not ")+"a controller");
        //info("Clearing action selectors for pos: "+originator.toShortString()+", this network tile is "+(this.getLevel().isClientSide() ? "clientside" : "serverside"));
        //info("There were "+this.actionSelectors.size()+" selectors.");
        ArrayList<ForceModifierSelector> newSelectors = new ArrayList<>();
        for (ForceModifierSelector selector : this.actionSelectors) {
            if (!selector.getOriginPosition().equals(originator)) {
                //info("Adding selector which has a position of: "+selector.getOriginPosition().toShortString()+", which is not the same as "+originator.toShortString());
                newSelectors.add(selector);
            }
        }
        this.actionSelectors.clear();
        this.actionSelectors = newSelectors;
        //info("There are now "+this.actionSelectors.size()+" selectors.");
        updateSortedActionSelectors();
        //info("After updating our sorted list of action selectors, there are now "+this.actionSelectors.size()+" selectors.");
    }

    public void addActionSelector(ForceModifierSelector selector) {
        this.actionSelectors.add(selector);
        //info("aas: We are "+((this instanceof ForceControllerTileEntity) ? "" : "not ")+"a controller");
        //info("Added action selector with action: "+selector.getAction());
        updateSortedActionSelectors();
    }

    @SuppressWarnings("unchecked")
    public List<ForceModifierSelector> getActionSelectors() {
        return (List<ForceModifierSelector>) this.actionSelectors.clone();
    }

    public void updateSortedActionSelectors() {
        if (this.getActionSelectors().size()==0) {
            this.sortedActionSelectors.clear();
            return;
        }
        HashMap<Integer, ArrayList<ForceModifierSelector>> sorted = new HashMap<>();

        int minPriority = Integer.MAX_VALUE;
        int maxPriority = Integer.MIN_VALUE;

        for (ForceModifierSelector sel : this.getActionSelectors()) {
            int priority = sel.getPriority();
            if (priority<minPriority) {
                minPriority = priority;
            }
            if (priority>maxPriority) {
                maxPriority = priority;
            }
        }

        for (int priority=minPriority; priority<=maxPriority; priority++) {
            ArrayList<ForceModifierSelector> temp = new ArrayList<>();
            for (ForceModifierSelector sel : this.getActionSelectors()) {
                if (sel.getPriority()==priority) {
                    temp.add(sel);
                }
            }
            if (temp.size()!=0) {
                sorted.put(priority, temp);
            }
        }

        this.sortedActionSelectors = sorted;
    }

    public HashMap<Integer, ArrayList<ForceModifierSelector>> getSortedActionSelectors() {
        return this.sortedActionSelectors;
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

    public void handleDirty() { //this doesn't seem to every execute, as ForceControllerTileEntity is the only thing currently marking as dirty, and that has custom handleDirty implementation
        CompoundNBT shareddata = new CompoundNBT();
        this.writeSyncedShared(shareddata);
        CompoundNBT data = new CompoundNBT();
        data.putString(TAG_PACKET_TYPE, "DATA_SYNC");
        data.put(TAG_PACKET_MESSAGE, shareddata);

        this.addPacket(new ForceNetworkPacket(ForceNetworkDirection.TO_MASTER, data, this.getBlockPos()));
        this.markAsClean();
    }

    public void onReceiveToMasterPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {}

    public void onReceiveToServantsPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {
        if (this.getLevel()!=null && packet.data.getString(TAG_PACKET_TYPE).equals("DATA_SYNC")) {
            if (packet.createdGameTime > this.getLatestPacketGameTime()) {
                BlockPos acceptingBlockPos = null;
                for (Direction dir : Direction.values()) {
                    BlockPos testPos = this.getBlockPos().relative(dir);
                    TileEntity te = this.getLevel().getBlockEntity(testPos);
                    if (te instanceof ForceNetworkTileEntity) {
                        ForceNetworkTileEntity fnte = (ForceNetworkTileEntity) te;
                        if (fnte.getDistance()<this.getDistance()) {
                            acceptingBlockPos = testPos;
                            break;
                        }
                    }
                } //accept from one side only, should fix some problems
                AdvancedForcefields.LOGGER.warn("ABP: "+(acceptingBlockPos!=null ? acceptingBlockPos.toShortString() : "Nothing")+", OP: "+packet.originPos.toShortString());
                if (acceptingBlockPos==null || acceptingBlockPos.equals(packet.originPos)) {
                    this.loadShared(this.getLevel().getBlockState(myPos), packet.data.getCompound(TAG_PACKET_MESSAGE).copy());
                    this.markDirtyFast();
                    this.setLatestPacketGameTime(packet.createdGameTime);
                }
            } else {
                AdvancedForcefields.LOGGER.info("ForceNetworkTileEntity discarding late packet, latest time: "+this.getLatestPacketGameTime()+", packet time: "+packet.createdGameTime);
            }
        } else if (packet.data.getString(TAG_PACKET_TYPE).equals("NETWORK_RELEASE")) {
            this.onNetworkBuild(null);
            this.setLocked(false);
            this.reset();
            BlockPos pos = this.getBlockPos();
            World world = this.getLevel();
            BlockState state = world.getBlockState(pos);
            if (state.is(AdvancedForcefieldsTags.Blocks.FORCE_TUBE)) {
                world.setBlock(pos, state.setValue(ForceTubeBlock.ENABLED, false), Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.NO_RERENDER);
            }
        }
    }

    protected void reset() {
        this.setColor(16711680);
        this.clearActionSelectors();
        this.resetLatestPacketGameTime();
    }

    public void handlePackets() {
        ForceNetworkPacket packet = this.popPacket();

        if (this.getLevel()!=null && !this.getLevel().isClientSide() && packet!=null) {
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
                                                .addPacket(packet.copy().setOriginPos(this.getBlockPos()));
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

    protected void info(String msg) {
        AdvancedForcefields.LOGGER.info(msg);
    }

    protected boolean sharesMasterPos(BlockPos otherPos) {
        BlockPos myPos = this.getBlockPos();
        if (this.getLevel()==null)
            return false;
        TileEntity tile = this.getLevel().getBlockEntity(otherPos);
        if (tile instanceof ForceNetworkTileEntity) {
            ForceNetworkTileEntity networkTile = (ForceNetworkTileEntity) tile;
            if (this.hasMasterPos()==networkTile.hasMasterPos()) {
                if (this.hasMasterPos() && networkTile.hasMasterPos()) {
                    return this.getMasterPos().equals(networkTile.getMasterPos());
                }
                return true;
            }
        }
        return false;
    }

    protected boolean canConnect(BlockPos otherPos, boolean recursed) {
        boolean isOk = false;
        BlockPos myPos = this.getBlockPos();
        Direction direction = Direction.fromNormal(myPos.getX()-otherPos.getX(), myPos.getY()-otherPos.getY(), myPos.getZ()-otherPos.getZ());
        World world = this.getLevel();
        String reason = "NONE";
        if (world!=null) {
            BlockState otherBlock = world.getBlockState(otherPos);
            if (otherBlock.is(AdvancedForcefieldsTags.Blocks.FORCE_COMPONENT)) {
                if (!this.isLocked()) {
                    isOk = true;
                } else {
                    isOk = this.isConnected(direction);
                    if (!isOk)
                        reason = "Not connected to the "+direction.getName();
                    //info("["+this.getBlockPos()+"]->["+otherPos+"] isOk1: "+isOk);
                }
                TileEntity otherTile = world.getBlockEntity(otherPos);
                if (otherTile instanceof ForceNetworkTileEntity && !recursed) {
                    boolean changeReason = isOk;
                    isOk = isOk && ((ForceNetworkTileEntity) otherTile).canConnect(myPos, true);
                    if (changeReason&&!isOk)
                        reason += ", recursive connection check failed";
                    //info("["+this.getBlockPos()+"]->["+otherPos+"] isOk2: "+isOk);
                    changeReason = isOk;
                    isOk = isOk && this.hasMasterPos() == ((ForceNetworkTileEntity) otherTile).hasMasterPos();
                    if (changeReason&&!isOk)
                        reason += ", hasMasterPos state differs";
                    //info("["+this.getBlockPos()+"]->["+otherPos+"] isOk3: "+isOk);
                    if (this.hasMasterPos() && ((ForceNetworkTileEntity) otherTile).hasMasterPos()) {
                        changeReason = isOk;
                        isOk = isOk && this.getMasterPos().equals(((ForceNetworkTileEntity) otherTile).getMasterPos());
                        if (changeReason&&!isOk)
                            reason += ", masterPos differs";
                        //info("["+this.getBlockPos()+"]->["+otherPos+"] isOk4 "+isOk);
                    }
                }
            } else {
                reason += ", non-force component adjacent";
            }
        } else {
            reason += ", no level!";
        }
        if (!recursed && isOk != sharesMasterPos(otherPos)) {
            info("DISAGREEMENT: canConnect returning: "+isOk+", and simple masterPos check returns: "+sharesMasterPos(otherPos)+", for block at "+this.getBlockPos().toShortString()+", connecting to "+otherPos.toShortString()+", giving reason: "+reason);
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
        this.setLocked(nbt.getBoolean(TAG_IS_LOCKED));
        this.resetLatestPacketGameTime();
        this.setLatestPacketGameTime(nbt.getLong(TAG_LATEST_PACKET_GAME_TIME));
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
        nbt.putBoolean(TAG_IS_LOCKED, this.isLocked());
        nbt.putLong(TAG_LATEST_PACKET_GAME_TIME, this.getLatestPacketGameTime());
    }

    public void loadShared(BlockState state, CompoundNBT nbt) {
        this.setColor(nbt.getInt(TAG_COLOR));

        ListNBT actionList = nbt.getList(TAG_ACTION_SELECTORS, Constants.NBT.TAG_COMPOUND);
        this.clearActionSelectors();
        for (int i=0; i<actionList.size(); i++) {
            CompoundNBT actionNBT = actionList.getCompound(i);
            this.addActionSelector(ForceModifierSelector.fromNBT(actionNBT));
        }
    }

    public void writeSyncedShared(CompoundNBT nbt) {
        nbt.putInt(TAG_COLOR, this.getColor());

        ListNBT actionList = new ListNBT();
        for (ForceModifierSelector sel : this.getActionSelectors()) {
            actionList.add(sel.toNBT());
        }
        nbt.put(TAG_ACTION_SELECTORS, actionList);
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

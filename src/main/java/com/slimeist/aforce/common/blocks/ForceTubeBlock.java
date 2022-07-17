package com.slimeist.aforce.common.blocks;

import com.mojang.math.Vector3f;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.client.util.ClientUtils;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import com.slimeist.aforce.common.tiles.ForceNetworkTileEntity;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import com.slimeist.aforce.common.tiles.helpers.ForceModifierSelector;
import com.slimeist.aforce.core.enums.*;
import com.slimeist.aforce.core.init.RegistryInit;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.interfaces.IForceNetworkBlock;
import com.slimeist.aforce.core.util.ColorUtil;
import com.slimeist.aforce.core.util.MiscUtil;
import com.slimeist.aforce.core.util.RenderLayerHandler;
import com.slimeist.aforce.core.util.TileEntityHelper;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ForceTubeBlock extends BasePipeBlock implements IForceNetworkBlock, EntityBlock {

    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public ForceTubeBlock(@Nonnull Properties properties) {
        super(properties);

        registerDefaultState(defaultBlockState()
                .setValue(ENABLED, false)
        );
        RenderLayerHandler.setRenderType(this, RenderLayerHandler.RenderTypeSkeleton.TRANSLUCENT);
    }

    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.BLOCK;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ForceTubeTileEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return TileEntityHelper.createTickerHelper(type, TileEntityTypeInit.FORCE_TUBE_TYPE, ForceTubeTileEntity::tick);
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, worldIn, pos, oldState, isMoving);
        this.doUpdate(state, worldIn, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        //super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        this.doUpdate(state, worldIn, pos);
    }

    public void doUpdate(BlockState state, Level worldIn, BlockPos pos) {
        if (!worldIn.isClientSide) {
            this.updateDistance(worldIn, pos, state);
            this.markTEDirty(worldIn, pos);
        }

        BlockState targetState = getTargetState(worldIn, pos, state.getValue(WATERLOGGED), this.getInternalDistance(worldIn, pos)!=-1);
        if(!targetState.equals(state)) {
            worldIn.setBlock(pos, targetState, UPDATE_ALL | UPDATE_INVISIBLE);
        }
    }

    private BlockState getTargetState(Level worldIn, BlockPos pos, boolean waterlog, boolean enabled) {
        BlockState newState = defaultBlockState();
        newState = newState.setValue(WATERLOGGED, waterlog);
        newState = newState.setValue(ENABLED, enabled);

        for(Direction facing : Direction.values()) {
            BooleanProperty prop = CONNECTIONS[facing.ordinal()];

            BlockPos neighborpos = pos.offset(facing.getNormal()); //(worldIn, pos, facing);
            BlockState neighborstate = worldIn.getBlockState(neighborpos);
            boolean matching = isMatchingBlock(worldIn.getBlockState(pos), neighborstate, pos, neighborpos, worldIn);

            newState = newState.setValue(prop, matching);
        }

        return newState;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (state.getValue(ENABLED)) {
            return Shapes.block();
        }
        return super.getShape(state, worldIn, pos, context);
    }

    @Override
    public boolean isMatchingBlock(BlockState mystate, BlockState neighborstate, BlockPos mypos, BlockPos neighborpos, BlockGetter blockReader) {
        return this.isMatchingBlock(mypos, neighborpos, blockReader);
    }

    public boolean isMatchingBlock(BlockPos mypos, BlockPos neighborpos, BlockGetter blockReader) {
        BlockEntity myTile = blockReader.getBlockEntity(mypos);
        if (myTile instanceof ForceTubeTileEntity) {
            return ((ForceTubeTileEntity) myTile).canConnect(neighborpos);
        }
        return false;
    }



    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
        BlockEntity tile = worldIn.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            return ((ForceTubeTileEntity) tile).getComparatorOutput();
        }
        return 0;
    }

    @Override
    public int getDistance(Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity && this.shouldSignal(world, pos)) {
            int distance = ((ForceNetworkTileEntity) tile).getDistance();
            //LOGGER.info("getDistance @ "+pos.toShortString()+" (shouldSignal: "+this.shouldSignal(world, pos)+"), returning "+distance);
            return distance;
        }
        //LOGGER.info("getDistance @ "+pos.toShortString()+" (shouldSignal: "+this.shouldSignal(world, pos)+"), returning -1, because we did not find a good TE, or shouldn't signal");
        return -1;
    }

    private int getInternalDistance(Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            int distance = ((ForceNetworkTileEntity) tile).getDistance();
            //LOGGER.info("getInteranlDistance @ "+pos.toShortString()+" returning "+distance);
            return distance;
        }
        //LOGGER.info("getInternalDistance @ "+pos.toShortString()+" returning -1, because we did not find a good TE");
        return -1;
    }

    @Override
    public void setDistance(Level world, BlockPos pos, int distance) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            ((ForceNetworkTileEntity) tile).setDistance(distance);
        }
    }

    @Override
    public boolean hasCloser(Level world, BlockPos pos, BlockPos bannedPos, int distance) {
        for (Direction dir : Direction.values()) {
            BlockPos testPos = pos.relative(dir);
            if (!testPos.equals(bannedPos) && this.canConnect(world, pos, testPos)) {
                BlockState testState = world.getBlockState(testPos);
                Block testBlock = testState.getBlock();
                if (testBlock instanceof IForceNetworkBlock) {
                    int d1 = ((IForceNetworkBlock) testBlock).getDistance(world, testPos);
                    if (d1<distance) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isConnected(Level world, BlockPos pos, BlockPos otherPos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            return ((ForceNetworkTileEntity) tile).isConnected(otherPos);
        } else {
            LOGGER.error("Did not find expected ForceNetworkTileEntity at ["+pos+"], when checking whether connected to ["+otherPos+"]");
        }
        return false;
    }

    public boolean hasMasterPos(Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            return ((ForceNetworkTileEntity) tile).hasMasterPos();
        } else {
            LOGGER.error("Did not find expected ForceNetworkTileEntity at ["+pos+"], when checking whether it hasMasterPos");
        }
        return false;
    }

    public boolean canConnect(Level world, BlockPos pos, BlockPos otherPos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            return ((ForceNetworkTileEntity) tile).canConnect(otherPos);
        } else {
            LOGGER.error("Did not find expected ForceNetworkTileEntity at [" + pos + "], when checking whether can connect to [" + otherPos + "]");
        }
        return false;
    }

    public boolean shouldSignal(Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            return ((ForceTubeTileEntity) tile).isSignalling();
        } else {
            LOGGER.error("Did not find expected ForceTubeTileEntity at [" + pos + "], when checking whether should signal");
        }
        return false;
    }

    public void setSignalling(Level world, BlockPos pos, boolean shouldSignal) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ((ForceTubeTileEntity) tile).setSignalling(shouldSignal);
        } else {
            LOGGER.error("Did not find expected ForceTubeTileEntity at [" + pos + "], when setting shouldSignal");
        }
    }

    private void networkDisconnect(Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ((ForceTubeTileEntity) tile).networkDisconnect();
        } else {
            LOGGER.error("Did not find expected ForceTubeTileEntity at [" + pos + "], when disconnecting from network");
        }
    }

    private void markTEDirty(Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ((ForceTubeTileEntity) tile).setChanged();
            BlockState state = world.getBlockState(pos);
            world.sendBlockUpdated(pos, state, state, UPDATE_CLIENTS);
            //LOGGER.info("Marked ForceTubeTileEntity at [" + pos + "] as dirty");
        } else {
            LOGGER.error("Did not find expected ForceTubeTileEntity at [" + pos + "], when marking TE as dirty");
        }
    }

    public void updateDistance(Level world, BlockPos pos, BlockState state) {
        int targetDistance = this.calculateTargetDistance(world, pos);
        if (this.getDistance(world, pos) != targetDistance) {
            //LOGGER.warn("Distance is: "+this.getDistance(world, pos)+", target is: "+targetDistance);
            this.setDistance(world, pos, targetDistance);
            if (targetDistance==-1) {
                this.networkDisconnect(world, pos);
            }
            //this.markTEDirty(world, pos);
            //LOGGER.warn("Distance is now: "+this.getDistance(world, pos));

            world.updateNeighborsAt(pos, this);
        }
    }

    private int calculateTargetDistance(Level world, BlockPos pos) {
        this.setSignalling(world, pos, false);
        int dist = this.getLeastNeighborDistance(world, pos);
        this.setSignalling(world, pos, true);
        if (dist==-1) {
            return dist;
        } else {
            return dist+1;
        }
    }

    private int getLeastNeighborDistance(Level world, BlockPos pos) {
        int d = -1;
        boolean hasCloser = false;
        int myd = this.getInternalDistance(world, pos);
        String msg = "\ngetLeastNeighborDistance @ "+pos.toShortString()+": ";

        for (Direction dir : Direction.values()) {
            BlockPos testPos = pos.relative(dir);
            if (this.canConnect(world, pos, testPos)) {
                BlockState testState = world.getBlockState(testPos);
                msg += "\n\tConnected to the "+dir.getName()+" to "+testState;
                Block testBlock = testState.getBlock();
                if (testBlock instanceof IForceNetworkBlock) {
                    int d1 = ((IForceNetworkBlock) testBlock).getDistance(world, testPos);
                    msg += ", with distance "+d1+", our current calculated distance is "+d;
                    if (d1 != -1 && (d1 < d || d==-1)) { //basically, if we are getting a distance from our neighbor, and they are closer than we are
                        msg += ", calculated distance was set to "+d1;
                        d = d1;
                    }
                    if (d1<myd || ((IForceNetworkBlock) testBlock).hasCloser(world, testPos, pos, d1)) {
                        hasCloser = true;
                        msg += ", encountered closer tube";
                    }
                }
            }
        }
        msg += "\n";
        //LOGGER.error(msg);
        return hasCloser ? d : -1;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_200123_1_, BlockGetter p_200123_2_, BlockPos p_200123_3_) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState p_220080_1_, BlockGetter p_220080_2_, BlockPos p_220080_3_) {
        return 1.0F;
    }

    public static void info(String msg) {
        AdvancedForcefields.LOGGER.info(msg);
    }

    @Override
    public void fallOn(Level world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (entity.isSuppressingBounce()) {
            super.fallOn(world, state, pos, entity, fallDistance);
        } else {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof ForceTubeTileEntity) {
                ForceTubeTileEntity forceTile = (ForceTubeTileEntity) tile;
                if (this.getFallDamageType(entity, forceTile) == FallDamageType.DAMAGE) {
                    super.fallOn(world, state, pos, entity, fallDistance);
                } else {
                    entity.causeFallDamage(fallDistance, 0.0F, DamageSource.FALL);
                }
            }
        }
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter world, Entity entity) {
        BlockPos pos = entity.getOnPos();
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ForceTubeTileEntity forceTile = (ForceTubeTileEntity) tile;
            this.runOnCollide(entity, forceTile, this.getCollisionType(entity, forceTile), ForceInteractionType.LAND_ON);
        }
        super.updateEntityAfterFallOn(world, entity);
    }

    @Override
    public void stepOn(Level world, BlockPos pos, BlockState state, Entity entity) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ForceTubeTileEntity forceTile = (ForceTubeTileEntity) tile;
            this.runOnCollide(entity, forceTile, this.getCollisionType(entity, forceTile), ForceInteractionType.STEP_ON);
        }

        super.stepOn(world, pos, state, entity);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ForceTubeTileEntity forceTile = (ForceTubeTileEntity) tile;
            this.runOnCollide(entity, forceTile, this.getCollisionType(entity, forceTile), ForceInteractionType.INSIDE);
        }

        super.entityInside(state, world, pos, entity);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter blockReader, BlockPos pos, CollisionContext context) {
        VoxelShape shape = super.getCollisionShape(state, blockReader, pos, context);
        VoxelShape empty = Shapes.empty();
        if (!shape.isEmpty() && context instanceof EntityCollisionContext && !(blockReader instanceof RenderChunkRegion)) {
            EntityCollisionContext entityContext = (EntityCollisionContext) context;
            Entity entity = entityContext.getEntity().orElse(null);

            BlockEntity tile = blockReader.getBlockEntity(pos);
            if (tile instanceof ForceTubeTileEntity && entity!=null) {
                ForceTubeTileEntity forceTubeTile = (ForceTubeTileEntity) tile;
                if (forceTubeTile.hasMasterPos()) {

                    //info("Entity [" + entity.getName().getString() + "] colliding with ForceTubeTileEntity at " + pos.toShortString());

                    CollisionType collisionType = this.getCollisionType(entity, forceTubeTile);

                    this.runOnCollide(entity, forceTubeTile, collisionType, ForceInteractionType.NEARBY);

                    boolean isColliding = false;
                    boolean isVeryNearby = false;

                    List<AABB> boxes = shape.move(pos.getX(), pos.getY(), pos.getZ()).toAabbs();

                    AABB entityBox = entity.getBoundingBox();

                    for (AABB box : boxes) {
                        AABB box2 = box.inflate(0.01);
                        AABB box3 = box.inflate(0.5);
                        if (entityBox.intersects(box2)) {
                            isColliding = true;
                        }
                        if (entityBox.intersects(box3)) {
                            isVeryNearby = true;
                        }
                        if (isColliding&&isVeryNearby) {
                            break;
                        }
                    }

                    if (isColliding) {
                        this.runOnCollide(entity, forceTubeTile, collisionType, ForceInteractionType.COLLIDE);
                    }

                    if (isVeryNearby) {
                        this.runOnCollide(entity, forceTubeTile, collisionType, ForceInteractionType.VERY_CLOSE);
                    }

                    if (collisionType == CollisionType.SOLID) {
                        return shape;
                    } else if (collisionType == CollisionType.EMPTY) {
                        return empty;
                    } else {
                        LOGGER.error("CollisionType of: " + collisionType.name() + ". Not sure how this happened, but it shouldn't have.");
                    }
                }
            }
        }
        return shape;
    }

    public CollisionType getCollisionType(Entity entity, ForceTubeTileEntity forceTubeTile) {
        CollisionType collisionType = CollisionType.SOLID;
        HashMap<Integer, ArrayList<ForceModifierSelector>> selectors = forceTubeTile.getSortedActionSelectors();
        if (selectors.size() > 0) {
            int max = Collections.max(selectors.keySet());
            int min = Collections.min(selectors.keySet());

            for (int i = min; i <= max; i++) {
                ArrayList<ForceModifierSelector> temp = selectors.get(i);
                if (temp == null || temp.isEmpty()) {
                    continue;
                }
                for (ForceModifierSelector sel : temp) {
                    if (sel.validForEntity(entity)) {
                        ForceModifierRegistry registered = RegistryInit.MODIFIER_REGISTRY.getValue(new ResourceLocation(sel.getAction()));
                        if (registered != null) {
                            CollisionType test = registered.getAction().collisionType();
                            if (test != CollisionType.INHERIT) {
                                collisionType = test;
                            }
                        }
                    }
                }
            }
        }
        return collisionType;
    }

    public BurningType getBurningType(Entity entity, ForceTubeTileEntity forceTubeTile) {
        BurningType burningType = BurningType.NO_BURN;
        HashMap<Integer, ArrayList<ForceModifierSelector>> selectors = forceTubeTile.getSortedActionSelectors();
        if (selectors.size() > 0) {
            int max = Collections.max(selectors.keySet());
            int min = Collections.min(selectors.keySet());

            for (int i = min; i <= max; i++) {
                ArrayList<ForceModifierSelector> temp = selectors.get(i);
                if (temp == null || temp.isEmpty()) {
                    continue;
                }
                for (ForceModifierSelector sel : temp) {
                    if (sel.validForEntity(entity)) {
                        ForceModifierRegistry registered = RegistryInit.MODIFIER_REGISTRY.getValue(new ResourceLocation(sel.getAction()));
                        if (registered != null) {
                            BurningType test = registered.getAction().burningType();
                            if (test != BurningType.INHERIT) {
                                burningType = test;
                            }
                        }
                    }
                }
            }
        }
        return burningType;
    }

    public FallDamageType getFallDamageType(Entity entity, ForceTubeTileEntity forceTubeTile) {
        FallDamageType damageType = FallDamageType.DAMAGE;
        HashMap<Integer, ArrayList<ForceModifierSelector>> selectors = forceTubeTile.getSortedActionSelectors();
        if (selectors.size() > 0) {
            int max = Collections.max(selectors.keySet());
            int min = Collections.min(selectors.keySet());

            for (int i = min; i <= max; i++) {
                ArrayList<ForceModifierSelector> temp = selectors.get(i);
                if (temp == null || temp.isEmpty()) {
                    continue;
                }
                for (ForceModifierSelector sel : temp) {
                    if (sel.validForEntity(entity)) {
                        ForceModifierRegistry registered = RegistryInit.MODIFIER_REGISTRY.getValue(new ResourceLocation(sel.getAction()));
                        if (registered != null) {
                            FallDamageType test = registered.getAction().fallDamageType();
                            if (test != FallDamageType.INHERIT) {
                                damageType = test;
                            }
                        }
                    }
                }
            }
        }
        return damageType;
    }

    public void runOnCollide(Entity entity, ForceTubeTileEntity forceTubeTile, CollisionType collisionType, ForceInteractionType interactionType) {
        HashMap<Integer, ArrayList<ForceModifierSelector>> selectors = forceTubeTile.getSortedActionSelectors();
        if (selectors.size() > 0) {
            int max = Collections.max(selectors.keySet());
            int min = Collections.min(selectors.keySet());

            for (int i = max; i >= min; i--) {
                ArrayList<ForceModifierSelector> temp = selectors.get(i);
                if (temp == null || temp.isEmpty()) {
                    continue;
                }
                for (ForceModifierSelector sel : temp) {
                    if (sel.validForEntity(entity)) {
                        ForceModifierRegistry registered = RegistryInit.MODIFIER_REGISTRY.getValue(new ResourceLocation(sel.getAction()));
                        if (registered != null) {
                            registered.getAction().onCollide(entity.level, forceTubeTile.getBlockPos(), entity, collisionType, interactionType, sel.getTriggerStack());
                        }
                    }
                }
            }
        }
    }

    protected void doParticlesForDirection(ForceNetworkDirection networkDirection, ForceTubeTileEntity ftte, Level world, BlockPos pos, Random rand) {
        int limit = 20;
        long now = world.getGameTime();
        long last_packet = networkDirection == ForceNetworkDirection.TO_MASTER ? ftte.getLastToMasterPacketGT() : ftte.getLatestToServantPacketGameTime();
        //LOGGER.log((now-last_packet)<= limit ? Level.ERROR : Level.INFO, "Trying particles for direction " + networkDirection.name() + ", last packet at: " + last_packet + ", time between then and now: " + (now-last_packet));
        if (true || now - last_packet <= limit) {
            //ramp up from 0% particles at 0% time to 100% particles at 15% time, and back down from 100% particles at 60% time to 0% particles at 100% time
            float percentTime = ((float) (now - last_packet)) / limit;
            double percentParticles = 0;
            if (percentTime > 0.00 && percentTime < 0.5) {
                percentParticles = percentTime * (1 / 0.15);
            } else if (percentTime >= 0.5 && percentTime <= 0.90) {
                percentParticles = 1.00;
            } else if (percentTime > 0.90 && percentTime < 1.00) {
                percentParticles = (1.00 - percentTime) * (1 / 0.4);
            }
            percentParticles = 1.00d;
            //LOGGER.info("Looking for "+(networkDirection==ForceNetworkDirection.TO_MASTER?"closer":"farther")+" nodes");
            //LOGGER.info("Starting to look for positions, there are: "+(networkDirection==ForceNetworkDirection.TO_MASTER ? ftte.getCloserNodes() : ftte.getFartherNodes()).size());
            ArrayList<BlockPos> neighbors = ftte.getConnectedNodes();//networkDirection == ForceNetworkDirection.TO_MASTER ? ftte.getCloserNodes() : ftte.getFartherNodes();
            for (int i = 0; i < 5; i++) {
                if (rand.nextDouble() < percentParticles) {
                    BlockPos otherPos;
                    if (neighbors.size()>0) {
                        otherPos = (BlockPos) MiscUtil.randomChoice(neighbors, rand);
                    } else {
                        otherPos = pos.above();
                    }
                    Vec3i dir = new Vec3i(pos.getX() - otherPos.getX(), pos.getY() - otherPos.getY(), pos.getZ() - otherPos.getZ()); //from me to otherPos
                    double lower = 0;//0.48
                    double upper = 0.3;//0.6
                    double offsetX = (dir.getX() == 0 ? MiscUtil.randomSignedDouble(lower, upper, rand) : 0);
                    double offsetY = (dir.getY() == 0 ? MiscUtil.randomSignedDouble(lower, upper, rand) : 0);
                    double offsetZ = (dir.getZ() == 0 ? MiscUtil.randomSignedDouble(lower, upper, rand) : 0);

                    double startX = pos.getX() + offsetX + 0.5;
                    double startY = pos.getY() + offsetY + 0.5;
                    double startZ = pos.getZ() + offsetZ + 0.5;

                    double endX = otherPos.getX() + 0.5;
                    double endY = otherPos.getY() + 0.5;
                    double endZ = otherPos.getZ() + 0.5;

                    double betweenPercent = MiscUtil.randomDouble(0.0d, 0.5d, rand);
                    startX = MiscUtil.lerp(startX, endX, betweenPercent);
                    startY = MiscUtil.lerp(startY, endY, betweenPercent);
                    startZ = MiscUtil.lerp(startZ, endZ, betweenPercent);

                    int[] intcolor = ColorUtil.unpackRGBA(ftte.getColor());

                    float red = intcolor[0] / 255.0f;
                    float green = intcolor[1] / 255.0f;
                    float blue = intcolor[2] / 255.0f;
                    float alpha = intcolor[3] / 255.0f;
                    float scale = Math.max(1.0f, (1 - alpha) * 2.0f);
                    double speed = 1000.0f;
                    if (alpha > 0.001) {
                        world.addParticle(new DustParticleOptions(new Vector3f(1.0f-red, 1.0f-green, 1.0f-blue), scale), startX, startY, startZ, (startX-endX)*speed, (startY-endY)*speed, (startZ-endZ)*speed);
                    }
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState blockState, Level world, BlockPos pos, Random rand) {
        super.animateTick(blockState, world, pos, rand);
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ForceTubeTileEntity) {
            ForceTubeTileEntity ftte = (ForceTubeTileEntity) te;
            if (ClientUtils.mc().player!=null && MiscUtil.isPlayerWearingShimmeringHelmet(ClientUtils.mc().player) && ClientUtils.mc().player.isShiftKeyDown()) {
                doParticlesForDirection(ForceNetworkDirection.TO_MASTER, ftte, world, pos, rand);
            }
            //doParticlesForDirection(ForceNetworkDirection.TO_SERVANTS, ftte, world, pos, rand);
            //if received to servant or to master packet, spawn particles in respective direction, see below (we show the directions we are propogating)
            //spawn DUST particles going from lower blocks to me, and from me to higher blocks (edges of this block to center, and vice versa)
        }
    }
}

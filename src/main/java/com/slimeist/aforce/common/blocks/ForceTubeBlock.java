package com.slimeist.aforce.common.blocks;

import com.google.common.collect.Sets;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import com.slimeist.aforce.common.tiles.ForceControllerTileEntity;
import com.slimeist.aforce.common.tiles.ForceModifierTileEntity;
import com.slimeist.aforce.common.tiles.ForceNetworkTileEntity;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import com.slimeist.aforce.common.tiles.helpers.ForceModifierSelector;
import com.slimeist.aforce.core.enums.BurningType;
import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.FallDamageType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import com.slimeist.aforce.core.init.RegistryInit;
import com.slimeist.aforce.core.interfaces.IForceNetworkBlock;
import com.slimeist.aforce.core.util.RenderLayerHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.EntitySelectionContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

public class ForceTubeBlock extends BasePipeBlock implements IForceNetworkBlock {

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
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED);
    }

    @Override
    public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, worldIn, pos, oldState, isMoving);
        this.doUpdate(state, worldIn, pos);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        //super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        this.doUpdate(state, worldIn, pos);
    }

    private void doUpdate(BlockState state, World worldIn, BlockPos pos) {
        if (!worldIn.isClientSide) {
            this.updateDistance(worldIn, pos, state);
            this.markTEDirty(worldIn, pos);
        }

        BlockState targetState = getTargetState(worldIn, pos, state.getValue(WATERLOGGED), this.getInternalDistance(worldIn, pos)!=-1);
        if(!targetState.equals(state)) {
            worldIn.setBlock(pos, targetState, Constants.BlockFlags.DEFAULT | Constants.BlockFlags.NO_RERENDER);
        }
    }

    private BlockState getTargetState(World worldIn, BlockPos pos, boolean waterlog, boolean enabled) {
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
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (state.getValue(ENABLED)) {
            return VoxelShapes.block();
        }
        return super.getShape(state, worldIn, pos, context);
    }

    @Override
    public boolean isMatchingBlock(BlockState mystate, BlockState neighborstate, BlockPos mypos, BlockPos neighborpos, IBlockReader blockReader) {
        return this.isMatchingBlock(mypos, neighborpos, blockReader);
    }

    public boolean isMatchingBlock(BlockPos mypos, BlockPos neighborpos, IBlockReader blockReader) {
        TileEntity myTile = blockReader.getBlockEntity(mypos);
        if (myTile instanceof ForceTubeTileEntity) {
            return ((ForceTubeTileEntity) myTile).canConnect(neighborpos);
        }
        return false;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState block, IBlockReader world) {
        return new ForceTubeTileEntity();
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tile = worldIn.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            return ((ForceTubeTileEntity) tile).getComparatorOutput();
        }
        return 0;
    }

    @Override
    public int getDistance(World world, BlockPos pos) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity && this.shouldSignal(world, pos)) {
            int distance = ((ForceNetworkTileEntity) tile).getDistance();
            //LOGGER.info("getDistance @ "+pos.toShortString()+" (shouldSignal: "+this.shouldSignal(world, pos)+"), returning "+distance);
            return distance;
        }
        //LOGGER.info("getDistance @ "+pos.toShortString()+" (shouldSignal: "+this.shouldSignal(world, pos)+"), returning -1, because we did not find a good TE, or shouldn't signal");
        return -1;
    }

    private int getInternalDistance(World world, BlockPos pos) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            int distance = ((ForceNetworkTileEntity) tile).getDistance();
            //LOGGER.info("getInteranlDistance @ "+pos.toShortString()+" returning "+distance);
            return distance;
        }
        //LOGGER.info("getInternalDistance @ "+pos.toShortString()+" returning -1, because we did not find a good TE");
        return -1;
    }

    @Override
    public void setDistance(World world, BlockPos pos, int distance) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            ((ForceNetworkTileEntity) tile).setDistance(distance);
        }
    }

    @Override
    public boolean hasCloser(World world, BlockPos pos, BlockPos bannedPos, int distance) {
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

    public boolean isConnected(World world, BlockPos pos, BlockPos otherPos) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            return ((ForceNetworkTileEntity) tile).isConnected(otherPos);
        } else {
            LOGGER.error("Did not find expected ForceNetworkTileEntity at ["+pos+"], when checking whether connected to ["+otherPos+"]");
        }
        return false;
    }

    public boolean hasMasterPos(World world, BlockPos pos) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            return ((ForceNetworkTileEntity) tile).hasMasterPos();
        } else {
            LOGGER.error("Did not find expected ForceNetworkTileEntity at ["+pos+"], when checking whether it hasMasterPos");
        }
        return false;
    }

    public boolean canConnect(World world, BlockPos pos, BlockPos otherPos) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            return ((ForceNetworkTileEntity) tile).canConnect(otherPos);
        } else {
            LOGGER.error("Did not find expected ForceNetworkTileEntity at [" + pos + "], when checking whether can connect to [" + otherPos + "]");
        }
        return false;
    }

    public boolean shouldSignal(World world, BlockPos pos) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            return ((ForceTubeTileEntity) tile).isSignalling();
        } else {
            LOGGER.error("Did not find expected ForceTubeTileEntity at [" + pos + "], when checking whether should signal");
        }
        return false;
    }

    public void setSignalling(World world, BlockPos pos, boolean shouldSignal) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ((ForceTubeTileEntity) tile).setSignalling(shouldSignal);
        } else {
            LOGGER.error("Did not find expected ForceTubeTileEntity at [" + pos + "], when setting shouldSignal");
        }
    }

    private void networkDisconnect(World world, BlockPos pos) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ((ForceTubeTileEntity) tile).networkDisconnect();
        } else {
            LOGGER.error("Did not find expected ForceTubeTileEntity at [" + pos + "], when disconnecting from network");
        }
    }

    private void markTEDirty(World world, BlockPos pos) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ((ForceTubeTileEntity) tile).setChanged();
            BlockState state = world.getBlockState(pos);
            world.sendBlockUpdated(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE);
            //LOGGER.info("Marked ForceTubeTileEntity at [" + pos + "] as dirty");
        } else {
            LOGGER.error("Did not find expected ForceTubeTileEntity at [" + pos + "], when marking TE as dirty");
        }
    }

    private void updateDistance(World world, BlockPos pos, BlockState state) {
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

    private int calculateTargetDistance(World world, BlockPos pos) {
        this.setSignalling(world, pos, false);
        int dist = this.getLeastNeighborDistance(world, pos);
        this.setSignalling(world, pos, true);
        if (dist==-1) {
            return dist;
        } else {
            return dist+1;
        }
    }

    private int getLeastNeighborDistance(World world, BlockPos pos) {
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

    /*@Override
    public VoxelShape getVisualShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }*/

    public static void info(String msg) {
        AdvancedForcefields.LOGGER.info(msg);
    }

    @Override
    public void fallOn(World world, BlockPos pos, Entity entity, float fallDistance) {
        if (entity.isSuppressingBounce()) {
            super.fallOn(world, pos, entity, fallDistance);
        } else {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof ForceTubeTileEntity) {
                ForceTubeTileEntity forceTile = (ForceTubeTileEntity) tile;
                if (this.getFallDamageType(entity, forceTile) == FallDamageType.DAMAGE) {
                    super.fallOn(world, pos, entity, fallDistance);
                } else {
                    entity.causeFallDamage(fallDistance, 0.0F);
                }
            }
        }
    }

    @Override
    public void updateEntityAfterFallOn(IBlockReader world, Entity entity) {
        BlockPos pos = entity.getOnPos();
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ForceTubeTileEntity forceTile = (ForceTubeTileEntity) tile;
            this.runOnCollide(entity, forceTile, this.getCollisionType(entity, forceTile), ForceInteractionType.LAND_ON);
        }
    }

    @Override
    public void stepOn(World world, BlockPos pos, Entity entity) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ForceTubeTileEntity forceTile = (ForceTubeTileEntity) tile;
            this.runOnCollide(entity, forceTile, this.getCollisionType(entity, forceTile), ForceInteractionType.STEP_ON);
        }

        super.stepOn(world, pos, entity);
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ForceTubeTileEntity forceTile = (ForceTubeTileEntity) tile;
            this.runOnCollide(entity, forceTile, this.getCollisionType(entity, forceTile), ForceInteractionType.INSIDE);
        }

        super.entityInside(state, world, pos, entity);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = super.getCollisionShape(state, blockReader, pos, context);
        VoxelShape empty = VoxelShapes.empty();
        if (!shape.isEmpty() && context instanceof EntitySelectionContext && !(blockReader instanceof ChunkRenderCache)) {
            EntitySelectionContext entityContext = (EntitySelectionContext) context;
            Entity entity = entityContext.getEntity();

            TileEntity tile = blockReader.getBlockEntity(pos);
            if (tile instanceof ForceTubeTileEntity && entity!=null) {
                ForceTubeTileEntity forceTubeTile = (ForceTubeTileEntity) tile;
                if (forceTubeTile.hasMasterPos()) {

                    //info("Entity [" + entity.getName().getString() + "] colliding with ForceTubeTileEntity at " + pos.toShortString());

                    CollisionType collisionType = this.getCollisionType(entity, forceTubeTile);

                    this.runOnCollide(entity, forceTubeTile, collisionType, ForceInteractionType.NEARBY);

                    boolean isColliding = false;
                    boolean isVeryNearby = false;

                    List<AxisAlignedBB> boxes = shape.move(pos.getX(), pos.getY(), pos.getZ()).toAabbs();

                    AxisAlignedBB entityBox = entity.getBoundingBox();

                    for (AxisAlignedBB box : boxes) {
                        AxisAlignedBB box2 = box.inflate(0.01);
                        AxisAlignedBB box3 = box.inflate(0.5);
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
}

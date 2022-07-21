package com.slimeist.aforce.common.blocks;

import com.slimeist.aforce.common.tiles.ForceModifierTileEntity;
import com.slimeist.aforce.common.tiles.SimpleForceModifierTileEntity;
import com.slimeist.aforce.common.tiles.ForceNetworkTileEntity;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.interfaces.IForceNetworkBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import static com.slimeist.aforce.AdvancedForcefields.LOGGER;

import javax.annotation.Nullable;

public class ForceModifierBlock extends BaseEntityBlock implements IForceNetworkBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ForceModifierBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(POWERED, false)
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(POWERED);
    }

    public BlockState getStateForPlacement(BlockPlaceContext p_196258_1_) {
        return this.defaultBlockState().setValue(FACING, p_196258_1_.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SimpleForceModifierTileEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_) {
        return createTickerHelper(p_153214_, TileEntityTypeInit.FORCE_MODIFIER_TYPE, SimpleForceModifierTileEntity::tick);
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, worldIn, pos, oldState, isMoving);
        this.doUpdate(state, worldIn, pos);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (placer instanceof Player) {
            BlockEntity myTile = worldIn.getBlockEntity(pos);
            if (myTile instanceof ForceModifierTileEntity) {
                ForceModifierTileEntity modifieTE = (ForceModifierTileEntity) myTile;
                modifieTE.owner = placer.getName().getString();
            }
        }
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
        BlockEntity tile = world.getBlockEntity(pos);
        if(tile instanceof ForceModifierTileEntity)
            return ((ForceModifierTileEntity)tile).canEntityDestroy(entity);
        return super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        this.doUpdate(state, worldIn, pos);
    }

    private void doUpdate(BlockState state, Level worldIn, BlockPos pos) {
        if (!worldIn.isClientSide) {
            this.updateDistance(worldIn, pos, state);
        }

        BlockEntity myTile = worldIn.getBlockEntity(pos);
        if (myTile instanceof ForceModifierTileEntity) {
            boolean powered = worldIn.hasNeighborSignal(pos);
            if (powered!=state.getValue(POWERED)) {
                worldIn.setBlock(pos, state.setValue(POWERED, powered), UPDATE_CLIENTS);
                ForceModifierTileEntity modifierTile = (ForceModifierTileEntity) myTile;
                if (powered) {
                    modifierTile.onPowered();
                } else {
                    modifierTile.onDepowered();
                }
            }
        }
    }

    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.BLOCK;
    }

    public boolean shouldSignal(Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceModifierTileEntity) {
            return ((ForceModifierTileEntity) tile).isSignalling();
        } else {
            LOGGER.error("Did not find expected ForceModifierTileEntity at [" + pos + "], when checking whether should signal");
        }
        return false;
    }

    public void setSignalling(Level world, BlockPos pos, boolean shouldSignal) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceModifierTileEntity) {
            ((ForceModifierTileEntity) tile).setSignalling(shouldSignal);
        } else {
            LOGGER.error("Did not find expected ForceModifierTileEntity at [" + pos + "], when setting shouldSignal");
        }
    }

    private void networkDisconnect(Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceModifierTileEntity) {
            ((ForceModifierTileEntity) tile).networkDisconnect();
        } else {
            LOGGER.error("Did not find expected ForceModifierTileEntity at [" + pos + "], when disconnecting from network");
        }
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

    public boolean canConnect(Level world, BlockPos pos, BlockPos otherPos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            return ((ForceNetworkTileEntity) tile).canConnect(otherPos);
        } else {
            LOGGER.error("Did not find expected ForceNetworkTileEntity at [" + pos + "], when checking whether can connect to [" + otherPos + "]");
        }
        return false;
    }

    /*
    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random rnd) {
        if (state.getValue(POWERED)) {
            double d0 = (double)pos.getX() + 0.5D;
            double d1 = (double)pos.getY();
            double d2 = (double)pos.getZ() + 0.5D;
            if (rnd.nextDouble() < 0.1D) {
                world.playLocalSound(d0, d1, d2, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction direction = state.getValue(FACING);
            Direction.Axis direction$axis = direction.getAxis();
            double d3 = 0.52D;
            double d4 = rnd.nextDouble() * 0.6D - 0.3D;
            double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.52D : d4;
            double d6 = rnd.nextDouble() * 9.0D / 16.0D;
            double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : d4;
            world.addParticle(ParticleTypes.PORTAL, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
        }
    }*/

    public BlockState rotate(BlockState p_185499_1_, Rotation p_185499_2_) {
        return p_185499_1_.setValue(FACING, p_185499_2_.rotate(p_185499_1_.getValue(FACING)));
    }

    public BlockState mirror(BlockState p_185471_1_, Mirror p_185471_2_) {
        return p_185471_1_.rotate(p_185471_2_.getRotation(p_185471_1_.getValue(FACING)));
    }

    // Called when the block is right clicked
    // In this block it is used to open the block gui when right clicked by a player
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if (worldIn.isClientSide) return InteractionResult.SUCCESS; // on client side, don't do anything

        MenuProvider namedContainerProvider = this.getMenuProvider(state, worldIn, pos);
        if (namedContainerProvider != null) {
            if (!(player instanceof ServerPlayer)) return InteractionResult.FAIL;  // should always be true, but just in case...
            ServerPlayer serverPlayerEntity = (ServerPlayer)player;
            BlockEntity tile = worldIn.getBlockEntity(pos);
            if (tile instanceof ForceModifierTileEntity && ((ForceModifierTileEntity) tile).canUseGui(player)) {
                NetworkHooks.openGui(serverPlayerEntity, namedContainerProvider, (packetBuffer) -> {
                    packetBuffer.writeBlockPos(pos);
                });
            }
            // (packetBuffer)->{} is just a do-nothing because we have no extra data to send
        }
        return InteractionResult.SUCCESS;
    }

    // This is where you can do something when the block is broken. In this case drop the inventory's contents
    // Code is copied directly from vanilla eg ChestBlock, CampfireBlock
    @Override
    public void onRemove(BlockState state, Level world, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileentity = world.getBlockEntity(blockPos);
            if (tileentity instanceof ForceModifierTileEntity) {
                ForceModifierTileEntity tileEntityModifier = (ForceModifierTileEntity) tileentity;
                tileEntityModifier.dropAllContents(world, blockPos);
            }
//      worldIn.updateComparatorOutputLevel(pos, this);  if the inventory is used to set redstone power for comparators
            super.onRemove(state, world, blockPos, newState, isMoving);  // call it last, because it removes the TileEntity
        }
    }

    // render using a BakedModel
    // required because the default (super method) is INVISIBLE for BlockContainers.
    @Override
    public RenderShape getRenderShape(BlockState iBlockState) {
        return RenderShape.MODEL;
    }
}
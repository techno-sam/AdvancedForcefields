package com.slimeist.aforce.common.blocks;

import com.slimeist.aforce.common.tiles.ForceControllerTileEntity;
import com.slimeist.aforce.common.tiles.ForceNetworkTileEntity;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.interfaces.IForceNetworkBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Random;

public class ForceControllerBlock extends BaseEntityBlock implements IForceNetworkBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ForceControllerBlock(Properties properties) {
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
        return new ForceControllerTileEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_) {
        return createTickerHelper(p_153214_, TileEntityTypeInit.FORCE_CONTROLLER_TYPE, ForceControllerTileEntity::tick);
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
            if (myTile instanceof ForceControllerTileEntity) {
                ForceControllerTileEntity controllerTE = (ForceControllerTileEntity) myTile;
                controllerTE.owner = placer.getName().getString();
            }
        }
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
        BlockEntity tile = world.getBlockEntity(pos);
        if(tile instanceof ForceControllerTileEntity)
            return ((ForceControllerTileEntity)tile).canEntityDestroy(entity);
        return super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        this.doUpdate(state, worldIn, pos);
    }

    private void doUpdate(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity myTile = worldIn.getBlockEntity(pos);
        if (myTile instanceof ForceControllerTileEntity) {
            boolean powered = worldIn.hasNeighborSignal(pos);
            if (powered!=state.getValue(POWERED)) {
                worldIn.setBlock(pos, state.setValue(POWERED, powered), UPDATE_CLIENTS);
                ForceControllerTileEntity controllerTile = (ForceControllerTileEntity) myTile;
                if (powered) {
                    controllerTile.onPowered();
                } else {
                    controllerTile.onDepowered();
                }
            }
        }
    }

    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.BLOCK;
    }

    @Override
    public int getDistance(Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceNetworkTileEntity) {
            return ((ForceNetworkTileEntity) tile).getDistance();
        }
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
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, Random rnd) {
        if (state.getValue(POWERED)) {
            double d0 = (double)pos.getX() + 0.5D;
            double d1 = (double)pos.getY();
            double d2 = (double)pos.getZ() + 0.5D;
            if (rnd.nextDouble() < 0.1D) {
                world.playLocalSound(d0, d1, d2, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
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
    }

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
            if (tile instanceof ForceControllerTileEntity && ((ForceControllerTileEntity) tile).canUseGui(player)) {
                NetworkHooks.openGui(serverPlayerEntity, namedContainerProvider, (packetBuffer) -> {
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
            if (tileentity instanceof ForceControllerTileEntity) {
                ForceControllerTileEntity tileEntityController = (ForceControllerTileEntity) tileentity;
                tileEntityController.dropAllContents(world, blockPos);
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
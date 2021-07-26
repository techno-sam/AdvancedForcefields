package com.slimeist.aforce.common.blocks;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.common.tiles.ForceControllerTileEntity;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import com.slimeist.aforce.core.util.TileEntityHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.EntitySelectionContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class ForceTubeBlock extends BasePipeBlock {

    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public ForceTubeBlock(@Nonnull Properties properties) {
        super(properties);

        registerDefaultState(defaultBlockState()
                .setValue(ENABLED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ENABLED);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        BlockState targetState = getTargetState(worldIn, pos, state.getValue(WATERLOGGED), state.getValue(ENABLED));
        if(!targetState.equals(state))
            worldIn.setBlock(pos, targetState, 2 | 4);
    }

    private BlockState getTargetState(World worldIn, BlockPos pos, boolean waterlog, boolean enabled) {
        BlockState newState = defaultBlockState();
        newState = newState.setValue(WATERLOGGED, waterlog);
        newState = newState.setValue(ENABLED, enabled);

        for(Direction facing : Direction.values()) {
            BooleanProperty prop = CONNECTIONS[facing.ordinal()];

            BlockPos neighborpos = pos.offset(facing.getNormal()); //(worldIn, pos, facing);
            BlockState neighborstate = worldIn.getBlockState(neighborpos);
            boolean matching = isMatchingBlock(neighborstate.getBlock());

            newState = newState.setValue(prop, matching);
        }

        return newState;
    }

    /*@Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        super.tick(state, world, pos, rand);
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof ForceTubeTileEntity) {
            ForceTubeTileEntity tubeTile = (ForceTubeTileEntity) tile;
            AdvancedForcefields.LOGGER.info("Ticking ForceTubeBlock with a correct tile");
            boolean hasMaster = tubeTile.hasMaster();
            if (hasMaster!=state.getValue(ENABLED)) {
                AdvancedForcefields.LOGGER.info("ENABLED state of: "+state.getValue(ENABLED)+", and hasMaster() state of: "+hasMaster+", is inconsistent");
                //state = state.getBlockState();
                state = state.setValue(ENABLED, hasMaster);
                AdvancedForcefields.LOGGER.info("Changed state to: "+state.toString());
                world.setBlockAndUpdate(pos, state);
            }
        }
        world.getBlockTicks().scheduleTick(pos, this, 10);
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState state1, boolean p_220082_5_) {
        if (!world.isClientSide) {
            //world.getBlockTicks().scheduleTick(pos, this, 1);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return super.getStateForPlacement(context).setValue(ENABLED, false);
    }*/

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (state.getValue(ENABLED)) {
            return VoxelShapes.block();
        }
        return super.getShape(state, worldIn, pos, context);
    }


    /*@Override
    public void entityInside(BlockState blockstate, World world, BlockPos blockpos, Entity entity) {
        super.entityInside(blockstate, world, blockpos, entity);
        if (entity.isColliding(blockpos, blockstate)) {
            entity.makeStuckInBlock(blockstate, new Vector3d(0.9F, 0.9D, 0.9F));
        }
    }*/

    @Override
    public boolean isMatchingBlock(Block block) {
        return block.is(AdvancedForcefieldsTags.Blocks.FORCE_TUBE);
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
    public void setPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        ForceTubeTileEntity.updateNeighbors(world, pos, state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader blockReader, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = super.getCollisionShape(state, blockReader, pos, context);
        VoxelShape empty = VoxelShapes.empty();
        if (!shape.isEmpty() && context instanceof EntitySelectionContext && !(blockReader instanceof ChunkRenderCache)) {
            EntitySelectionContext entityContext = (EntitySelectionContext) context;
            Entity entity = entityContext.getEntity();

            TileEntity tile = blockReader.getBlockEntity(pos);
            if (tile instanceof ForceTubeTileEntity) {
                ForceTubeTileEntity forceTubeTile = (ForceTubeTileEntity) tile;
                if (forceTubeTile.hasMaster()) {
                    BlockPos masterPos = forceTubeTile.getMasterPos();
                    if (masterPos != null) {
                        //AdvancedForcefields.LOGGER.warn("getting collision box for a forceTube with a controller at: " + masterPos.toString());
                        TileEntity masterTile = TileEntityHelper.getTile(ForceControllerTileEntity.class, blockReader, masterPos).orElse(null);
                        if (masterTile instanceof ForceControllerTileEntity) {
                            ForceControllerTileEntity controllerTile = (ForceControllerTileEntity) masterTile;
                            int blockingmode = controllerTile.getBlockingMode();
                            switch (blockingmode) {
                                case 1: //Block NONE
                                    return empty;
                                case 2: //Block ALL
                                    return shape;
                                case 3: //Block PLAYERS
                                    if (entity instanceof PlayerEntity) {
                                        return shape;
                                    } else {
                                        return empty;
                                    }
                                case 4: //Block MOBS
                                    if (entity instanceof MobEntity) {
                                        return shape;
                                    } else {
                                        return empty;
                                    }
                            }
                        }
                    }
                }
            }
        }
        return shape;
    }
}

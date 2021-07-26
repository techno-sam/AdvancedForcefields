package com.slimeist.aforce.common.blocks;

import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.core.util.RenderLayerHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nonnull;

public class BasePipeBlock extends Block implements IWaterLoggable { //derived from vazkii's Quark Oddities Pipe Block

    private static final VoxelShape CENTER_SHAPE = VoxelShapes.box(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875);

    private static final VoxelShape DOWN_SHAPE = VoxelShapes.box(0.3125, 0, 0.3125, 0.6875, 0.6875, 0.6875);
    private static final VoxelShape UP_SHAPE = VoxelShapes.box(0.3125, 0.3125, 0.3125, 0.6875, 1, 0.6875);
    private static final VoxelShape NORTH_SHAPE = VoxelShapes.box(0.3125, 0.3125, 0, 0.6875, 0.6875, 0.6875);
    private static final VoxelShape SOUTH_SHAPE = VoxelShapes.box(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 1);
    private static final VoxelShape WEST_SHAPE = VoxelShapes.box(0, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875);
    private static final VoxelShape EAST_SHAPE = VoxelShapes.box(0.3125, 0.3125, 0.3125, 1, 0.6875, 0.6875);

    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    @SuppressWarnings("unchecked")
    protected static final BooleanProperty[] CONNECTIONS = new BooleanProperty[] {
            DOWN, UP, NORTH, SOUTH, WEST, EAST
    };

    private static final VoxelShape[] SIDE_BOXES = new VoxelShape[] {
            DOWN_SHAPE, UP_SHAPE, NORTH_SHAPE, SOUTH_SHAPE, WEST_SHAPE, EAST_SHAPE
    };

    private static final VoxelShape[] shapeCache = new VoxelShape[64];

    public BasePipeBlock(@Nonnull Properties properties) {
        super(properties);

        registerDefaultState(defaultBlockState()
                .setValue(DOWN, false).setValue(UP, false)
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(WEST, false).setValue(EAST, false)
                .setValue(WATERLOGGED, false)
        );

        RenderLayerHandler.setRenderType(this, RenderLayerHandler.RenderTypeSkeleton.CUTOUT_MIPPED);
    }

    @Override
    public boolean isToolEffective(BlockState state, ToolType tool) {
        return tool == ToolType.PICKAXE;
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        BlockState targetState = getTargetState(worldIn, pos, state.getValue(WATERLOGGED));
        if(!targetState.equals(state))
            worldIn.setBlock(pos, targetState, 2 | 4);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getTargetState(context.getLevel(), context.getClickedPos(), context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    public boolean isMatchingBlock(Block block) {
        return block.is(Tags.Blocks.GLASS);
    }

    private BlockState getTargetState(World worldIn, BlockPos pos, boolean waterlog) {
        BlockState newState = defaultBlockState();
        newState = newState.setValue(WATERLOGGED, waterlog);

        for(Direction facing : Direction.values()) {
            BooleanProperty prop = CONNECTIONS[facing.ordinal()];

            BlockPos neighborpos = pos.offset(facing.getNormal()); //(worldIn, pos, facing);
            BlockState neighborstate = worldIn.getBlockState(neighborpos);
            boolean matching = isMatchingBlock(neighborstate.getBlock());

            newState = newState.setValue(prop, matching);
        }

        return newState;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        int index = 0;
        for(Direction dir : Direction.values()) {
            int ord = dir.ordinal();
            if(state.getValue(CONNECTIONS[ord]))
                index += (1 << ord);
        }

        VoxelShape cached = shapeCache[index];
        if(cached == null) {
            VoxelShape currShape = CENTER_SHAPE;

            for(Direction dir : Direction.values()) {
                boolean connected = isConnected(state, dir);
                if(connected)
                    currShape = VoxelShapes.or(currShape, SIDE_BOXES[dir.ordinal()]);
            }

            shapeCache[index] = currShape;
            cached = currShape;
        }

        return cached;
    }

    public static boolean isConnected(BlockState state, Direction side) {
        BooleanProperty prop = CONNECTIONS[side.ordinal()];
        return state.getValue(prop);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, SOUTH, WEST, EAST, WATERLOGGED);
    }
}

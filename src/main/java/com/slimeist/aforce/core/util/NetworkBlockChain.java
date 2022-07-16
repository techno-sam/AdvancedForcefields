package com.slimeist.aforce.core.util;

import com.slimeist.aforce.AdvancedForcefields;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import java.util.*;

public class NetworkBlockChain {

    protected static Direction[] DIRECTIONS = Direction.values();

    protected LevelAccessor world;
    protected BlockPos masterPos;
    protected int maxSearch; //how long to search before giving up
    protected BlockBehaviour.StatePredicate validTube;
    protected BlockBehaviour.StatePredicate validComponent;
    protected ArrayList<BlockPos> tubeBlocks; //positions of blocks linking our network together
    protected ArrayList<BlockPos> componentBlocks; //positions of blocks attached to our network (tubeBlocks + modifiers)
    protected HashMap<BlockPos, Integer> distances; //how far in network space each position is from the master

    public NetworkBlockChain(LevelAccessor world, BlockPos start, int maxSearch, BlockBehaviour.StatePredicate validTube, BlockBehaviour.StatePredicate validComponent) {
        this.world = world;
        this.masterPos = start;
        this.maxSearch = maxSearch;
        this.validTube = validTube;
        this.validComponent = validComponent;

        this.tubeBlocks = new ArrayList<BlockPos>();
        this.componentBlocks = new ArrayList<BlockPos>();
        this.distances = new HashMap<BlockPos, Integer>();
    }

    public LevelAccessor getWorld() {
        return this.world;
    }

    public BlockPos getMasterPos() {
        return this.masterPos;
    }

    public int getMaxSearch() {
        return this.maxSearch;
    }

    public ArrayList<BlockPos> getTubeBlocks() {
        return this.tubeBlocks;
    }

    public ArrayList<BlockPos> getComponentBlocks() {
        return this.componentBlocks;
    }

    public HashMap<BlockPos, Integer> getDistances() {
        return this.distances;
    }

    public int size() {
        return this.tubeBlocks.size();
    }

    protected boolean isValidTube(BlockPos pos) {
        return pos.equals(this.masterPos) || this.isValidTube(pos, this.getWorld().getBlockState(pos));
    }

    protected boolean isValidTube(BlockPos pos, BlockState state) {
        return this.validTube.test(state, this.getWorld(), pos);
    }

    protected boolean isValidComponent(BlockPos pos) {
        return pos.equals(this.masterPos) || this.isValidComponent(pos, this.getWorld().getBlockState(pos));
    }

    protected boolean isValidComponent(BlockPos pos, BlockState state) {
        return this.validComponent.test(state, this.getWorld(), pos);
    }

    public NetworkBlockChain runSearch() {
        this.tubeBlocks.clear();
        this.componentBlocks.clear();
        this.distances.clear();
        this.search();
        return this;
    }

    protected static void log(String msg) {
        AdvancedForcefields.LOGGER.info(msg);
    }

    protected void search() {
        ArrayDeque<StackEntry> stack = new ArrayDeque<StackEntry>();
        stack.push(new StackEntry(this.masterPos));
        this.distances.put(this.masterPos, 0);

        while (!stack.isEmpty()) {
            //log("NBC: searchSteps: " + searchSteps);
            if (this.size() > this.maxSearch) {
                //log("NBC giving up because size: " + this.size() + ", or searchSteps: " + this.searchSteps + ", exceed maxSearch: " + this.maxSearch);
                return;
            }
            StackEntry entry = stack.peek();

            if (entry != null) {
                BlockPos pos = entry.getPos();
                if (!entry.isChecked()) {
                    if (!TileEntityHelper.isBlockLoaded(this.getWorld(), pos)) {
                        stack.pop();
                        continue;
                    }
                    if (this.isValidComponent(pos) && !this.componentBlocks.contains(pos)) {
                        this.componentBlocks.add(pos);
                    }

                    if (pos != this.masterPos) {
                        int distance = this.getDistances().getOrDefault(pos, -1);
                        for (Direction dir : DIRECTIONS) {
                            if (distance == -1) {
                                distance = this.getDistances().getOrDefault(pos.relative(dir), -1);
                                if (distance != -1) {
                                    distance++;
                                }
                            } else {
                                int adjdist = this.getDistances().getOrDefault(pos.relative(dir), -1);
                                if (adjdist != -1) {
                                    distance = adjdist + 1;
                                }
                            }
                        }
                        this.getDistances().put(pos, distance);
                    }

                    if (!this.isValidTube(pos) || this.tubeBlocks.contains(pos)) {
                        stack.pop();
                        continue;
                    }

                    this.tubeBlocks.add(pos);
                    entry.setChecked();
                }

                Direction direction = entry.getNextDirection().orElse(null);
                if (direction != null) {
                    stack.push(new StackEntry(pos.relative(direction)));
                    continue;
                }
                stack.pop();
            }
        }
    }

    protected class StackEntry {

        protected final BlockPos pos;
        protected Deque<Direction> directions; //which directions are valid for searching
        protected boolean checked; //whether this has been checked for adding to tubeBlocks and componentBlocks

        protected StackEntry(BlockPos pos) {
            this.pos = pos;
        }

        public BlockPos getPos() {
            return pos;
        }

        public Optional<Direction> getNextDirection() {
            if (this.directions==null) {
                this.directions = new ArrayDeque<Direction>(Arrays.asList(DIRECTIONS));
            }

            if (this.directions.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(this.directions.pop());
        }

        public boolean isChecked() {
            return this.checked;
        }

        public void setChecked() {
            this.checked = true;
        }
    }
}

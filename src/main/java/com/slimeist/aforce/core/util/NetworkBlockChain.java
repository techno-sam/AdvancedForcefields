package com.slimeist.aforce.core.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.*;

public class NetworkBlockChain { //based on ForgeEndertech BlockChain
    protected static Direction[] DIRECTIONS = Direction.values();
    protected IWorld world;
    protected BlockPos masterPos;
    protected ArrayList<BlockPos> tubeBlocks; //blocks that link our network together
    protected ArrayList<BlockPos> componentBlocks; //blocks that are part of our network (tubes and modifiers)
    protected HashMap<BlockPos, Integer> distances; //how far in network space each position
    protected int maxSearch; //how many blocks we try to search through before we give up
    protected boolean stopSearch = false;

    protected AbstractBlock.IPositionPredicate validTube;
    protected AbstractBlock.IPositionPredicate validComponent;

    public NetworkBlockChain(IWorld world, BlockPos masterPos, int maxSearch, AbstractBlock.IPositionPredicate validTube, AbstractBlock.IPositionPredicate validComponent) {
        this.world = world;
        this.masterPos = masterPos;
        this.maxSearch = maxSearch;
        this.tubeBlocks = new ArrayList<BlockPos>();
        this.componentBlocks = new ArrayList<BlockPos>();
        this.distances = new HashMap<BlockPos, Integer>();
        this.validTube = validTube;
        this.validComponent = validComponent;
    }

    public NetworkBlockChain create() { //search, but prepare stuff first
        this.stopSearch = false;
        this.tubeBlocks.clear();
        this.componentBlocks.clear();
        this.distances.clear();
        this.search();
        return this;
    }

    public Direction[] getDirections() {
        return DIRECTIONS;
    }

    public IWorld getWorld() {
        return this.world;
    }

    public BlockPos getMasterPos() {
        return this.masterPos;
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
        return this.isValidTube(pos, this.getWorld().getBlockState(pos)) || pos.equals(this.masterPos);
    }

    protected boolean isValidTube(BlockPos pos, BlockState blockState) {
        return this.validTube.test(blockState, this.getWorld(), pos);
    }

    protected boolean isValidComponent(BlockPos pos) {
        return this.isValidComponent(pos, this.getWorld().getBlockState(pos)) || pos.equals(this.masterPos);
    }

    protected boolean isValidComponent(BlockPos pos, BlockState blockState) {
        return this.validComponent.test(blockState, this.getWorld(), pos);
    }

    protected void search() {
        ArrayDeque<SearchStackEntry> stack = new ArrayDeque<SearchStackEntry>();
        stack.push(new SearchStackEntry(this.masterPos));
        this.distances.put(this.masterPos, 0);

        while (!stack.isEmpty()) {
            if (this.size()>this.maxSearch) {
                return;
            }
            SearchStackEntry entry = stack.peek();
            if (entry!=null) {
                BlockPos pos = entry.getPos();
                if (!entry.isChecked()) {
                    if (!TileEntityHelper.isBlockLoaded(this.getWorld(), pos)) {
                        stack.pop();
                        continue;
                    }
                    if (this.isValidComponent(pos) && !this.componentBlocks.contains(pos)) {
                        this.componentBlocks.add(pos);
                    }
                    if (!this.isValidTube(pos) || this.tubeBlocks.contains(pos)) {
                        stack.pop();
                        continue;
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
                    this.tubeBlocks.add(pos);
                    entry.setChecked();
                }

                if (this.stopSearch) {
                    return;
                }
                Direction direction = entry.getNextDirection().orElse(null);
                if (direction != null) {
                    stack.push(new SearchStackEntry(pos.relative(direction)));
                    continue;
                }
                stack.pop();
            }
        }
    }

    protected class SearchStackEntry {//an entry in the stack to continue searching
        protected final BlockPos pos;
        protected Deque<Direction> directions; //in which directions should this entry be searched from
        protected boolean checked = false;

        public SearchStackEntry(BlockPos pos) {
            this.pos = pos;
        }

        public BlockPos getPos(){
            return this.pos;
        }

        public Optional<Direction> getNextDirection() {//what direction to search in next
            if (this.directions==null) {
                this.directions = new ArrayDeque<Direction>(Arrays.asList(NetworkBlockChain.this.getDirections()));
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
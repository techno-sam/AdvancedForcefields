package com.slimeist.aforce.client;

import com.slimeist.aforce.common.CommonProxy;
import net.minecraft.world.World;

import com.slimeist.aforce.client.util.ClientUtils;

public class ClientProxy extends CommonProxy {

    @Override
    public World getClientWorld() {
        return ClientUtils.mc().level;
    }

}

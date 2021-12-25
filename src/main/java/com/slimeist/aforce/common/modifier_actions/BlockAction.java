package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;

public class BlockAction implements IForceModifierAction {

    public BlockAction() {
    }

    @Override
    public CollisionType collisionType() {
        return CollisionType.SOLID;
    }
}

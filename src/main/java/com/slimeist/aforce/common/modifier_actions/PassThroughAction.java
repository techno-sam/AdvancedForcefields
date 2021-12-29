package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.FallDamageType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;

public class PassThroughAction implements IForceModifierAction {

    public PassThroughAction() {
    }

    @Override
    public CollisionType collisionType() {
        return CollisionType.EMPTY;
    }
}

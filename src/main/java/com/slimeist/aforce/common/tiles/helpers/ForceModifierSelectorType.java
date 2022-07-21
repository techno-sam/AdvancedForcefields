package com.slimeist.aforce.common.tiles.helpers;

import java.util.function.Supplier;

public enum ForceModifierSelectorType {
    BASE(BaseForceModifierSelector::new),
    SIMPLE(SimpleForceModifierSelector::new),
    ADVANCED(AdvancedForceModifierSelector::new),
    ;

    private final Supplier<? extends BaseForceModifierSelector> initializer;

    ForceModifierSelectorType(Supplier<? extends BaseForceModifierSelector> initializer) {
        this.initializer = initializer;
    }

    public BaseForceModifierSelector create() {
        return this.initializer.get();
    }
}

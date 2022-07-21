# Advanced Forcefields

By Slimeist for **Forge 1.16 - 1.18**.

## Credit

I wrote this as a pretty inexperienced modder, so I have borrowed some code from various mods, listed here:
* **Quark** by Vazkii - Pipe models and basic block code
* **Immersive Engineering** by BluSunrize - gui elements under [com.slimeist.aforce.client.gui.ie_elements](src/main/java/com/slimeist/aforce/client/gui/ie_elements) are directly copied,
  and [ContainerScreenForceModifier](src/main/java/com/slimeist/aforce/client/gui/ContainerScreenForceModifier.java) is based off of IE.
* **Reliquary** by P3pp3rF1y - [MobHelper](src/main/java/com/slimeist/aforce/core/util/MobHelper.java) is copied

## Guide

### Enderite

<img src="https://i.imgur.com/MMZa0n6.png" height="128" alt="Enderite Ore" title="Enderite Ore" hspace=5> <img hspace=5 src="https://i.imgur.com/S08XUHE.png" height="128" alt="Enderite Block" title="Enderite Block">

**Enderite**, as the name suggests is a new ore found in the End.
It can be smelted into ingots, which are used to craft the various force
blocks in this mod.

### Force Controller

<img src="https://i.imgur.com/dqZJayg.png" height="128" alt="Force Controller">
<br>
<img src="https://i.imgur.com/RE458ZC.png" height="256" alt="Force Controller GUI">

The **force controller** is the backbone of any force network.
When a **force controller** is powered by redstone, any attached
**force tubes** gain full block collision and obey blocking rules
set by **force modifiers**, or simply block all entities by default.

>***Note**: Force controllers are fueled using ender pearls or eyes
of ender, however currently fuel is ignored.*

### Force Tubes

<img src="https://i.imgur.com/rRLdJpW.png" height="128" title="4 force tubes demonstrating connection patterns" alt="Force Tubes">

**Force tubes** link to one another in all 6 directions. When **force tubes**
are linked into a network by a powered **force controller**, they will not connect
to other networks or unlinked tubes.

When a **force controller** is powered, **force tubes** display a color based on a mix
of stained glass colors placed in the **force controller**.

<img src="https://i.imgur.com/mYryueo.png" height="256" alt="Powered Force Tube Network">

### Force Modifiers

Multiple **force modifiers** can be included in a network, and their effects will
be applied based on the filters set within.

#### Basic Force Modifier

<img src="https://i.imgur.com/CzN5Lyb.png" height="128" alt="Force Modifier">
<br>
<img src="https://i.imgur.com/czPv8lH.png" height="256" alt="Force Modifier GUI">

Filters:

- *Blacklist toggle*: When checked, entities named any of the names listed to the
  left will not have the effect applied. When unchecked, only entities with names
  **matching** will have the effect applied.
- *Animals toggle*: Whether animals can be targeted
- *Players toggle*: Whether players can be targeted
- *Neutrals toggle*: Whether neutral mobs can be targeted
- *Priority*: Order in which effect will be applied when multiple modifiers are part
  of the same network. (Higher numbers are applied first)

#### Advanced Force Modifier

<img src="https://i.imgur.com/5ql1Zkp.png" height="128" alt="Advanced Force Modifier">
<br>
<img src="https://i.imgur.com/YBHRKrh.png" height="256" alt="Advanced Force Modifier GUI">

Filters:

- *Entity selector*: Similar to selector in commands, filters entities based on a
  variety of conditions listed on the [Minecraft Wiki Page](https://minecraft.fandom.com/wiki/Target_selectors)
- *Blacklist toggle*: When checked, the entity selector's behavior will be inverted
- *Priority*: Order in which effect will be applied when multiple modifiers are part
  of the same network. (Higher numbers are applied first)

#### Effects:

| Item | Description |
| ---- | ----------- |
| <img src="https://i.imgur.com/MI30bwv.png" height=56 title="Cobblestone"> | Prevent movement through |
| <img src="https://i.imgur.com/XwPj18N.png" height=56 title="Any Trapdoor"> | Allow movement through |
| <img src="https://i.imgur.com/210jJhd.png" height=56 title="Slime Block"> | Bounce entities, and apply knockback |
| <img src="https://i.imgur.com/EfoXk29.png" height=56 title="Magma Block"> | Burn entities on top, like magma blocks |
| <img src="https://i.imgur.com/WDhsqqQ.png" height=56 title="Blaze Rod"> | Light entities on fire |
| <img src="https://i.imgur.com/9ClkSPB.png" height=56 title="Shulker Shell"> | Give entities levitation, strength based on number of shells |
| <img src="https://i.imgur.com/fsLH9Gm.png" height=56 title="Any Lingering Potion"> | Give entities potion effect |
| <img src="https://i.imgur.com/48kQ6m5.png" height=56 title="Powder Snow Bucket"> | Freeze entities, like powder snow |

### Shimmering Armor

Does crazy stuff with invisibility. You'll have to try it to discover for yourself.

### Crafting

#### Shimmering Cloth

<img src="https://i.imgur.com/2Ks9raj.png">

#### Shimmering Armor

<img src="https://i.imgur.com/SsZeZAM.png">

#### Force Controller

<img src="https://i.imgur.com/sRToGX3.png">

#### Force Modifier

<img src="https://i.imgur.com/IFTHSUd.png">

#### Advanced Force Modifier

<img src="https://i.imgur.com/KDfPjml.png">

#### Force Tubes

<img src="https://i.imgur.com/9ZACpmm.png">
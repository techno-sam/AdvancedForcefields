##Network specifications
###Network components
* Three types of network components
    * **Master**:
        * Only one per network, network built when powered by redstone signal
    * **Tube**:
        * As many as you like per network, get assigned a number based on
    distance from master in network space
        * Can link to other components in all 6 directions, connections are
    locked when the network is built
    * **Modifier**
        * Will be implemented a bit later, a device that modifies the
    behavior of the field based on various attributes

###Building the network
* When **master** gets powered, spreads out through the network,
  giving each block a number, based on how far it is from the **master** in
  network space.
  
* Once the network is fully built, each tube gets locked, so that it cannot
join other tubes, and potentially create a network with multiple masters
  
###Network Data
* The **master** block will contain a list of all components part of it's network
* Three types of data
    * **Internal** data - created when network is built (largely used to transmit **shared** data)
        * Distance from master in network space
        * What directions we can connect in
        * BlockPos of master
        * 'Packets' that we are transmitting, and in which direction
          (to master, or outwards through the network)
        * Are we 'dirty' (do we need to re-send data)
        * Are we locked in yet
    * **Personal** data - independent of network status
        * Filters and other settings for **modifiers**
        * Endergy level for **masters**
    * **Shared** data - shared across entire network, updated by sending
    packets to the **master** (send to adjacent tube with lowest distance value),
      then the **master** sends it back out over the network (propogate to
      components with a higher distance value)
      	* Will be stored on master even when network deactivated
		* Data such as blocking mode (actually will be filters for modifiers)
        * Color for **force tubes**
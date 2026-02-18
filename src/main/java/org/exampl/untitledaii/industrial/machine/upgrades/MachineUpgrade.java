package org.exampl.untitledaii.industrial.machine.upgrades;

import net.minecraft.world.item.Item;

/**
 * Machine upgrade items.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class MachineUpgrade extends Item {

    private final UpgradeType type;

    public MachineUpgrade(Properties properties, UpgradeType type) {
        super(properties);
        this.type = type;
    }

    public UpgradeType getType() {
        return type;
    }

    /**
     * Types of machine upgrades.
     */
    public enum UpgradeType {
        /**
         * Speed upgrade - increases processing speed by 50% per level.
         */
        SPEED_UPGRADE("speed", 1.5f, 0),

        /**
         * Energy efficiency upgrade - reduces energy consumption by 20% per level.
         */
        ENERGY_UPGRADE("energy", 0, -0.2f),

        /**
         * Output multiplier upgrade - increases output by 100% per level.
         */
        OUTPUT_UPGRADE("output", 0, 0);

        private final String id;
        private final float speedModifier;
        private final float energyModifier;

        UpgradeType(String id, float speedModifier, float energyModifier) {
            this.id = id;
            this.speedModifier = speedModifier;
            this.energyModifier = energyModifier;
        }

        public String getId() {
            return id;
        }

        public float getSpeedModifier() {
            return speedModifier;
        }

        public float getEnergyModifier() {
            return energyModifier;
        }
    }
}

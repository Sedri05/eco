package com.willfp.eco.core.config.base;

import com.willfp.eco.core.EcoPlugin;
import com.willfp.eco.core.config.BaseConfig;
import com.willfp.eco.core.config.ConfigType;
import org.jetbrains.annotations.NotNull;

/**
 * Default plugin config.yml.
 */
public class ConfigYml extends BaseConfig {
    /**
     * Config.yml.
     *
     * @param plugin The plugin.
     */
    public ConfigYml(@NotNull final EcoPlugin plugin) {
        super("config", plugin, true, ConfigType.YAML);
    }

    /**
     * Config.yml.
     *
     * @param plugin       The plugin.
     * @param removeUnused Remove unused.
     */
    public ConfigYml(@NotNull final EcoPlugin plugin,
                     final boolean removeUnused) {
        super("config", plugin, removeUnused, ConfigType.YAML);
    }

    /**
     * Config.yml.
     *
     * @param plugin The plugin.
     * @param name   The config name.
     * @deprecated Rename config.yml by using a custom config, e.g. {@link BaseConfig}.
     */
    @Deprecated
    public ConfigYml(@NotNull final EcoPlugin plugin,
                     @NotNull final String name) {
        super(name, plugin, true, ConfigType.YAML);
    }

    /**
     * Config.yml.
     *
     * @param plugin       The plugin.
     * @param name         The config name.
     * @param removeUnused Remove unused.
     * @deprecated Rename config.yml by using a custom config, e.g. {@link BaseConfig}.
     */
    @Deprecated
    public ConfigYml(@NotNull final EcoPlugin plugin,
                     @NotNull final String name,
                     final boolean removeUnused) {
        super(name, plugin, removeUnused, ConfigType.YAML);
    }
}

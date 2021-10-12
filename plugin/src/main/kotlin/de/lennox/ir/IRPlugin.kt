package de.lennox.ir

import de.lennox.ir.command.CommandRegistry
import de.lennox.ir.command.RecorderCommand
import de.lennox.ir.config.Config
import de.lennox.ir.config.DatabaseType
import de.lennox.ir.event.EventRegistry
import de.lennox.ir.intave.IntaveAccessListener
import de.lennox.ir.intave.IntaveViolationCache
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

lateinit var plugin: IRPlugin;

class IRPlugin : JavaPlugin() {
    lateinit var driver: Driver

    override fun onEnable() {
        println("Enabling Intave Recorder")
        plugin = this

        val config = Config(File(dataFolder, "database.json")).config
        // Initialize the driver
        val type = DatabaseType.byConfigValue(config.databaseType)
        if (type == null) {
            logger.severe("Invalid database type parameter ${config.databaseType}")
            logger.severe("Available options are: ${DatabaseType.values().contentToString()}")
            pluginLoader.disablePlugin(this)
            return
        }
        driver = type.driverFunction(config)

        // Register all necessary objects
        registerEvents()
        registerCommands()
    }

    private fun registerEvents() {
        // Register all events in the Bukkit API
        EventRegistry.registerEventsIn(
            IntaveViolationCache(),
            IntaveAccessListener()
        )
    }

    private fun registerCommands() {
        // Register all commands in the Bukkit API
        CommandRegistry.registerCommands(
            RecorderCommand()
        )
    }
}
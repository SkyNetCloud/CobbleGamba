package ca.skynetcloud.cobblegamba.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import ca.skynetcloud.cobblegamba.config.ConfigHandler
import ca.skynetcloud.cobblegamba.config.ConfigHandler.configPath
import ca.skynetcloud.cobblegamba.config.ConfigHandler.gson
import ca.skynetcloud.cobblegamba.screens.MainMenu
import ca.skynetcloud.cobblegamba.util.PM
import net.minecraft.client.gui.screen.ingame.CrafterScreen
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object CobbleGambaCommand {

    fun registerGambaCommand(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal("gamba")
                .executes(CobbleGambaCommand::openCobbleStatsMenu)
//                .then(literal("reload")
//                    .requires { it.hasPermissionLevel(2) }
//                    .executes(CobbleGambaCommand::reloadConfig))
        )
    }

    private fun reloadConfig(ctx: CommandContext<ServerCommandSource>): Int {
        val player = ctx.source.player
        val configString = configPath.readText()
        ConfigHandler.config = gson.fromJson(configString, ConfigHandler.PrizePlacements::class.java)
        configPath.writeText(gson.toJson(ConfigHandler.config))
        player?.sendMessage(PM.returnStyledText("<green>Successfully reloaded config!"))
        return Command.SINGLE_SUCCESS
    }


    private fun openCobbleStatsMenu(ctx: CommandContext<ServerCommandSource>): Int {

        try {
            val player = ctx.source.player

            if (player != null) {
                println("Opening MainMenu for player: ${player.displayName}")
                val screenHandler = MainMenu()
                player.openHandledScreen(screenHandler)
            } else {
                println("Player is null.")
                ctx.source.sendError(PM.returnStyledText("<red>Error: Player not found"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
            ctx.source.sendError(Text.literal("<red>An unexpected error occurred: ${e.message}"))
        }

        return 1
    }
}

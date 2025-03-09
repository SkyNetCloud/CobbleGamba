package ca.skynetcloud.cobblegamba

import ca.skynetcloud.cobblegamba.commands.CobbleGambaCommand
import ca.skynetcloud.cobblegamba.config.ConfigHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.logging.LogManager

class Cobblegamba : ModInitializer {

    var MOD_ID = "cobblegamba"


    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            CobbleGambaCommand.registerGambaCommand(dispatcher)
        }
    }

    companion object {
        val config: ConfigHandler = ConfigHandler
        val MOD_ID = "cobblegamba"
        val logManager: Logger? = LoggerFactory.getLogger(Cobblegamba.Companion.MOD_ID)
    }
}

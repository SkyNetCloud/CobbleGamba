package ca.skynetcloud.cobblegamba.screens

import ca.skynetcloud.cobblegamba.Cobblegamba.Companion.config
import ca.skynetcloud.cobblegamba.config.ConfigHandler
import ca.skynetcloud.cobblegamba.config.ConfigHandler.gson
import ca.skynetcloud.cobblegamba.config.ConfigHandler.guiSettings
import ca.skynetcloud.cobblegamba.util.CustomItemStack
import ca.skynetcloud.cobblegamba.util.PM
import ca.skynetcloud.cobblegamba.util.PM.setLore
import com.cobblemon.mod.common.CobblemonItems
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import java.lang.reflect.Method

class MainMenu: NamedScreenHandlerFactory {
    private val inventory: SimpleInventory

    init {
        inventory = SimpleInventory(size())

        populateInventory()
    }

    private fun populateInventory() {


        val pokemonCaught = CustomItemStack.create(CobblemonItems.POKE_BALL.defaultStack,"<aqua>Pokemon Gamba")


        setLore(pokemonCaught, listOf(PM.returnStyledText("<white>Pokemon Gamba:")))

        for (i in 0 until inventory.size()) {
            inventory.setStack(i, CustomItemStack.create(Items.GRAY_STAINED_GLASS_PANE.defaultStack, " "))
        }

        if (config.config.categoryOptions.pokemonMenu.second) {
            inventory.setStack(config.config.categoryOptions.pokemonMenu.first, pokemonCaught)
        }

        inventory.setStack(31, CustomItemStack.create(Items.BARRIER.defaultStack , "<red>Close"))
    }

    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler {
        val handler = object : GenericContainerScreenHandler(
            ScreenHandlerType.GENERIC_9X4, syncId, inv, inventory, rows()
        ) {
            override fun onSlotClick(
                slotNumber: Int, button: Int, action: SlotActionType, playerEntity: PlayerEntity
            ) {
                val closeHandledScreenMethod: Method = PlayerEntity::class.java.getDeclaredMethod("closeHandledScreen")
                if (!inventory.getStack(slotNumber).isOf(Items.GRAY_STAINED_GLASS_PANE)) {
                    when (slotNumber) {
                        config.config.categoryOptions.pokemonMenu.first -> cobbleGambaScreen(player, "Gamba")
                        31 -> closeHandledScreenMethod.invoke(playerEntity)
                        else -> {
                            return
                        }
                    }
                }
            }
        }
        return handler
    }

    override fun getDisplayName(): Text {
        val currentData = guiSettings.readText()
        val readData = gson.fromJson(currentData, ConfigHandler.secConfig::class.java)

        return PM.returnStyledText(readData.menuSettings.mainMenu)
    }

    fun size(): Int {
        return rows() * 9
    }

    fun rows(): Int {
        return 4
    }


}

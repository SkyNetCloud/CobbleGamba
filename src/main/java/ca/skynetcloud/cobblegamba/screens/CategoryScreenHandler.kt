package ca.skynetcloud.cobblegamba.screens

import ca.skynetcloud.cobblegamba.Cobblegamba
import ca.skynetcloud.cobblegamba.config.ConfigHandler
import ca.skynetcloud.cobblegamba.config.ConfigHandler.gson
import ca.skynetcloud.cobblegamba.util.CustomItemStack
import ca.skynetcloud.cobblegamba.util.PM
import ca.skynetcloud.cobblegamba.util.PokemonUtility
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.item.components.PokemonItemComponent
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.server.network.ServerPlayerEntity

@Suppress("NAME_SHADOWING")
class CategoryScreenHandler(
    syncId: Int,
    private val player: PlayerEntity,
    categoryName: String
) : GenericContainerScreenHandler(ScreenHandlerType.GENERIC_9X6, syncId, player.inventory, SimpleInventory(9 * 6), 6) {

    private val selectedPokemons: MutableList<Pokemon> = mutableListOf()
    private val playerCooldowns = mutableMapOf<PlayerEntity, Long>()


    init {
        populateInventory(categoryName)
    }

    private fun populateInventory(categoryName: String) {
        // Clear inventory initially
        for (i in 0 until inventory.size()) {
            inventory.setStack(i, CustomItemStack.create(Items.GRAY_STAINED_GLASS_PANE.defaultStack, " "))
        }

        // Set bottom rows to black stained glass
        for (i in 45 until 54) {
            inventory.setStack(i, CustomItemStack.create(Items.BLACK_STAINED_GLASS_PANE.defaultStack, " "))
        }

        // Set the return button
        inventory.setStack(49, CustomItemStack.create(Items.BARRIER.defaultStack, "<red>Return"))


        when (categoryName) {
            "Gamba" -> {
                PokemonUtility.displayPokemonItemStack(player, inventory as SimpleInventory)
            }
            else -> {
                    return
                }
        }
    }



    private fun handlePokemonSelection(itemStack: ItemStack, guiSlot: Int) {
        if (itemStack.item == CobblemonItems.POKEMON_MODEL) {
            if (player is ServerPlayerEntity) {
                val partyStorageSlot = Cobblemon.storage.getParty(player)

                if (partyStorageSlot.size() < 6) {
                    player.sendMessage(PM.returnStyledText("<red>Your party is empty!"))
                    return
                }

                if (guiSlot < 20 || guiSlot > 25) {
                    player.sendMessage(PM.returnStyledText("<red>Invalid Pokémon slot"))
                    return
                }

                val partySlot = guiSlot - 20

                if (partySlot < 0 || partySlot >= 6) {
                    player.sendMessage(PM.returnStyledText("<red>Invalid Pokémon slot"))
                    return
                }

                val selectedPokemon = partyStorageSlot.get(partySlot)

                if (selectedPokemon == null) {
                    player.sendMessage(PM.returnStyledText("<red>No Pokémon found in slot $partySlot"))
                    return
                }

                if (isPokemonSelected(selectedPokemon)) {
                    unselectPokemon(selectedPokemon, partySlot)
                } else {
                    selectPokemon(selectedPokemon, partySlot)
                }


                updateConfirmButton()
            }
        }
    }

    private fun isPokemonSelected(pokemon: Pokemon): Boolean {
        return selectedPokemons.any { it.uuid == pokemon.uuid }
    }

    private fun updateConfirmButton() {
        val confirmButtonSlot = 53
        if (selectedPokemons.size >= 3) {
            inventory.setStack(confirmButtonSlot, CustomItemStack.create(Items.GREEN_STAINED_GLASS_PANE.defaultStack, "<green>Confirm"))
        } else {
            inventory.setStack(confirmButtonSlot, CustomItemStack.create(Items.RED_STAINED_GLASS_PANE.defaultStack, "<red>Confirm (3+ required)"))
        }
    }

    private fun executeRandomTrade() {
        if (selectedPokemons.size >= 3) {
            if (isCooldownActive(player)) {
                player.sendMessage(PM.returnStyledText("<red>You must wait before making another trade."))
                return
            }

            if (player is ServerPlayerEntity) {
                val party: PlayerPartyStore = Cobblemon.storage.getParty(player)

                val pokemonToRemove = mutableListOf<Pokemon>()

                for (selected in selectedPokemons) {
                    val match = party.find { it == selected }
                    if (match != null) {
                        pokemonToRemove.add(match)
                    }
                }

                if (pokemonToRemove.isEmpty()) {
                    player.sendMessage(PM.returnStyledText("<red>No valid Pokémon found to trade."))
                    return
                }

                for (pokemon in pokemonToRemove) {
                    party.remove(pokemon)
                    player.sendMessage(PM.returnStyledText("<red>You traded: ${pokemon.species.name}"))
                }

                // Give the player a random Pokémon
                val randomPokemon = PokemonUtility.getRandomPokemon()
                Cobblemon.storage.getParty(player).add(randomPokemon)
                player.sendMessage(PM.returnStyledText("<green>You received: ${randomPokemon.species.name} from the trade!"))

                selectedPokemons.clear()

                playerCooldowns[player] = System.currentTimeMillis()
            }
        } else {
            player.sendMessage(PM.returnStyledText("<red>You must select at least 3 Pokémon to trade."))
        }
    }

    private fun isCooldownActive(player: PlayerEntity): Boolean {
        val currentTime = System.currentTimeMillis()
        val cooldownTime = getCooldownTime()

        return playerCooldowns[player]?.let {
            currentTime - it < cooldownTime
        } ?: false
    }

    private fun getCooldownTime(): Long {
        val currentData = ConfigHandler.guiSettings.readText()
        val readData = gson.fromJson(currentData, ConfigHandler.basicConfig::class.java)
        return readData.basicConfig.coolDown * 1000L
    }

    override fun onClosed(player: PlayerEntity) {
        super.onClosed(player)
        selectedPokemons.clear()
    }

    override fun onSlotClick(slotIndex: Int, button: Int, actionType: SlotActionType?, player: PlayerEntity?) {
        when (slotIndex) {
            49 -> {
                player?.openHandledScreen(MainMenu())
            }
            53 -> {
                if (selectedPokemons.size >= 3) {
                    executeRandomTrade()
                }
            }
            52 -> {
                selectedPokemons.clear()
                player?.sendMessage(PM.returnStyledText("<red>You have cancelled the selection."))

            }
            else -> {
                val itemStack = inventory.getStack(slotIndex)
                if (player != null) {
                    onClosed(player)
                }

                handlePokemonSelection(itemStack, slotIndex)
            }
        }
    }

    private fun getPartySlot(pokemon: Pokemon): Int? {
        val partyStorageSlot = Cobblemon.storage.getParty(player as ServerPlayerEntity)
        return partyStorageSlot.indexOf(pokemon).takeIf { it >= 0 }
    }

    fun selectPokemon(pokemon: Pokemon, partySlot: Int) {
        selectedPokemons.add(pokemon)
        player.sendMessage(PM.returnStyledText("<aqua>You selected: ${pokemon.species.name} from slot $partySlot"))
    }

    private fun unselectPokemon(pokemon: Pokemon, partySlot: Int) {
        selectedPokemons.removeAt(partySlot)
        player.sendMessage(PM.returnStyledText("<red>You unselected: ${pokemon.species.name} from slot $partySlot"))
    }

}

fun cobbleGambaScreen(player: PlayerEntity, category: String) {

    val currentData = ConfigHandler.guiSettings.readText()
    val readData = gson.fromJson(currentData, ConfigHandler.secConfig::class.java)


    var categoryName = ""

    when(category) {
        "Gamba" -> {
            categoryName = readData.menuSettings.gambaMenu
        }
        else -> { categoryName = "Default" }
    }

    player.openHandledScreen(
        SimpleNamedScreenHandlerFactory(
            { syncId, _, player ->
                CategoryScreenHandler(
                    syncId,
                    player,
                    category
                )
            },
            PM.returnStyledText(categoryName)
        )
    )

}


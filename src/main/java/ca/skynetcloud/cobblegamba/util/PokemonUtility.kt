package ca.skynetcloud.cobblegamba.util;

import ca.skynetcloud.cobblegamba.Cobblegamba
import ca.skynetcloud.cobblegamba.Cobblegamba.Companion.config
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonItemComponents
import com.cobblemon.mod.common.CobblemonItems
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.client.gui.trade.PartySlot
import com.cobblemon.mod.common.item.components.PokemonItemComponent
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Pokemon.Companion.loadFromJSON
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack

import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.*
import java.util.concurrent.ExecutionException
import java.util.function.Consumer
import kotlin.random.Random


object PokemonUtility {
    @Throws(ExecutionException::class, InterruptedException::class)
    fun getRandomPokemon(): Pokemon {
        val pokemon = Pokemon()
        pokemon.createPokemonProperties(PokemonPropertyExtractor.ALL)



        val generatedPokemonNames: MutableSet<String> = HashSet()
        val arraypokemons: MutableList<Pokemon> = ArrayList()


        arraypokemons.forEach(Consumer { pokemon1: Pokemon -> generatedPokemonNames.add(pokemon1.species.showdownId()) })

        val ran = Random

        generatedPokemonNames.add(pokemon.species.showdownId())
        pokemon.level =
            ran.nextInt(config.basicConfig.basicConfig.maxlv - config.basicConfig.basicConfig.minlv) + config.basicConfig.basicConfig.minlv
        return pokemon
    }

    fun displayPokemonItemStack(player: PlayerEntity, inventory: SimpleInventory) {
        if (player is ServerPlayerEntity) {
            val party: PlayerPartyStore = Cobblemon.storage.getParty(player)

            var slotIndex = 20
            for (pokemon in party) {
                val poke = pokemon.species.name
                val itemStack = ItemStack(CobblemonItems.POKEMON_MODEL)

                itemStack.apply {
                    set(DataComponentTypes.CUSTOM_NAME, PM.returnStyledText("<aqua>$poke"))
                    set(CobblemonItemComponents.POKEMON_ITEM, PokemonItemComponent(pokemon.species.resourceIdentifier, pokemon.aspects))
                }

                if (slotIndex < inventory.size()) {
                    inventory.setStack(slotIndex, itemStack)
                    slotIndex++
                }
            }
        }
    }

}
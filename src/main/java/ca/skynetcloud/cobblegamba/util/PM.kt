package ca.skynetcloud.cobblegamba.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.item.ItemStack
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.text.Text
import net.minecraft.text.Text.Serialization.fromJson

object PM {

    private var wrapperLookup: WrapperLookup? = null

    private fun getWrapperLookup(): WrapperLookup? {
        return wrapperLookup ?: BuiltinRegistries.createWrapperLookup().also { wrapperLookup = it }
    }

    private fun parseMessageWithStyles(text: String, placeholder: String): Component {
        var mm = MiniMessage.miniMessage();
        return mm.deserialize(text.replace("{placeholder}", placeholder)).decoration(TextDecoration.ITALIC, false)
    }

    fun returnStyledText(text: String): Text {

        val component = parseMessageWithStyles(text, "placeholder")
        val gson = GsonComponentSerializer.gson()
        val json = gson.serialize(component)
        return fromJson(json, getWrapperLookup()) as Text
    }

    fun setLore(itemStack: ItemStack, lore: List<Text>) {
        val loreComponent = LoreComponent(lore)
        itemStack.set(DataComponentTypes.LORE, loreComponent)
    }

}
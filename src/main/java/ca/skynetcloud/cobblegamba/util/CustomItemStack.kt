package ca.skynetcloud.cobblegamba.util

import net.minecraft.component.DataComponentTypes.CUSTOM_NAME
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

object CustomItemStack {

    fun create(itemStack: ItemStack, name: String): ItemStack {
        return itemStack.apply {
            this.set(CUSTOM_NAME, PM.returnStyledText(name))
        }
    }
}
package ca.skynetcloud.cobblegamba.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

object ConfigHandler {

    val configPath = File("config/CobbleGamba/config.json")
    val guiSettings = File("config/CobbleGamba/menu/gui_settings.json")

    val gson: Gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    var secConfig = Config()

    var config = PrizePlacements()

    var basicConfig = BasicConfig()

    init {
        if (configPath.exists()) {
            val configString = configPath.readText()
            config = gson.fromJson(configString, PrizePlacements::class.java)
        } else {
            createFileIfNotExists(guiSettings, gson.toJson(secConfig))
            createFileIfNotExists(configPath, gson.toJson(basicConfig))
        }
    }

    data class PrizePlacements(
        val categoryOptions: CategoryOptions = CategoryOptions(),

    )

    data class BasicConfig (
        val basicConfig: ConfigSetting = ConfigSetting()
    )

    data class Config (
        val menuSettings: ConfigurableGUISettings = ConfigurableGUISettings(),

    )

    data class ConfigurableGUISettings(
        val mainMenu: String = "<gold>CobbleGamba",
        val gambaMenu: String = "<gold>Gamba"
    )

    data class ConfigSetting(
        val coolDown: Long = 10000L,
        val minlv: Int = 5,
        val maxlv: Int = 36
    )

    data class CategoryOptions(
        val pokemonMenu: Pair<Int, Boolean> = Pair(13, true),
    )

    private fun createFileIfNotExists(file: File, content: String? = null) {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
            content?.let { file.writeText(it) }
        }
    }

}
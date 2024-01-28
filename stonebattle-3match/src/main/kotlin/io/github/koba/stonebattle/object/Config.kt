package io.github.koba.stonebattle.`object`

import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException

object Config {

    fun Player.getDeathTime(path: String): Double {
        val file = File("plugins/Stone-Battle/${uniqueId}.yml")

        val directory = File("plugins/Stone-Battle")

        if (!directory.exists()) directory.mkdir()

        if (!file.exists()) try {
            file.createNewFile()
        } catch (e: IOException) {
            Bukkit.broadcastMessage(e.toString())
        }

        val config = YamlConfiguration.loadConfiguration(file)
        return config.getDouble(path)
    }

    fun Player.setConfig(path: String, key: Any) {
        val file = File("plugins/Stone-Battle/${uniqueId}.yml")

        val directory = File("plugins/Stone-Battle")

        if (!directory.exists()) directory.mkdir()

        if (!file.exists()) try {
            file.createNewFile()
        } catch (e: IOException) {
            Bukkit.broadcastMessage(e.toString())
        }

        val config = YamlConfiguration.loadConfiguration(file)

        config.set(path, key)
        try {
            config.save(file)
        } catch (e: IOException) {
            Bukkit.broadcastMessage(e.toString())
        }
    }
}
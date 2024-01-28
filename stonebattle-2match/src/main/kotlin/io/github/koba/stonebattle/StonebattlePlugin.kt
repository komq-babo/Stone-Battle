package io.github.koba.stonebattle

import io.github.koba.stonebattle.KommandStone.team1bar
import io.github.koba.stonebattle.KommandStone.team2bar
import io.github.koba.stonebattle.`object`.Config.getDeathTime
import io.github.koba.stonebattle.`object`.Config.setConfig
import io.github.monun.kommand.kommand
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class StonebattlePlugin : JavaPlugin() {

    companion object {
        lateinit var instance: StonebattlePlugin
    }

    init {
        instance = this
    }

    override fun onEnable() {
        server.pluginManager.registerEvents(Events(), this)
        server.scheduler.runTaskTimer(this, Death(), 0L, 2L)

        saveConfig()
        val configFile = File(dataFolder, "config.yml")

        if (configFile.length() == 0L) {
            config.options().copyDefaults(true)
            saveConfig()
        }

        setupCommands()
    }

    override fun onDisable() {
        for (player in Bukkit.getOnlinePlayers()) {
            team1bar?.removePlayer(player)
            team2bar?.removePlayer(player)
        }
    }

    private fun setupCommands() = kommand {
        KommandStone.register(this@StonebattlePlugin, this)
    }
}

class Death : Runnable {
    private fun getInstance(): Plugin {
        return StonebattlePlugin.instance
    }

    override fun run() {
        for (player in Bukkit.getOnlinePlayers()) {
            if (-0.1 != player.getDeathTime("DeathTime")) {
                if (0.0 < player.getDeathTime("DeathTime")) {
                    player.sendActionBar("리스폰까지 ${(kotlin.math.floor((player.getDeathTime("DeathTime") * 10))) / 10}초")
                    player.setConfig("DeathTime", player.getDeathTime("DeathTime") - 0.1)
                } else {
                    for (team in Bukkit.getScoreboardManager().mainScoreboard.teams) {
                        if (team.hasEntry(player.name)) {
                            player.teleport(getInstance().config.getLocation("${team.name}.spawn")!!)
                        }
                    }

                    player.setConfig("DeathTime", -0.1)


                    player.gameMode = GameMode.SURVIVAL
                    player.sendActionBar("부활!")
                }
            }
        }
    }
}

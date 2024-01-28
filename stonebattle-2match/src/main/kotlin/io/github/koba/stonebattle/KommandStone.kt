package io.github.koba.stonebattle

import io.github.koba.stonebattle.Events.Companion.teamName
import io.github.monun.kommand.PluginKommand
import io.github.monun.kommand.getValue
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameRule
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Team

object KommandStone {

    private fun getInstance(): Plugin {
        return StonebattlePlugin.instance
    }

    var game: Boolean = false

    var team1bar: BossBar? = null
    var team2bar: BossBar? = null

    var team1: String? = null
    var team2: String? = null

    private lateinit var plugin: StonebattlePlugin

    private var i = 1

    internal fun register(plugin: StonebattlePlugin, kommand: PluginKommand) {
        KommandStone.plugin = plugin

        kommand.register("sb") {
            requires { isPlayer && isOp }
            then("start") {
                executes {
                    if (!game) {
                        i = 1

                        for (team in Bukkit.getScoreboardManager().mainScoreboard.teams) {
                            if (i == 1) {
                                team1bar = Bukkit.createBossBar(team.name, BarColor.WHITE, BarStyle.SOLID)
                                team1bar?.progress = 1.0
                                team1 = team.name
                            }
                            if (i == 2) {
                                team2bar = Bukkit.createBossBar(team.name, BarColor.WHITE, BarStyle.SOLID)
                                team2bar?.progress = 1.0
                                team2 = team.name
                                break
                            }
                            i += 1
                        }

                        for (world in Bukkit.getWorlds()) {
                            world.setGameRule(GameRule.KEEP_INVENTORY, true)
                            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
                        }

                        for (player in Bukkit.getOnlinePlayers()) {

                            for (team in Bukkit.getScoreboardManager().mainScoreboard.teams) {
                                if (team.hasEntry(player.name)) {
                                    teamName = team.name
                                    player.teleport(getInstance().config.getLocation("$teamName.spawn")!!)

                                    if (team1 == team.name) {
                                        team1bar?.addPlayer(player)
                                    } else if (team2 == team.name) {
                                        team2bar?.addPlayer(player)
                                    }
                                }
                            }

                            player.sendMessage("게임시작")
                        }

                        game = true
                    }
                }
            }
            then("stop") {
                executes {
                    if (game) {
                        for (player in Bukkit.getOnlinePlayers()) {
                            team1bar?.removeAll()
                            team2bar?.removeAll()
                        }
                        game = false
                    } else {
                        player.sendMessage("${ChatColor.RED}게임이 진행중이지 않습니다.")
                    }
                }
            }
            then("team") {
                then("team" to team()) {
                    then("core") {
                        executes {
                            val team: Team by it
                            getInstance().config.set("${team.name}.core", player.getTargetBlock(null, 4).location)
                            getInstance().saveConfig()

                            player.sendMessage("core ${team.name}=${player.getTargetBlock(null, 4)}")
                        }
                    }
                    then("spawn") {
                        executes {
                            val team: Team by it
                            getInstance().config.set("${team.name}.spawn", player.location)
                            getInstance().saveConfig()

                            sender.sendMessage("spawn ${team.name}=${player.location}")
                        }
                    }
                }
            }
        }
    }
}
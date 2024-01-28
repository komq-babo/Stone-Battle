package io.github.koba.stonebattle

import io.github.koba.stonebattle.KommandStone.game
import io.github.koba.stonebattle.KommandStone.team1
import io.github.koba.stonebattle.KommandStone.team1bar
import io.github.koba.stonebattle.KommandStone.team2
import io.github.koba.stonebattle.KommandStone.team2bar
import io.github.koba.stonebattle.`object`.Config.setConfig
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.BlockIterator
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random.Default.nextInt

class Events : Listener {

    private fun getInstance(): Plugin {
        return StonebattlePlugin.instance
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action = event.action

        if (player.itemInHand.type != Material.COBBLESTONE) return
        if (player.getCooldown(Material.COBBLESTONE) != 0) return
        if (player.gameMode == GameMode.SPECTATOR) return
        if (action == Action.LEFT_CLICK_AIR) {
            val stone = player.world.spawn(player.eyeLocation, Snowball::class.java)

            player.playSound(player.eyeLocation, Sound.ENTITY_SNOWBALL_THROW, 0.5f, 0.5f)

            stone.shooter = player
            stone.item = ItemStack(Material.COBBLESTONE)
            stone.velocity = player.eyeLocation.direction.multiply(1)

            player.setCooldown(Material.COBBLESTONE, 2)

            if (player.gameMode == GameMode.CREATIVE) return

            player.itemInHand.amount -= 1

            if (player.itemInHand.amount == 0) {
                for (i in 0..35) {
                    if (player.inventory.getItem(i) != null && player.inventory.getItem(i)!!.type == Material.COBBLESTONE) {
                        player.inventory.setItemInMainHand(player.inventory.getItem(i))
                        player.inventory.setItem(i, ItemStack(Material.AIR))
                        break
                    }
                }
            }
        }

        if (action == Action.RIGHT_CLICK_AIR) {

            val item = event.item ?: return

            val loc = player.location.apply { y -= 0.001; pitch = 0F }
            if (loc.block.type.isAir) return
            val iterator = BlockIterator(loc, 0.0, 8)

            while (iterator.hasNext()) {
                val block = iterator.next()

                if (block.type.isAir) {
                    block.type = Material.COBBLESTONE
                    loc.world.playSound(
                        block.location.add(0.5, 0.5, 0.5),
                        Sound.BLOCK_STONE_PLACE,
                        1.0F, 1.0F
                    )
                    if (player.gameMode != GameMode.CREATIVE) item.amount--
                    break
                }
            }
        }
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        if (event.entity !is Snowball) return
        if ((event.entity as Snowball).item.type != Material.COBBLESTONE) return
        if (event.entity.shooter !is Player) return
        if (event.hitEntity != null) {
            if (event.hitEntity !is LivingEntity) return

            val entity: LivingEntity = event.hitEntity as LivingEntity

            entity.noDamageTicks = 0
            entity.damage(1.0, event.entity.shooter as Player)

            event.entity.world.spawnParticle(
                Particle.BLOCK_CRACK,
                event.entity.location,
                20,
                0.0,
                0.0,
                0.0,
                0.0,
                Material.COBBLESTONE.createBlockData(),
                true
            )

            event.entity.world.playSound(event.entity.location, Sound.BLOCK_STONE_BREAK, 1f, 1f)
        }
        if (event.hitBlock is Block) {
            val block: Block = event.hitBlock!!

            event.entity.world.spawnParticle(
                Particle.BLOCK_CRACK,
                event.entity.location,
                20,
                0.0,
                0.0,
                0.0,
                0.0,
                Material.COBBLESTONE.createBlockData(),
                true
            )
            event.entity.world.playSound(event.entity.location, Sound.BLOCK_STONE_BREAK, 1f, 1f)

            if (block.type == Material.COBBLESTONE) {
                val random = (Math.random() * 100 + 1).toInt()
                if (random < 20) {
                    block.breakNaturally(ItemStack(Material.NETHERITE_PICKAXE))
                }
            } else if (block.type == Material.OBSIDIAN) {
                event.entity.world.spawnParticle(
                    Particle.FLAME,
                    event.entity.location,
                    10,
                    0.0,
                    0.0,
                    0.0,
                    0.1,
                    null,
                    true
                )
                val random = (Math.random() * 64 + 1)
                if (random.toInt() == 1) {
                    event.hitBlock!!.type = Material.LAVA
                }
            } else if (block.type == Material.CHEST) {
                val random = (Math.random() * 256 + 1)
                if (random.toInt() == 1) {

                    event.entity.world.spawnParticle(
                        Particle.BLOCK_CRACK,
                        event.entity.location,
                        10,
                        0.0,
                        0.0,
                        0.0,
                        0.0,
                        Material.CHEST.createBlockData(),
                        true
                    )

                    block.breakNaturally(ItemStack(Material.NETHERITE_AXE))
                }
            } else if (block.location == getInstance().config.getLocation("${team1}.core")) {
                if (!game) return
                val shooter = event.entity.shooter as Player

                if (Bukkit.getScoreboardManager().mainScoreboard.getTeam(team1.toString())!!
                        .hasEntry(shooter.name)
                ) return

                if (team1bar!!.progress <= 0.005) {
                    game = false
                    for (players in Bukkit.getOnlinePlayers()) {
                        players.sendTitle("게임종료!", team2, 10, 100, 10)
                        players.gameMode = GameMode.SPECTATOR
                        players.inventory.clear()

                        Bukkit.getScheduler().cancelTasks(getInstance())

                        team1bar?.removePlayer(players)
                        team2bar?.removePlayer(players)
                    }
                    return
                }
                team1bar!!.progress -= 0.005
                val afterProgress = team1bar!!.progress
                shooter.playSound(shooter.location, Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1f)
                team1bar?.addPlayer(shooter)

                Bukkit.getScheduler().scheduleSyncDelayedTask(getInstance(), {
                    if (team1bar!!.progress == afterProgress) {
                        team1bar?.removePlayer(shooter)
                    }
                }, 100)

                for (player in Bukkit.getOnlinePlayers()) {
                    if (Bukkit.getScoreboardManager().mainScoreboard.getTeam(team1.toString())!!
                            .hasEntry(player.name)
                    ) {
                        player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}코어가 공격받고있습니다!")
                    }
                }

            } else if (block.location == getInstance().config.getLocation("${team2}.core")) {
                if (!game) return
                val shooter = event.entity.shooter as Player
                if (Bukkit.getScoreboardManager().mainScoreboard.getTeam(team2.toString())!!
                        .hasEntry(shooter.name)
                ) return

                if (team2bar!!.progress <= 0.005) {
                    game = false
                    for (players in Bukkit.getOnlinePlayers()) {
                        players.sendTitle("게임종료!", team1, 10, 100, 10)
                        players.gameMode = GameMode.SPECTATOR
                        players.inventory.clear()

                        Bukkit.getScheduler().cancelTasks(getInstance())

                        team1bar?.removePlayer(players)
                        team2bar?.removePlayer(players)
                    }
                    return
                }
                team2bar!!.progress -= 0.005
                val afterProgress = team2bar!!.progress
                shooter.playSound(shooter.location, Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1f)
                team2bar?.addPlayer(shooter)

                Bukkit.getScheduler().scheduleSyncDelayedTask(getInstance(), {
                    if (team2bar!!.progress == afterProgress) {
                        team2bar?.removePlayer(shooter)
                    }
                }, 100)

                for (player in Bukkit.getOnlinePlayers()) {
                    if (Bukkit.getScoreboardManager().mainScoreboard.getTeam(team2.toString())!!
                            .hasEntry(player.name)
                    ) {
                        player.sendActionBar("${ChatColor.RED}${ChatColor.BOLD}코어가 공격받고있습니다!")
                    }
                }
            } else {
                val random = (Math.random() * 100 + 1).toInt()
                if (random < 20) {
                    block.breakNaturally(ItemStack(Material.NETHERITE_PICKAXE))
                }
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val block = event.block

        for (team in Bukkit.getScoreboardManager().mainScoreboard.teams) {
            if (block.location == getInstance().config.getLocation("${team.name}.core")) {
                if (event.player.gameMode != GameMode.CREATIVE) {
                    event.isCancelled = true
                }
            }
        }

        if (player.inventory.itemInHand.type != Material.STONE_PICKAXE) return

        if (!event.block.getSideBlocksType().contains(Material.LAVA)) return
        if (!event.block.getSideBlocksType().contains(Material.WATER)) return

        event.isDropItems = false

        val loc = block.location.add(0.5, 0.8, 0.5)
        val count = max(1, sqrt(nextInt(64).toDouble()).toInt())

        for (i in 0..<count) {
            val item = ItemStack(Material.COBBLESTONE)

            loc.world.dropItem(loc, item).apply {
                pickupDelay -= i * 2
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onMergeItem(event: ItemMergeEvent) {
        if (event.entity.itemStack.type == Material.COBBLESTONE) event.isCancelled = true
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val location = player.location

        if (location.y < -10) {
            player.damage(100.0)
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.block.location.y < 70) return
        event.isCancelled = true
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        if (game) {
            val player = event.player


            if (event.player.gameMode != GameMode.CREATIVE) {
                for (i in 0..35) {
                    if (player.inventory.getItem(i) != null) {
                        val item = player.inventory.getItem(i)!!
                        if (item.type == Material.COBBLESTONE) {
                            item.amount /= 2
                            player.inventory.setItem(i, item)
                        }
                    }
                }
            }

            player.gameMode = GameMode.SPECTATOR

            player.setConfig("DeathTime", 10.0)

            for (team in Bukkit.getScoreboardManager().mainScoreboard.teams) {
                if (team.hasEntry(player.name)) {
                    teamName = team.name
                    player.teleport(getInstance().config.getLocation("$teamName.spawn")!!)
                }
            }
        }
    }

    companion object {
        var teamName: String? = null
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.setConfig("DeathTime", -0.1)
    }
}

private fun Block.getSideBlocksType(): MutableList<Material> {
    val block = this
    val location = block.location
    val blocks = mutableListOf<Material>()

    blocks.add(location.clone().add(1.0, 0.0, 0.0).block.type)
    blocks.add(location.clone().add(-1.0, 0.0, 0.0).block.type)
    blocks.add(location.clone().add(0.0, 0.0, 1.0).block.type)
    blocks.add(location.clone().add(0.0, 0.0, -1.0).block.type)
    return blocks
}
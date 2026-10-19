/*
 * This file is part of Kalimba, a fork of LiquidBounce (https://github.com/CCBlueX/LiquidBounce).
 *
 * Licensed under the GNU General Public License v3 or later.
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.ModuleCategories
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.regular
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.entity.BarrelBlockEntity
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.block.entity.DispenserBlockEntity
import net.minecraft.world.level.block.entity.EnderChestBlockEntity
import net.minecraft.world.level.block.entity.HopperBlockEntity
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity
import net.minecraft.world.level.block.entity.SkullBlockEntity
import net.minecraft.world.level.block.entity.SpawnerBlockEntity
import java.io.File
import java.time.Instant

/**
 * BaseFinder
 *
 * Passive base/stash detector for anarchy servers. Every chunk the server sends is scanned for
 * player-placed block entities; a chunk is reported once its total reaches the configured
 * thresholds. Natural structures (dungeon spawners, mineshaft chests) are filtered by
 * [requireStorage] and [ignoreSingleSpawner].
 */
object ModuleBaseFinder : ClientModule("BaseFinder", ModuleCategories.WORLD) {

    private val chests by int("Chests", 4, 0..64)
    private val shulkers by int("Shulkers", 1, 0..64)
    private val barrels by int("Barrels", 4, 0..64)
    private val hoppers by int("Hoppers", 4, 0..64)
    private val dispensers by int("Dispensers", 4, 0..64)
    private val enderChests by int("EnderChests", 1, 0..64)
    private val skulls by int("Skulls", 3, 0..64)
    private val spawners by int("Spawners", 0, 0..64)

    /** A chunk with no chests/barrels/shulkers at all is more likely a natural structure. */
    private val requireStorage by boolean("ChestRequired", true)
    private val ignoreSingleSpawner by boolean("IgnoreDungeons", true)

    private val notify by boolean("Notification", true)
    private val censorCoords by boolean("CensorCoords", false)
    private val logToFile by boolean("Logger", true)

    private val pending = ArrayDeque<ChunkPos>()
    private val reported = LongOpenHashSet()

    override fun onDisabled() {
        pending.clear()
    }

    @Suppress("unused")
    private val worldChangeHandler = handler<WorldChangeEvent> {
        pending.clear()
        reported.clear()
    }

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet
        if (packet is ClientboundLevelChunkWithLightPacket) {
            pending.addLast(ChunkPos(packet.x, packet.z))
        }
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        // The chunk is only attached to the level after the packet is processed, so scan a tick later.
        var budget = 4
        while (budget-- > 0 && pending.isNotEmpty()) {
            val pos = pending.removeFirst()
            if (reported.contains(ChunkPos.pack(pos.x, pos.z))) continue
            scan(pos)
        }
    }

    private fun scan(pos: ChunkPos) {
        val chunk = world.chunkSource.getChunkNow(pos.x, pos.z) ?: return

        var nChests = 0; var nShulkers = 0; var nBarrels = 0; var nHoppers = 0; var nDispensers = 0
        var nEnder = 0; var nSkulls = 0; var nSpawners = 0

        for (be in chunk.blockEntities.values) {
            when (be) {
                is ChestBlockEntity -> nChests++
                is ShulkerBoxBlockEntity -> nShulkers++
                is BarrelBlockEntity -> nBarrels++
                is HopperBlockEntity -> nHoppers++
                is DispenserBlockEntity -> nDispensers++
                is EnderChestBlockEntity -> nEnder++
                is SkullBlockEntity -> nSkulls++
                is SpawnerBlockEntity -> nSpawners++
            }
        }

        val storage = nChests + nShulkers + nBarrels
        if (requireStorage && storage == 0) return
        if (ignoreSingleSpawner && nSpawners == 1 && storage <= 2 && nShulkers == 0) return

        val hit = (chests > 0 && nChests >= chests) ||
            (shulkers > 0 && nShulkers >= shulkers) ||
            (barrels > 0 && nBarrels >= barrels) ||
            (hoppers > 0 && nHoppers >= hoppers) ||
            (dispensers > 0 && nDispensers >= dispensers) ||
            (enderChests > 0 && nEnder >= enderChests) ||
                        (skulls > 0 && nSkulls >= skulls) ||
            (spawners > 0 && nSpawners >= spawners)
        if (!hit) return

        reported.add(ChunkPos.pack(pos.x, pos.z))

        val coords = if (censorCoords) "[hidden]" else "${pos.middleBlockX}, ${pos.middleBlockZ}"
        val summary = "chests=$nChests shulkers=$nShulkers barrels=$nBarrels hoppers=$nHoppers " +
            "dispensers=$nDispensers ender=$nEnder skulls=$nSkulls spawners=$nSpawners"

        if (notify) chat(regular("BaseFinder: possible base at $coords ($summary)"))
        if (logToFile) log("${Instant.now()} ${pos.middleBlockX} ${pos.middleBlockZ} $summary")
    }

    private fun log(line: String) = runCatching {
        File(mc.gameDirectory, "kalimba/basefinder.log").apply { parentFile.mkdirs() }.appendText(line + "\n")
    }
}

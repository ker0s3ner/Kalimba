/*
 * This file is part of Kalimba, a fork of LiquidBounce (https://github.com/CCBlueX/LiquidBounce).
 *
 * Licensed under the GNU General Public License v3 or later.
 */
package net.ccbluex.liquidbounce.features.kalimba

import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.events.ChatReceiveEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.TransferOrigin
import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.misc.antibot.ModuleAntiBot
import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType

/**
 * Tracks what the current server lets us do, so combat and mining modules can stay quiet where
 * the server would reject them anyway (spawn protection, no-PvP zones, claims, NPC "players").
 */
abstract class ServerRestriction(private val retryMs: Long, private val expiryMs: Long) {
    @Volatile
    private var deniedAt = 0L
    @Volatile
    private var lastProbe = 0L

    val denied: Boolean get() = deniedAt != 0L && System.currentTimeMillis() - deniedAt < expiryMs

    fun deny() {
        deniedAt = System.currentTimeMillis()
    }

    fun allow() {
        deniedAt = 0L
    }

    /**
     * True when the action is permitted. While denied, one probe attempt is let through every
     * [retryMs] so the client notices when the server starts allowing it again.
     */
    fun permits(): Boolean {
        if (!denied) return true
        val now = System.currentTimeMillis()
        if (now - lastProbe < retryMs) return false
        lastProbe = now
        return true
    }
}

object PvpGuard : EventListener {

    private val zone = object : ServerRestriction(retryMs = 5_000, expiryMs = 30_000) {}

    private val denyPatterns = listOf(
        Regex("pvp (is )?(currently )?(disabled|off|not allowed)", RegexOption.IGNORE_CASE),
        Regex("(can't|cannot|can not|not allowed to|unable to) (attack|hit|hurt|damage|fight)", RegexOption.IGNORE_CASE),
        Regex("(safe ?zone|spawn protection|protected area|no[- ]?pvp)", RegexOption.IGNORE_CASE),
    )

    @Suppress("unused")
    private val chatHandler = handler<ChatReceiveEvent> { event ->
        if (denyPatterns.any { it.containsMatchIn(event.message) }) zone.deny()
    }

    @Suppress("unused")
    private val worldHandler = handler<WorldChangeEvent> { zone.allow() }

    /** True when attacking [target] is pointless: bot/NPC, protected player, or we are in a no-PvP zone. */
    @JvmStatic
    fun shouldIgnore(target: Entity): Boolean {
        if (target !is Player) return false

        val self = mc.player ?: return false
        if (self.isSpectator || mc.gameMode?.playerMode == GameType.ADVENTURE) return true
        if (target.isInvulnerable || target.abilities.invulnerable) return true
        if (!zone.permits()) return true

        // Real players always have a tab-list entry; NPCs and many bots do not.
        if (mc.connection?.getPlayerInfo(target.uuid) == null) return true
        if (ModuleAntiBot.running && ModuleAntiBot.isBot(target)) return true

        return false
    }
}

object MiningGuard : EventListener {

    private val area = object : ServerRestriction(retryMs = 5_000, expiryMs = 60_000) {}

    private val recent = HashMap<BlockPos, Long>()
    private var strikes = 0

    private val denyPatterns = listOf(
        Regex("(can't|cannot|can not|not allowed to|don't have permission to|unable to) (break|mine|destroy|build|place)", RegexOption.IGNORE_CASE),
        Regex("(protected|claimed|spawn protection|no permission)", RegexOption.IGNORE_CASE),
    )

    /** Mining modules call this before breaking; false means skip until the server allows it again. */
    @JvmStatic
    fun permits(): Boolean {
        val mode = mc.gameMode?.playerMode
        if (mode == GameType.ADVENTURE || mode == GameType.SPECTATOR) return false
        return area.permits()
    }

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        val packet = event.packet
        if (event.origin == TransferOrigin.OUTGOING && packet is ServerboundPlayerActionPacket &&
            packet.action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            recent[packet.pos.immutable()] = System.currentTimeMillis()
            if (recent.size > 64) recent.entries.removeIf { System.currentTimeMillis() - it.value > 2_000 }
        } else if (event.origin == TransferOrigin.INCOMING && packet is ClientboundBlockUpdatePacket) {
            val at = recent.remove(packet.pos) ?: return@handler
            if (System.currentTimeMillis() - at > 2_000) return@handler

            if (packet.blockState.isAir) {
                strikes = 0
                area.allow()
            } else if (++strikes >= 3) {
                // The server put the block back three times in a row: breaking is not allowed here.
                area.deny()
            }
        }
    }

    @Suppress("unused")
    private val chatHandler = handler<ChatReceiveEvent> { event ->
        if (denyPatterns.any { it.containsMatchIn(event.message) }) area.deny()
    }

    @Suppress("unused")
    private val worldHandler = handler<WorldChangeEvent> {
        recent.clear()
        strikes = 0
        area.allow()
    }
}

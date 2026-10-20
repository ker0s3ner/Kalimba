# Kalimba

A Minecraft utility client built for anarchy servers, with base hunting as its focus.
Kalimba is a fork of [LiquidBounce](https://github.com/CCBlueX/LiquidBounce) (Fabric, Kotlin) and keeps its
GPL-3.0 license. Older server versions (1.8 and up) are reachable through the bundled ViaFabricPlus.

## Modules

Modules are grouped into sections in the ClickGUI:

| Section  | What is in it |
|----------|---------------|
| PvP      | KillAura, Aimbot, AutoClicker, Criticals, Velocity, Backtrack, Hitbox, KeepSprint and other melee/bow combat modules |
| CPvP     | CrystalAura, CrystalView, HoleFiller, HoleESP, Surround, BlockIn, BlockTrap, AutoTrap, BedDefender |
| Combat   | AutoArmor, AutoLeave, AutoDodge, AutoPearl and other combat helpers |
| World    | **Base hunting:** BaseFinder, NewChunks, StorageESP, XRay, BlockESP, Breadcrumbs, LogoffSpot, StrongholdFinder, VoidESP |
| Player   | Scaffold, Nuker, PacketMine, AutoTool, AutoFarm, ChestStealer and other building/mining/inventory modules |
| Movement | Flight, Speed, ElytraFly, NoFall and friends |
| Render   | ESP, Nametags, FullBright, Zoom, HUD and other visuals |
| Misc / Exploit | Everything else |

The Fun category (Derp, Twerk, Notebot, ...) has been removed.

### Base hunting

- **BaseFinder** scans every chunk the server sends and logs chunks whose storage, spawner, hopper or skull
  counts pass your thresholds. It can require chests so natural dungeons are skipped, censor coordinates in chat,
  and writes hits to `kalimba/basefinder.log`.
- **NewChunks** highlights freshly generated chunks (fluid flow updates), which is how trails and old
  player-made terrain are told apart.
- **StorageESP**, **XRay**, **Breadcrumbs** and **LogoffSpot** cover the rest of the search loop.

### Server-aware behaviour

- **PvP modules ignore what the server will not let you hit.** Targets that are NPCs/bots (no tab-list entry or
  flagged by AntiBot), invulnerable players, or anyone while you are in a no-PvP zone, adventure mode or spectator
  are skipped. No-PvP zones are detected from server messages and re-checked every few seconds.
- **Mining modules pause where the server refuses block breaking** (Nuker, PacketMine, Fucker). If the server puts a
  broken block back repeatedly, or says you are not allowed to break here, mining stops and only a single test
  break is attempted every few seconds until the server lets you through again.

## Building

Requires JDK 25.

```bash
./gradlew build
```

The jar is written to `build/libs/`. GitHub Actions builds every push and pull request to `main` and uploads the jar
as a build artifact; published releases get the jar attached.

## Theme

The ClickGUI uses a warm wood palette (walnut surfaces, amber accents). Colours live in
[`src-theme/src/colors.scss`](src-theme/src/colors.scss).

## License

Kalimba is licensed under the [GNU General Public License v3.0](LICENSE). It is based on LiquidBounce by CCBlueX;
see [NOTICE](NOTICE).

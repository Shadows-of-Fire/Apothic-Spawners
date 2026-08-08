# Description
Spawner Stats are the adjustable properties of a mob spawner. They are targeted by [Stat Modifiers](./StatModifier.md), and their current values are stored on the spawner block entity.  

Spawner stats live in a built-in registry, which means new stats can only be added by mods, not by datapacks.

# Schema
In JSON, a spawner stat is referenced by its registry name.

```js
"string" // [Mandatory] || The registry name of the spawner stat.
```

The list of built-in stats is as follows:

## Integer stats
Accept integer values in the range [-32768, 32767].

1. `apothic_spawners:min_delay` - The minimum delay between spawn attempts, in ticks.
2. `apothic_spawners:max_delay` - The maximum delay between spawn attempts, in ticks.
3. `apothic_spawners:spawn_count` - The number of spawn attempts performed per activation.
4. `apothic_spawners:max_nearby_entities` - The maximum number of nearby entities before spawning is halted.
5. `apothic_spawners:req_player_range` - The distance (in blocks) a player must be within for the spawner to activate.
6. `apothic_spawners:spawn_range` - The horizontal radius (in blocks) in which mobs may be placed.

## Float stats
Accept float values in the range [-1, 1].

7. `apothic_spawners:initial_health` - The fraction of maximum health that spawned mobs start with. Default value = 1.

## Boolean stats
Accept `true` or `false`. All default to `false`.

8. `apothic_spawners:ignore_players` - The spawner spawns even when no player is in range.
9. `apothic_spawners:ignore_conditions` - Spawned mobs bypass their natural spawn conditions.
10. `apothic_spawners:redstone_control` - The spawner only operates while receiving redstone power.
11. `apothic_spawners:ignore_light` - Spawned mobs bypass light level requirements.
12. `apothic_spawners:no_ai` - Spawned mobs have no AI.
13. `apothic_spawners:silent` - Spawned mobs are silent.
14. `apothic_spawners:youthful` - Spawned mobs are babies, when possible.
15. `apothic_spawners:burning` - Spawned mobs are on fire.

## Level stats
Accept unbounded integer values.

16. `apothic_spawners:echoing` - Spawned mobs drop additional loot and experience. Each level grants one additional roll of the mob's loot table, and increases dropped experience by 100%. Default value = 0.

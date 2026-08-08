# Description
A Stat Modifier is a single stat change within a [Spawner Modifier](./SpawnerModifier.md).  

The type of the `"value"`, `"min"`, and `"max"` fields depends on the targeted stat - see [SpawnerStats](./SpawnerStats.md) for the list of stats and their value types.

# Dependencies
This object references the following objects:
1. [SpawnerStats](./SpawnerStats.md)

# Schema
```js
{
    "type": "string",  // [Mandatory] || The registry name of the spawner stat to modify.
    "value": value,    // [Mandatory] || The change to apply. The accepted type depends on the stat.
    "min": value,      // [Optional]  || A lower clamp on the resulting stat value. Only honored in "add" mode.
    "max": value,      // [Optional]  || An upper clamp on the resulting stat value. Only honored in "add" mode.
    "mode": "string"   // [Optional]  || The application mode. Either "add" (adds the value, clamped by min/max) or "set" (overwrites the stat unconditionally). Default value = "add".
}
```

Note: Boolean stats always assign the value directly, ignoring `"min"` and `"max"`. Thus, `"add"` and `"set"` behave identically.

# Examples
A stat change reducing the spawned mobs' initial health by 5%, to a floor of 20%.

```json
{
    "type": "apothic_spawners:initial_health",
    "value": -0.05,
    "min": 0.20
}
```

A stat change making the spawner silent.

```json
{
    "type": "apothic_spawners:silent",
    "value": true
}
```

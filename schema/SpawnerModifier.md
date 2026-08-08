# Description
A Spawner Modifier is a recipe that is applied by right-clicking a mob spawner with the matching items, adjusting the spawner's stats.  

Spawner Modifiers are normal recipes with the type `apothic_spawners:spawner_modifier`, loaded from the `data/<namespace>/recipe/` datapack folder.  

When a spawner is right-clicked, all spawner modifier recipes are checked against the held items, and the first match is applied. Recipes with an offhand ingredient are always checked before recipes without one, so a two-handed recipe takes priority over a mainhand-only recipe using the same item.

# Dependencies
This object references the following objects:
1. [StatModifier](./StatModifier.md)

# Schema
```js
{
    "type": "apothic_spawners:spawner_modifier",
    "mainhand": Ingredient,        // [Mandatory] || A vanilla ingredient matching the main hand item. Must not be empty. The matched item is always consumed.
    "offhand": Ingredient,         // [Optional]  || A vanilla ingredient matching the off hand item. When omitted, the off hand is not checked. Default value = empty.
    "consumes_offhand": boolean,   // [Optional]  || If the off hand item is consumed when the modifier is applied. Default value = false.
    "stat_changes": [              // [Mandatory] || The list of stat changes applied to the spawner.
        StatModifier
    ]
}
```

# Examples
A modifier which increases the spawn count by 2 (up to a maximum of 16) when a Fermented Spider Eye is used on a spawner.

```json
{
    "type": "apothic_spawners:spawner_modifier",
    "mainhand": {
        "item": "minecraft:fermented_spider_eye"
    },
    "stat_changes": [
        {
            "type": "apothic_spawners:spawn_count",
            "value": 2,
            "max": 16
        }
    ]
}
```

Apotheosis's Ascent spawner rune, which overwrites several stats at once using `"mode": "set"`, and only loads when Apothic Spawners is installed.

```json
{
    "neoforge:conditions": [
        {
            "type": "neoforge:mod_loaded",
            "modid": "apothic_spawners"
        }
    ],
    "type": "apothic_spawners:spawner_modifier",
    "mainhand": {
        "item": "apotheosis:ascent_spawner_upgrade_rune"
    },
    "stat_changes": [
        {
            "type": "apothic_spawners:min_delay",
            "mode": "set",
            "value": 100
        },
        {
            "type": "apothic_spawners:max_delay",
            "mode": "set",
            "value": 400
        },
        {
            "type": "apothic_spawners:spawn_count",
            "mode": "set",
            "value": 10
        },
        {
            "type": "apothic_spawners:max_nearby_entities",
            "mode": "set",
            "value": 18
        },
        {
            "type": "apothic_spawners:req_player_range",
            "mode": "set",
            "value": 32
        }
    ]
}
```

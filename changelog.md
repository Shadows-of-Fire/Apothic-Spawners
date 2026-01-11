## 1.3.3
* PrincessStellar: Added Brazilian translation.
* Fixed an issue where hostile mobs would linger in peaceful for a short time.

## 1.3.2
* Wajt: Added Ukrainian translation.
* Updated to Placebo 9.9.0

## 1.3.1
* Bagel: Fixed an issue where the "Ignores Light" modifier didn't work in dimensions with ambient light.
* Fixed an issue where spawners containing a mob with a non-default `MobCategory` would cause the name to not display.

## 1.3.0
* Added a way to track if spawners have been modified by a player.
  * Breaking a mob spawner with silk touch also counts as a modification.
* Reduced the default max level of Echoing to 3 (was 5).
* Made Echoing apply to mob experience as well as drops.
* Moved the spawner blacklist from the config file to the entity type tag `apothic_spawners:blacklisted_from_spawners`.
  * Modpacks relying on the config option will need to migrate to adding blacklisted entities to the tag.
* ZHAY10086: Added Chinese Translation.

## 1.2.1
* RuyaSavascisi: Added Turkish translation.
* Fixed the "Captured!" advancement being granted when picking up any item.
* Fixed spawner modifiers which *do* consume the offhand item showing that it was not consumed.
* Added a `mode` field to `StatModifier`. The field has two options: `add` (the default) and `set`.
  * Using `"mode": "set"` allows creating stat modifiers that apply specific presets, instead of adjusting stats relative to the current value.
* When using the "Spawners Drop Empty" mode, broken spawners will now have their spawn delay reset.
  * This should reduce the chance that two empty spawners are unstackable.

## 1.2.0
* Fixed all advancments not loading.
  * These files were left in the old file path `/advancements` (plural) after the 1.20 migration, which caused them to not load.
* Added a config option to make spawners lose their stored entity when broken with silk touch.
  * All stats are retained, and the entity is lost each time the spawner is broken.
* Directly integrated the functionality of the Mob Despawn Timers mod.
  * This is most relevant here, since people tend to run into this bug when working with spawners.
* A stock datapack artifact will now be published to CurseForge with each release of the mod.

## 1.1.1
* Fixed a potential CME when calling `StatModifier.modifierCodec`.

## 1.1.0
* Updated to Minecraft 1.21.1.
* Fixed an issue where rendering spawner stat tooltips would crash.
* The Banned Mobs config is now treated as a list of regex patterns.
* Quark: Added Russian translation.

## 1.0.2
* Updated to Placebo 9.3.5.

## 1.0.1
* Fixed Capturing not being available in the Enchanting Table.
* TheWinABagel: Added EMI compat.

## 1.0.0
* Modid changed to `apothic_spawners` from `apotheosis`.
* Spawner Stats are now namespaced.
* Added the Initial Health stat.
* Added the Burning stat.
* Added the Echoing stat.
* Renamed the Baby stat to Youthful.
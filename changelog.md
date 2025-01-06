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
# jeb_ Wool

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/Minecraft-26.3-62B47A)
![Loader](https://img.shields.io/badge/Loader-Fabric-DBD0B4)
![Java](https://img.shields.io/badge/Java-25-orange)

A small Fabric mod for Minecraft. Kill or shear a sheep named **`jeb_`** and it drops a special wool that **smoothly cycles through all 16 wool colors**, just like the sheep itself. Craft carpets, slabs, stairs, a bed and a cushion from it, and they all shimmer the same way.

## Features

| Item | How to get it |
|---|---|
| **jeb_ Wool** | Kill or shear an adult, unsheared sheep named `jeb_` (drops 1вЂ“3 blocks) |
| **jeb_ Carpet** | 2 jeb_ Wool in a row в†’ 3 carpets |
| **jeb_ Wool Slab** | 3 jeb_ Wool in a row в†’ 6 slabs |
| **jeb_ Wool Stairs** | 6 jeb_ Wool in a stair shape в†’ 4 stairs |
| **jeb_ Bed** | 3 jeb_ Wool on top, 3 planks of any type below. Works like a normal bed |
| **jeb_ Cushion** | 3 jeb_ Wool Slabs in a row. Place it on top of a block and sit on it (the cushion entity added in Minecraft 26.3) |

Everything is available in the **Colored Blocks** creative tab.

### Getting a `jeb_` sheep

- With a command: `/summon minecraft:sheep ~ ~ ~ {CustomName:"jeb_"}`
- Or rename a name tag to `jeb_` in an anvil and use it on a sheep.

The name must be exactly `jeb_` (lowercase, no spaces). The sheep must be an adult. Killing or shearing it is only rewarded while it still has its wool.

## Requirements

- Minecraft **26.3**
- [Fabric Loader](https://fabricmc.net/use/installer/)
- [Fabric API](https://modrinth.com/mod/fabric-api) for 26.3
- Java **25**

## Installation (players)

1. Install Fabric for Minecraft 26.3 and put Fabric API into your `mods` folder.
2. Put the `jebwool_prev1.0_fabric26.3.jar` into the same `mods` folder.

> Because the animated textures are generated from the textures of your own copy of Minecraft (see below), this repository does not contain any prebuilt art. A jar with textures has to be built locally by following the steps below.

## Building from source

You need **Java 25**, **Git** and **Windows with PowerShell** (the asset script uses `System.Drawing`).

```powershell
git clone https://github.com/crafter12234/jeb-wool.git
cd jeb-wool

# 1. First build: downloads Minecraft into the Gradle cache
./gradlew build

# 2. Generate textures and bed models from your own Minecraft files
powershell -ExecutionPolicy Bypass -File tools\generate-assets.ps1

# 3. Build again, now with the generated assets inside
./gradlew build
```

The finished jar is in `build/libs/` (use the one **without** `-sources` in its name). To test in a development client run `./gradlew runClient`.

### Why do I have to generate textures?

The mod does not draw its own art. It uses the original wool, bed and cushion textures of the game and shows them one after another:

- `tools/generate-assets.ps1` reads `minecraft-client.jar` from your Gradle cache,
- stacks the 16 colored textures into one long image and writes an animation file (`.mcmeta`) with interpolation, so colors blend smoothly (25 ticks per color, like the sheep),
- copies the vanilla white bed models and points them to the animated textures.

Those files are derived from Minecraft assets, so they are generated on your machine and ignored by Git (see `.gitignore`).

## How it works

- **Wool, carpet, slabs, stairs, bed:** animated block textures as described above.
- **Cushion item:** animated item texture.
- **Cushion in the world:** a subclass of the vanilla `Cushion` entity. The game cannot animate entity textures, so the color switches in steps every 25 ticks.
- **Drops:** Fabric API events handle killing and shearing sheep named `jeb_`.

## Project structure

```
src/main/java/.../JebWoolMod.java       entry point: drops, creative tab, flammability
src/main/java/.../ModBlocks.java        block registration
src/main/java/.../ModItems.java         item registration (cushion)
src/main/java/.../ModEntities.java      cushion entity registration
src/main/java/.../JebBedBlock.java      bed block
src/main/java/.../JebCushion*.java      cushion entity and item
src/client/java/.../client/             client side: cushion renderer
src/main/resources/assets/jeb_wool/     models, blockstates, translations
src/main/resources/data/                recipes, loot tables, tags
tools/generate-assets.ps1               builds animated textures and bed models
```

## Known limitations

- The cushion can only be placed on the **top face** of a block.
- Dispensers do not shear `jeb_` sheep into jeb_ Wool; only shears used by a player do.
- Killing a `jeb_` sheep drops jeb_ Wool **in addition to** the vanilla wool.
- Entity textures cannot be animated by the game, so the cushion in the world changes color in steps.

## Troubleshooting

| Problem | Solution |
|---|---|
| Blocks are purple/black | `tools\generate-assets.ps1` was not run, or it printed warnings |
| The script says a texture or model was not found | Copy the full script output into an issue; file names can differ between game versions |
| `package net.minecraft.client... does not exist` | Client-only classes must live in `src/client`, not `src/main` |
| The sheep drops nothing | The name must be exactly `jeb_`, and the sheep must be an adult that has not been sheared |
| Compile errors after a Minecraft update | Some Minecraft and Fabric API names change between versions and need updating |

## Contributing

Issues and pull requests are welcome. Please do not commit generated textures or models derived from Minecraft assets.

## License

The source code is released under the [MIT License](LICENSE). Copyright (c) 2026 crafter12234.

This project is not affiliated with or endorsed by Mojang Studios or Microsoft. *Minecraft* is a trademark of Mojang Studios.


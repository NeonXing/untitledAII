---
name: ava-documentation
description: This skill should be used when creating, updating, or reviewing documentation for the AVA Minecraft mod. Covers code comments, API docs, user guides, and developer documentation.
---

# AVA Documentation Skill

## Purpose

Create comprehensive, maintainable documentation for the AVA mod including code comments, API documentation, user guides, and developer resources.

## When to Use

Use this skill when:
- Writing code comments or JavaDoc
- Creating user guides or tutorials
- Documenting API or internal systems
- Updating README or CONTRIBUTING guides
- Creating changelog or release notes

## Code Documentation

### JavaDoc Standards

```java
/**
 * Represents the immutable configuration data for a gun.
 * 
 * <p>This class contains all static properties of a gun that do not change
 * during gameplay. Instances should be cached and reused via GunRegistry.</p>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * GunData ak47 = new GunData.Builder()
 *     .gunId("ak47")
 *     .maxAmmo(30)
 *     .fireRate(10.0f)
 *     .damage(30.0f)
 *     .build();
 * }</pre>
 *
 * @see GunState
 * @see GunRegistry
 * @author AVA Mod Team
 * @since 1.0.0
 */
public class GunData {
    /**
     * The unique identifier for this gun.
     * 
     * <p>Must match the resource location used for gun data files.</p>
     */
    private final String gunId;
    
    /**
     * Maximum ammunition capacity.
     * 
     * <p>This is the total ammo that can be loaded into the gun.</p>
     * 
     * @return the maximum ammo count
     */
    public int getMaxAmmo() {
        return maxAmmo;
    }
    
    /**
     * Fires the gun from the specified player's position.
     * 
     * <p>This method handles all the logic for firing including:</p>
     * <ul>
     *   <li>Checking ammo and cooldown</li>
     *   <li>Calculating spread</li>
     *   <li>Raycasting for hit detection</li>
     *   <li>Applying damage to hit entities</li>
     *   <li>Spawning visual effects</li>
     * </ul>
     *
     * @param player the player firing the gun
     * @param gunState the current state of the gun
     * @return true if the gun was fired successfully, false otherwise
     * @throws IllegalArgumentException if player or gunState is null
     */
    public boolean fireGun(ServerPlayer player, GunState gunState) {
        // Implementation
    }
}
```

### Comment Style Guide

```java
// GOOD: Descriptive comment for complex logic
// Apply spread based on movement speed and stance
// Sprinting increases spread 2x, crouching reduces spread 0.5x
float movementModifier = player.isSprinting() ? 2.0f : 
                         player.isCrouching() ? 0.5f : 1.0f;
float spread = baseSpread * movementModifier;

// GOOD: Explain why, not what
// Cache this calculation as it's called every tick for all players
private static final Map<String, Integer> GUN_ID_CACHE = new HashMap<>();

// BAD: Useless comment
// Set the ammo to 30
currentAmmo = 30;

// GOOD: Mark TODO/FIXME with clear expectations
// TODO: Implement different spread patterns for different guns
// Currently all guns use circular spread pattern

// FIXME: This causes a memory leak when player disconnects
// Need to clean up GunState instances properly
```

## API Documentation

### Creating API Docs

```markdown
# Gun Registry API

## Overview
The Gun Registry manages all gun data and provides access to gun configurations.

## Methods

### register
```java
public static void register(ResourceLocation id, GunData data)
```
Registers a new gun with the given ID and data.

**Parameters:**
- `id` - The unique resource location for this gun
- `data` - The gun configuration data

**Throws:**
- `IllegalArgumentException` - if id or data is null
- `IllegalStateException` - if a gun with this ID already exists

**Example:**
```java
GunRegistry.register(
    ResourceLocation.fromNamespaceAndPath("untitledaii", "ak47"),
    new GunData.Builder().gunId("ak47").maxAmmo(30).build()
);
```

### get
```java
public static Optional<GunData> get(String gunId)
```
Retrieves gun data by ID.

**Returns:**
- Optional containing the GunData, or empty if not found

**Since:** 1.0.0
```

## User Documentation

### README Structure

```markdown
# AVA Minecraft Mod

A high-performance Minecraft gun mod inspired by the game A.V.A (Alliance of Valiant Arms).

[![License](https://img.shields.io/badge/license-LGPL%20v3-blue.svg)](LICENSE)
[![Forge](https://img.shields.io/badge/Forge-1.20.1-brightgreen.svg)](https://files.minecraftforge.net/)
[![CurseForge](https://cf.way2muchnoise.eu/full_123456_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/untitledaii)

## Features

- 🎯 **Realistic Gun Mechanics** - Authentic firing, reloading, and recoil
- 🔫 **20+ Guns** - Pistols, SMGs, Rifles, Snipers, Shotguns, LMGs
- 🔧 **Attachment System** - Scopes, Suppressors, Magazines, Stocks
- 🎨 **High-Quality Models** - First-person and third-person models
- ⚡ **Optimized Performance** - Runs smoothly even on lower-end systems
- 🌐 **Multiplayer Support** - Full client-server synchronization

## Installation

### Requirements
- Minecraft 1.20.1
- Forge 47.2.0 or higher
- 4GB+ RAM recommended

### Download
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/untitledaii)
- [Modrinth](https://modrinth.com/mod/untitledaii)

### Setup
1. Install Forge 1.20.1
2. Download the mod jar file
3. Place in `.minecraft/mods` folder
4. Launch Minecraft

## Quick Start

[Installation and basic usage guide...]

## Guns List

[Table of all guns with stats...]

## Controls

[Key bindings...]

## Configuration

[Config file documentation...]

## Performance

[Optimization tips...]

## Troubleshooting

[Common issues and solutions...]

## Changelog

[Version history...]

## Contributing

[Developer guide...]

## Credits

[Acknowledgments...]

## License

LGPL v3
```

### User Guide Examples

```markdown
# How to Use Attachments

## Overview
Attachments can be attached to guns to modify their performance and behavior.

## Compatible Attachments

### Scopes
- **Red Dot Sight** - 2x zoom, minimal spread penalty
- **4x Scope** - 4x zoom, moderate spread penalty
- **8x Scope** - 8x zoom, high spread penalty

### Suppressors
- **Standard Suppressor** - Reduces sound by 70%
- **Tactical Suppressor** - Reduces sound by 90%

## How to Attach

1. **Equip** the gun you want to modify
2. **Open** the attachment menu with [V] key (default)
3. **Select** the attachment type you want to add
4. **Choose** from available attachments
5. **Confirm** to apply

## Removing Attachments

To remove an attachment:
1. Open attachment menu [V]
2. Select the attachment you want to remove
3. Click "Remove"

## Notes
- Not all guns support all attachment types
- Some attachments conflict with each other
- Attachments modify gun stats (damage, spread, etc.)
```

## Developer Documentation

### Architecture Overview

```markdown
# AVA Mod Architecture

## System Components

### Gun System
- **GunData** - Immutable gun configuration
- **GunState** - Mutable gun instance state
- **GunRegistry** - Central gun registry

### Attachment System
- **GunAttachment** - Attachment definition
- **AttachmentManager** - Manages attachment compatibility

### Networking
- **GunFirePacket** - Firing synchronization
- **GunStateSyncPacket** - State synchronization

### Rendering
- **GunRenderer** - First-person rendering
- **GunBER** - BlockEntityWithoutLevelRenderer

## Data Flow

```
User Input → Client Prediction → Server Validation → Update State → Sync to Clients
```

## Performance

- Use object pools for particles
- Cache BakedModel instances
- Implement distance culling
- Batch render operations

[More technical details...]
```

### Contributing Guide

```markdown
# Contributing to AVA Mod

Thank you for your interest in contributing!

## Getting Started

### Prerequisites
- JDK 17
- Git
- IntelliJ IDEA (recommended)

### Setup Development Environment

```bash
# Clone the repository
git clone https://github.com/NeonXing/untitledAII.git
cd untitledAII

# Build the project
./gradlew build

# Run in development mode
./gradlew runClient
```

## Coding Standards

- Follow `.codebuddy/rules/ava-code-quality`
- Use JavaDoc for public APIs
- Add unit tests for new features
- Use meaningful commit messages

## Commit Message Format

```
feat: add suppressor attachment system

- Implement GunAttachment data structure
- Add attachment compatibility checking
- Create attachment menu GUI

Closes #123
```

## Pull Request Process

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Ensure all tests pass
6. Submit PR with clear description

## Testing

```bash
# Run all tests
./gradlew test

# Run GameTest
./gradlew runGametest
```
```

## Changelog Management

### Format

```markdown
## [1.2.0] - 2025-02-20

### Added
- New suppressor attachment
- Attachment menu GUI
- Gun stat display in inventory

### Changed
- Improved recoil mechanics
- Updated gun models
- Optimized particle rendering

### Fixed
- Fixed ammo sync after reload
- Fixed scope zoom on different screen sizes
- Fixed crash when equipping attachments

### Performance
- Reduced memory allocations by 30%
- Improved rendering FPS by 15%

### Breaking
- Gun data file format changed (v1 to v2)
- Attachment IDs renamed for consistency
```

## Documentation Checklist

### Code Comments
- [ ] Public classes have JavaDoc
- [ ] Public methods have JavaDoc
- [ ] Complex logic has inline comments
- [ ] TODO/FIXME items are tracked

### User Documentation
- [ ] README is up to date
- [ ] Installation guide exists
- [ ] User guide covers all features
- [ ] Key bindings are documented

### Developer Documentation
- [ ] Architecture overview exists
- [ ] API documentation is complete
- [ ] Contributing guide exists
- [ ] Setup instructions are clear

### Release Documentation
- [ ] Changelog is maintained
- [ ] Release notes are written
- [ ] Migration guide for breaking changes

## Best Practices

1. **Document as you code** - Don't leave documentation for later
2. **Use examples** - Code examples are better than descriptions
3. **Keep it current** - Update docs when code changes
4. **Be consistent** - Use same style throughout
5. **Proofread** - Check for clarity and correctness

## References

- [Oracle JavaDoc Guide](https://www.oracle.com/java/technologies/javase/javadoc-tool.html)
- [Markdown Guide](https://www.markdownguide.org/)
- [Technical Writing Best Practices](https://developers.google.com/tech-writing/one)

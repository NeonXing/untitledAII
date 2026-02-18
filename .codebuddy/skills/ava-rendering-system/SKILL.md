---
name: ava-rendering-system
description: This skill should be used when implementing, optimizing, or refactoring rendering systems for the AVA Minecraft mod. Covers gun models, first-person rendering, particles, tracers, scope overlays, and GPU optimization techniques.
---

# AVA Rendering System Skill

## Purpose

Implement high-performance, visually impressive gun rendering systems using modern Minecraft rendering techniques, following best practices from TACZ and top rendering optimization mods.

## When to Use

Use this skill when:
- Designing gun model rendering (first-person or third-person)
- Implementing particle effects (muzzle flash, tracers, bullet hits)
- Creating scope overlay or zoom effects
- Optimizing render performance
- Working with BakedModel, IClientItemExtensions, or BlockEntityRenderer

## Rendering Architecture

### Modern Rendering Pipeline

Use **IClientItemExtensions** for custom item rendering:

```java
public class GunItem implements ItemLike, IClientItemExtensions {
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(GunRenderer::new);
    }
}

public class GunRenderer implements IClientItemExtensions {
    private final BlockEntityWithoutLevelRenderer renderer;
    
    public GunRenderer() {
        renderer = new GunBER();
    }
    
    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return renderer;
    }
}
```

### BlockEntityWithoutLevelRenderer (BER)

```java
public class GunBER extends BlockEntityWithoutLevelRenderer {
    private final ModelManager modelManager;
    private final PoseStack poseStack = new PoseStack();
    
    public GunBER(BlockEntityRenderDispatcher pDispatcher, 
                  EntityModelSet pModelSet, 
                  ItemRenderer pItemRenderer) {
        super(pDispatcher, pModelSet, pItemRenderer);
        this.modelManager = new GunModelManager();
    }
    
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transform,
                           PoseStack poseStack, MultiBufferSource buffer,
                           int light, int overlay) {
        // Render gun with attachments
        GunModel model = modelManager.getModel(stack);
        model.render(poseStack, buffer, light, overlay);
    }
}
```

## Gun Model Rendering

### First-Person Rendering

```java
public void renderFirstPerson(GunModel gun, PoseStack poseStack,
                               MultiBufferSource buffer, int light, int overlay) {
    // Apply arm animation
    float equipProgress = Minecraft.getInstance().options.useKeybind.getKey().isDown() 
        ? Mth.lerp(0.1f, lastEquipProgress, equipProgress) 
        : 0;
    
    poseStack.pushPose();
    poseStack.translate(0, -0.1f, -0.5f + equipProgress * 0.2f);
    
    // Render gun body
    renderPart(gun.body, poseStack, buffer, light, overlay);
    
    // Render equipped attachments
    for (GunAttachment attachment : gun.getAttachments()) {
        renderPart(attachment.getModel(), poseStack, buffer, light, overlay);
    }
    
    poseStack.popPose();
}
```

### Third-Person Rendering

```java
public void renderThirdPerson(GunModel gun, PoseStack poseStack,
                              MultiBufferSource buffer, int light, int overlay) {
    // Use simplified model for distant rendering
    float distanceSq = Minecraft.getInstance().player.distanceToSqr(entity);
    if (distanceSq > 64 * 64) {
        renderSimplifiedModel(gun, poseStack, buffer, light, overlay);
        return;
    }
    
    // Render full model with reduced detail
    renderPart(gun.body, poseStack, buffer, light, overlay);
}
```

### Model Caching

```java
public class GunModelManager {
    private static final Map<ResourceLocation, BakedModel> MODEL_CACHE = 
        new HashMap<>();
    
    private static final Map<String, GunModel> COMPOSED_CACHE = 
        new ConcurrentHashMap<>();
    
    public GunModel getModel(ItemStack stack) {
        String cacheKey = getCacheKey(stack);
        return COMPOSED_CACHE.computeIfAbsent(cacheKey, key -> {
            return composeModel(stack);
        });
    }
}
```

## Particle Effects

### Muzzle Flash

```java
public class MuzzleFlashParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet sprite;
    
    @Override
    public Particle createParticle(SimpleParticleType type, Level level,
                                   double x, double y, double z,
                                   double vx, double vy, double vz) {
        return new MuzzleFlashParticle(level, x, y, z, sprite);
    }
}

public class MuzzleFlashParticle extends TextureSheetParticle {
    public MuzzleFlashParticle(Level level, double x, double y, double z, 
                               SpriteSet spriteSet) {
        super(level, x, y, z);
        this.lifetime = 5;  // Short-lived
        this.quadSize = 0.3f;
        this.sprite = spriteSet.get(0, 0);
        this.alpha = 1.0f;
    }
    
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        if (age > lifetime / 2) {
            this.alpha = (lifetime - age) / (float)(lifetime / 2);
        }
        super.render(buffer, camera, partialTicks);
    }
}
```

### Distance Culling for Particles

```java
private static final float MAX_PARTICLE_DISTANCE_SQ = 48 * 48;

public void spawnMuzzleFlash(BlockPos pos, Direction facing) {
    Player player = Minecraft.getInstance().player;
    if (player.distanceToSqr(Vec3.atCenterOf(pos)) > MAX_PARTICLE_DISTANCE_SQ) {
        return;  // Don't spawn if too far
    }
    
    // Spawn particle
    level.addParticle(
        ParticleTypes.MOB_SPAWN_FLAME,
        pos.getX() + 0.5 + facing.getStepX() * 0.5,
        pos.getY() + 0.5 + facing.getStepY() * 0.5,
        pos.getZ() + 0.5 + facing.getStepZ() * 0.5,
        0, 0, 0
    );
}
```

### Bullet Tracers

```java
public class TracerParticle extends TextureSheetParticle {
    private final Vec3 start;
    private final Vec3 end;
    
    public TracerParticle(Level level, Vec3 start, Vec3 end) {
        super(level, start.x, start.y, start.z);
        this.start = start;
        this.end = end;
        this.lifetime = 10;
        this.quadSize = 0.02f;
    }
    
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        // Draw line from start to end
        Vec3[] points = new Vec3[] {start, end};
        for (Vec3 point : points) {
            renderQuad(buffer, camera, partialTicks, point);
        }
    }
}
```

### Bullet Hit Effects

```java
public void spawnBulletHit(BlockPos pos, Direction hitFace) {
    // Limit max simultaneous hit effects
    if (activeHitEffects.size() > 10) {
        return;
    }
    
    // Spawn particles
    for (int i = 0; i < 5; i++) {
        double spread = 0.1;
        double vx = (random.nextDouble() - 0.5) * spread + hitFace.getStepX() * 0.5;
        double vy = (random.nextDouble() - 0.5) * spread + hitFace.getStepY() * 0.5;
        double vz = (random.nextDouble() - 0.5) * spread + hitFace.getStepZ() * 0.5;
        
        level.addParticle(
            ParticleTypes.BLOCK,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5,
            vx, vy, vz,
            Block.id(level.getBlockState(pos))
        );
    }
}
```

## Scope Overlay

### 2D Overlay Rendering

```java
public class ScopeOverlayRenderer {
    private static ResourceLocation SCOPE_TEXTURE = 
        ResourceLocation.fromNamespaceAndPath("untitledaii", "textures/gui/scope.png");
    
    public static void renderScopeOverlay(GuiGraphics gui, float zoomLevel) {
        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // Render black vignette
        gui.fill(0, 0, screenWidth, screenHeight, 0xFF000000);
        
        // Render scope texture
        int size = (int)(screenHeight * 0.8f);
        int x = (screenWidth - size) / 2;
        int y = (screenHeight - size) / 2;
        gui.blit(SCOPE_TEXTURE, x, y, 0, 0, size, size, 256, 256);
        
        // Render crosshair
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        gui.fill(centerX - 10, centerY - 1, centerX + 10, centerY + 1, 0xFFFFFFFF);
        gui.fill(centerX - 1, centerY - 10, centerX + 1, centerY + 10, 0xFFFFFFFF);
    }
}
```

### Zoom System

```java
public class GunZoomHandler {
    private static float currentZoom = 1.0f;
    private static float targetZoom = 1.0f;
    
    public static void setZoom(float zoom) {
        targetZoom = zoom;
    }
    
    @SubscribeEvent
    public void onRenderTick(RenderTickEvent event) {
        if (event.phase == Phase.START) {
            // Smooth zoom transition
            currentZoom = Mth.lerp(0.1f, currentZoom, targetZoom);
            
            // Apply FOV change
            GameRenderer renderer = Minecraft.getInstance().gameRenderer;
            renderer.setFov(currentZoom * 70.0f);
        }
    }
}
```

## Rendering Optimization

### Batch Rendering

```java
public void renderMultiple(List<GunModel> guns, PoseStack poseStack,
                           MultiBufferSource buffer, int light, int overlay) {
    // Group by RenderType for batching
    Map<RenderType, List<GunModel>> byRenderType = guns.stream()
        .collect(Collectors.groupingBy(GunModel::getRenderType));
    
    for (Map.Entry<RenderType, List<GunModel>> entry : byRenderType.entrySet()) {
        VertexConsumer consumer = buffer.getBuffer(entry.getKey());
        for (GunModel gun : entry.getValue()) {
            gun.renderToBuffer(poseStack, consumer, light, overlay);
        }
    }
}
```

### Object Reuse

```java
public class GunRenderContext {
    // Reuse these objects across frames
    private final PoseStack poseStack = new PoseStack();
    private final List<VertexConsumer> consumers = new ArrayList<>();
    private final Vec3 tempVec = new Vec3(0, 0, 0);
    
    public void render(...) {
        // Reuse poseStack instead of creating new one
        poseStack.pushPose();
        // ... render ...
        poseStack.popPose();
    }
}
```

### Texture Optimization

- Use power-of-2 dimensions (256x256, 512x512)
- Merge similar textures into atlases
- Use texture compression where supported
- Preload textures on mod init

## Render Event Handling

```java
@SubscribeEvent
public void onRenderHand(RenderHandEvent event) {
    ItemStack stack = event.getItemStack();
    if (stack.getItem() instanceof GunItem) {
        // Cancel vanilla hand rendering
        event.setCanceled(true);
        
        // Render gun
        renderGunFirstPerson(stack, event.getPoseStack(), 
                            event.getMultiBufferSource(),
                            event.getPackedLight(), event.getPackedOverlay());
    }
}

@SubscribeEvent
public void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
    if (isZoomed()) {
        ScopeOverlayRenderer.renderScopeOverlay(event.getGuiGraphics(), 
                                                getZoomLevel());
    }
}
```

## Debug Rendering

```java
@SubscribeEvent
public void onRenderWorldLast(RenderLevelStageEvent event) {
    if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getLevelRenderer().renderBuffer.bufferSource();
        
        // Debug visualization
        renderDebugHitboxes(poseStack, buffer);
        renderDebugTracers(poseStack, buffer);
    }
}
```

## Common Pitfalls

1. **Direct OpenGL manipulation** - Use Minecraft's abstractions (GuiGraphics, VertexConsumer)
2. **Creating objects per-frame** - Reuse PoseStack, buffers, Vec3 instances
3. **Ignoring RenderType** - Group geometry by RenderType for batching
4. **No distance culling** - Skip rendering distant objects
5. **Inefficient texture loading** - Cache BakedModel instances

## Performance Checklist

- [ ] Using IClientItemExtensions for custom rendering
- [ ] BakedModel instances cached
- [ ] RenderType grouping for batching
- [ ] Distance culling implemented
- [ ] Objects reused in render loops
- [ ] Particle count limited
- [ ] Texture dimensions are power-of-2
- [ ] No direct OpenGL state manipulation

## References

- `.codebuddy/rules/ava-gun-rendering` - Rendering rules
- `.codebuddy/skills/ava-gun-architecture` - Gun system architecture
- `.codebuddy/skills/ava-performance-tuning` - Performance optimization
- TACZ rendering: https://github.com/F1zeiL/TACZ
- Embeddium: https://github.com/Embeddium/Embeddium
- Iris: https://github.com/IrisShaders/Iris

# 工业类 Minecraft 模组研究

## 优秀工业模组参考

### 1. IndustrialCraft 2 (IC2)
**特点:**
- EU (Energy Units) 能量系统
- 矿石加工链: 粉碎机 → 炼矿炉 → 提纯机 → 镁能炉
- 电池系统: 充电/放电机制
- 多种机器: 发电机、提取机、压缩机、剪贴机

**可借鉴:**
- 能量单位的标准化
- 多阶段加工流程设计
- 机器的通用化接口(可插入配方)

### 2. Thermal Expansion / Thermal Series
**特点:**
- RF (Redstone Flux) 能量系统 - 后来成为标准
- 简洁的机器设计
- 配方管理清晰
- 扩展性强

**可借鉴:**
- RF 能量系统设计
- 配方注册系统
- 机器的统一API设计

### 3. Mekanism
**特点:**
- 氢气系统: 电解水 → 氢气
- 液体管道系统
- 多倍矿石加工(1矿变多矿)
- 复杂的化学加工链

**可借鉴:**
- 气体/液体管道系统
- 多倍加工机制
- 化学反应配方设计

### 4. Create
**特点:**
- 机械动能系统(传动轴、皮带轮)
- 传送带系统
- 物理动画
- 模块化设计

**可借鉴:**
- 动能传输设计
- 传送带物品传输
- 动画系统集成

### 5. Immersive Engineering
**特点:**
- 真实感强的工业设计
- 大型结构(起重机、卷扬机)
- 导线系统
- 液压系统

**可借鉴:**
- 大型结构设计
- 导线能量传输
- 现实化工业概念

### 6. Applied Energistics 2 (AE2)
**特点:**
- ME (Matter Energy) 存储系统
- 终端界面
- 自动合成系统
- 量子隧道

**可借鉴:**
- 存储系统架构
- GUI 设计
- 自动化合成逻辑

### 7. Ender IO
**特点:**
- 立方能量/物品/流体管道
- 统一的导管系统
- 简洁的机器设计

**可借鉴:**
- 导管系统设计
- 能量/物品/流体统一管道

### 8. Botania
**特点:**
- 魔法科技融合
- 花朵作为机器
- 自动化系统
- 独特的能量系统(Mana)

**可借鉴:**
- 创新的能量概念
- 自动化逻辑

## 核心设计模式总结

### 能量系统设计

**选择 1: RF (Redstone Flux) 标准**
- 优点: 通用、兼容性强
- 缺点: 需要依赖 Forge Energy API

**选择 2: 自定义能量系统**
- 优点: 完全控制、独特性
- 缺点: 需要自己实现转换器

**推荐:** 使用 Forge Energy API (FE) 以保证兼容性

### 机器接口设计

```java
// 统一机器接口
public interface IMachine {
    /**
     * 检查机器是否可以运行
     */
    boolean canProcess();
    
    /**
     * 处理一个tick
     */
    void process();
    
    /**
     * 获取当前进度 (0.0 - 1.0)
     */
    float getProgress();
    
    /**
     * 获取能量存储
     */
    IEnergyStorage getEnergyStorage();
    
    /**
     * 获取物品处理器
     */
    IItemHandler getItemHandler();
}
```

### 配方系统设计

```java
// 配方注册系统
public class MachineRecipes {
    private static final Map<ResourceLocation, MachineRecipe> RECIPES = new HashMap<>();
    
    public static void register(ResourceLocation id, MachineRecipe recipe) {
        RECIPES.put(id, recipe);
    }
    
    public static MachineRecipe findRecipe(NonNullList<ItemStack> inputs) {
        return RECIPES.values().stream()
            .filter(r -> r.matches(inputs))
            .findFirst()
            .orElse(null);
    }
}

// 配方定义
public class MachineRecipe {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final NonNullList<ItemStack> outputs;
    private final int processTime;
    private final int energyRequired;
    
    public boolean matches(NonNullList<ItemStack> inputs) {
        // 匹配逻辑
    }
}
```

### 管道系统设计

```java
// 管道接口
public interface IPipe {
    /**
     * 传输类型
     */
    PipeType getType();  // ENERGY, ITEM, FLUID, GAS
    
    /**
     * 插入资源
     */
    int insert(PipeType type, Object resource, Direction side);
    
    /**
     * 提取资源
     */
    Object extract(PipeType type, Direction side, int amount);
}

// 导管类
public class ConduitBlock extends Block {
    // 统一能量/物品/流体传输
    private final CapabilityStorage energyStorage;
    private final CapabilityStorage itemStorage;
    private final CapabilityStorage fluidStorage;
}
```

### GUI 设计模式

```java
// 容器
public class MachineContainer extends AbstractContainerMenu {
    private final MachineBlockEntity tileEntity;
    
    public MachineContainer(int id, Inventory inv, MachineBlockEntity te) {
        super(Containers.MACHINE.get(), id);
        this.tileEntity = te;
        
        // 添加玩家背包槽位
        addSlot(new Slot(inv, ...));
        
        // 添加机器槽位
        addSlot(new MachineSlot(te.getInventory(), 0, 56, 17));
    }
}

// 屏幕渲染
public class MachineScreen extends AbstractContainerScreen<MachineContainer> {
    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, 
                          int mouseX, int mouseY) {
        renderArrow(poseStack, tileEntity.getProgress());
        renderEnergyBar(poseStack, tileEntity.getEnergyStored());
        renderSlots(poseStack);
    }
}
```

## 可借鉴的功能

### 1. 多倍矿石加工
- Macerator (粉碎机): 1矿 → 2粉
- Furnace (熔炉): 粉 → 锭
- 1矿最终可得 2-3 锭

### 2. 自动化系统
- 传送带 (Create)
- 自动合成 (AE2)
- 流水线设计

### 3. 能量传输
- 导线 (Immersive Engineering)
- 机器到机器直接传输
- 能量缓存

### 4. 存储系统
- 大容量存储
- 自动分类
- 远程访问 (AE2 量子终端)

### 5. 机器升级系统
- 速度升级
- 能量效率升级
- 输出倍率升级

## 项目结构建议

```
src/main/java/org/example/untitledaii/industrial/
├── energy/                    # 能量系统
│   ├── EnergyStorage.java
│   └── EnergyNetwork.java
├── machine/                   # 机器
│   ├── IMachine.java
│   ├── MachineBlock.java
│   ├── MachineBlockEntity.java
│   └── recipes/
│       ├── MachineRecipe.java
│       └── MachineRecipeManager.java
├── pipe/                      # 管道
│   ├── PipeBlock.java
│   ├── PipeBlockEntity.java
│   └── PipeNetwork.java
├── gui/                       # GUI
│   ├── MachineContainer.java
│   └── MachineScreen.java
└── item/                      # 工具与材料
    ├── Wrench.java
    ├── Circuit.java
    └── MachineUpgrade.java
```

## 技术选型建议

### 能量系统
使用 Forge Energy API (FE)
- 通用标准
- 与其他模组兼容

### 配方系统
使用 Forge 原生配方系统或自定义
- 支持 JSON 配方文件
- 支持数据包 (Datapack)

### 网络同步
使用 Capability + Network
- Capability: 存储机器状态
- Network: 同步到客户端

### 渲染
- BlockEntityRenderer: 机器动画
- IClientItemExtensions: 自定义物品渲染
- 粒子效果: 机器运行特效

## 性能优化建议

1. **Lazy Loading**: 按需加载方块实体
2. **Object Pooling**: 粒子和物品堆叠复用
3. **Distance Culling**: 远距离不渲染/不tick
4. **Event Throttling**: 降低网络包频率
5. **Caching**: 缓存配方查找结果

## 参考项目链接

- IndustrialCraft 2: https://github.com/minecraft-mod/industrialcraft2
- Mekanism: https://github.com/mekanism/Mekanism
- Thermal Series: https://github.com/CoFH/ThermalSeries
- Create: https://github.com/Creators-of-Create/Create
- Immersive Engineering: https://github.com/BluSunrize/ImmersiveEngineering
- Applied Energistics 2: https://github.com/AppliedEnergistics/Applied-Energistics-2
- Ender IO: https://github.com/EndlessIO/EndlessIO
- Botania: https://github.com/VazkiiMods/Botania

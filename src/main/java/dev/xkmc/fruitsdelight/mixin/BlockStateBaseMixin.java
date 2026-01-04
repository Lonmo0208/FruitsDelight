package dev.xkmc.fruitsdelight.mixin;

import dev.xkmc.fruitsdelight.init.registrate.FDEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Shadow
    public abstract boolean is(TagKey<Block> tag);

    // 缓存字符串常量，避免每次调用都创建新的字符串
    @Unique
    private static final String LEAF_PIERCING_TAG = FDEffects.LEAF_PIERCING.key().location().toString();

    @Inject(
        at = @At("HEAD"),
        cancellable = true,
        method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
    )
    public void fruitsdelight$getCollisionShape$passableLeave(
        BlockGetter level, 
        BlockPos pos, 
        CollisionContext ctx, 
        CallbackInfoReturnable<VoxelShape> cir
    ) {
        // 1. 快速失败：先检查是否是树叶，避免不必要的实体检查
        if (!this.is(BlockTags.LEAVES)) {
            return;
        }
        
        // 2. 只有实体碰撞上下文才需要进一步检查
        if (!(ctx instanceof EntityCollisionContext entityCtx)) {
            return;
        }
        
        // 3. 避免频繁的instanceof检查
        var entity = entityCtx.getEntity();
        if (entity == null) {
            return;
        }
        
        // 4. 检查是否为投掷物且具有特定标签
        if (entity instanceof Projectile projectile && 
            projectile.getTags().contains(LEAF_PIERCING_TAG)) {
            cir.setReturnValue(Shapes.empty());
        }
    }

    // 可选：添加一个安全的备选方法，避免可能的递归调用
    @Unique
    private boolean fruitsdelight$isLeafSafe() {
        try {
            return this.is(BlockTags.LEAVES);
        } catch (StackOverflowError | Error e) {
            // 如果检测到递归调用，返回false避免无限循环
            return false;
        }
    }
}

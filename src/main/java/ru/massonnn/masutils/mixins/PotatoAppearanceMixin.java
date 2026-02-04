package ru.massonnn.masutils.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.ArmorStandEntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.features.dungeons.StarMobs;
import ru.massonnn.masutils.client.hypixel.LocationUtils;
import ru.massonnn.masutils.client.utils.StarMobPersistentAccessor;
import ru.massonnn.masutils.client.utils.StarMobStateAccessor;

import static ru.massonnn.masutils.client.features.dungeons.StarMobs.isNameTag;

@Mixin(LivingEntityRenderer.class)
public abstract class PotatoAppearanceMixin {
    @Unique
    private static final ModelPart MASUTILS$CUBE_PART;
    @Unique
    private static final Identifier MASUTILS$REGULAR_POTATO = Identifier.of("masutils", "textures/potato.png");
    @Unique
    private static final Identifier MASUTILS$STAR_POTATO = Identifier.of("masutils", "textures/star_potato.png");

    static {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("cube", ModelPartBuilder.create().uv(0, 0)
                .cuboid(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F), ModelTransform.NONE);
        MASUTILS$CUBE_PART = root.createPart(16, 16);
    }

    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void captureStarStatus(net.minecraft.entity.LivingEntity entity, LivingEntityRenderState state, float f, CallbackInfo ci) {
        if (!LocationUtils.isInDungeons() || !MasUtilsConfigManager.get().qol.potatoMode) return;

        if (state instanceof StarMobStateAccessor stateAccessor && entity instanceof StarMobPersistentAccessor entityAccessor) {

            if (!entityAccessor.masutils$isPotato()) {
                boolean foundTag = false;
                boolean foundStar = false;

                if (entity.getCustomName() != null) {
                    String name = entity.getCustomName().getString();
                    if (isNameTag(name)) {
                        foundTag = true;
                        if (StarMobs.isStarMob(name)) foundStar = true;
                    }
                }

                if (!foundTag) {
                    net.minecraft.util.math.Box box = entity.getBoundingBox().expand(1.5, 5.0, 1.5);

                    var world = entity.getEntityWorld();
                    if (world != null) {
                        java.util.List<net.minecraft.entity.decoration.ArmorStandEntity> stands = world.getEntitiesByClass(
                                net.minecraft.entity.decoration.ArmorStandEntity.class,
                                box,
                                e -> e.getCustomName() != null
                        );

                        if (stands != null) {
                            for (net.minecraft.entity.decoration.ArmorStandEntity stand : stands) {
                                String sName = stand.getCustomName().getString();
                                if (isNameTag(sName)) {
                                    foundTag = true;
                                    if (StarMobs.isStarMob(sName)) foundStar = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (foundTag) {
                    entityAccessor.masutils$setPotato(true);
                    entityAccessor.masutils$setStar(foundStar);
                }
            }

            stateAccessor.setHasNameTag(entityAccessor.masutils$isPotato());
            stateAccessor.setStarMob(entityAccessor.masutils$isStar());
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderAsPotato(LivingEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, net.minecraft.client.render.state.CameraRenderState camera, CallbackInfo ci) {
        if (!LocationUtils.isInDungeons() || !MasUtilsConfigManager.get().qol.potatoMode) return;

        if (state instanceof ArmorStandEntityRenderState armorStandState) {
            if (armorStandState.displayName != null) {
                String text = armorStandState.displayName.getString();

                if (isNameTag(text)) {
                    Box searchUnder = new Box(
                            camera.pos.x + state.x - 1.0, camera.pos.y + state.y - 5.0, camera.pos.z + state.z - 1.0,
                            camera.pos.x + state.x + 1.0, camera.pos.y + state.y + 0.5, camera.pos.z + state.z + 1.0
                    );

                    var world = MinecraftClient.getInstance().world;
                    if (world != null) {
                        boolean hasMobBelow = !world.getOtherEntities(null, searchUnder,
                                e -> e instanceof net.minecraft.entity.LivingEntity &&
                                        !(e instanceof net.minecraft.entity.decoration.ArmorStandEntity)
                        ).isEmpty();

                        if (!hasMobBelow) {
                            ci.cancel();
                            return;
                        }
                    }

                    if (!StarMobs.isStarMob(text)) {
                        ci.cancel();
                    }
                }
            }
            return;
        }

        if (state instanceof StarMobStateAccessor accessor) {
            if (accessor.hasNameTag()) {
                ci.cancel();
                matrices.push();

                matrices.scale(-1.0F, -1.0F, 1.0F);
                float s = state.baseScale * 16.0f;
                matrices.scale(state.width * s, state.height * s, state.width * s);

                Identifier texture = accessor.isStarMob() ? MASUTILS$STAR_POTATO : MASUTILS$REGULAR_POTATO;

                queue.submitModelPart(
                        MASUTILS$CUBE_PART,
                        matrices,
                        RenderLayer.getEntitySolid(texture),
                        state.light,
                        LivingEntityRenderer.getOverlay(state, 0.0f),
                        (Sprite) null
                );

                matrices.pop();
            }
        }
    }
}
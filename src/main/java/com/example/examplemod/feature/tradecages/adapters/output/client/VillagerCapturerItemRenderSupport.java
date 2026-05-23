package com.example.examplemod.feature.tradecages.adapters.output.client;

import com.example.examplemod.feature.tradecages.adapters.input.VillagerCapturerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;

public final class VillagerCapturerItemRenderSupport {
    private static final VillagerCapturerRenderBackend RENDER_BACKEND = new MinecraftVillagerCapturerRenderBackend();

    private VillagerCapturerItemRenderSupport() {
    }

    public static boolean renderVillager(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
        return renderVillager(stack, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, poseStack, bufferSource, packedLight, 0, partialTick);
    }

    public static boolean renderVillager(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick) {
        Level level = Minecraft.getInstance().level;
        if (level == null || !VillagerCapturerItem.hasCapturedVillager(stack)) {
            return false;
        }

        Villager villager = VillagerCapturerItem.createCapturedVillager(level, stack, new BlockPos(0, 0, 0));
        if (villager == null) {
            return false;
        }

        poseStack.pushPose();
        try {
            applyDisplayTransform(displayContext, poseStack);
            return RENDER_BACKEND.render(villager, poseStack, bufferSource, packedLight, packedOverlay, partialTick);
        } finally {
            poseStack.popPose();
        }
    }

    private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
        switch (displayContext) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                poseStack.scale(0.38F, 0.38F, 0.38F);
                poseStack.translate(0.0D, -0.10D, 0.0D);
            }
            case GUI, FIXED, GROUND -> {
                poseStack.scale(0.35F, 0.35F, 0.35F);
                poseStack.translate(0.0D, -0.15D, 0.0D);
            }
            default -> {
                poseStack.scale(0.35F, 0.35F, 0.35F);
                poseStack.translate(0.0D, -0.15D, 0.0D);
            }
        }
    }

    public interface VillagerCapturerRenderBackend {
        boolean render(Villager villager, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick);
    }

    private static final class MinecraftVillagerCapturerRenderBackend implements VillagerCapturerRenderBackend {
        @Override
        public boolean render(Villager villager, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick) {
            try {
                Object renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(villager);
                if (renderer == null) {
                    return false;
                }

                if (invokeDirectRender(renderer, villager, poseStack, bufferSource, packedLight, partialTick)) {
                    return true;
                }

                Object renderState = createRenderState(renderer, villager, partialTick);
                if (renderState == null) {
                    return false;
                }

                return invokeRender(renderer, renderState, poseStack, bufferSource, packedLight, packedOverlay);
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        private static Object createRenderState(Object renderer, Villager villager, float partialTick) throws ReflectiveOperationException {
            Method creator = findMethod(renderer.getClass(), "createRenderState", 0);
            Object renderState = creator != null ? creator.invoke(renderer) : null;

            Method extractor = findMethod(renderer.getClass(), "extractRenderState", 0);
            if (extractor == null) {
                extractor = findMethod(renderer.getClass(), "extractRenderState", 1);
            }
            if (extractor == null) {
                extractor = findMethod(renderer.getClass(), "extractRenderState", 2);
            }
            if (extractor == null) {
                extractor = findMethod(renderer.getClass(), "extractRenderState", 3);
            }

            if (extractor != null) {
                Object[] args = buildArgs(extractor, villager, renderState, partialTick);
                Object returned = extractor.invoke(renderer, args);
                if (returned != null) {
                    renderState = returned;
                }
            }

            return renderState;
        }

        private static boolean invokeDirectRender(Object renderer, Villager villager, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) throws ReflectiveOperationException {
            Method render = null;
            for (Method method : renderer.getClass().getMethods()) {
                if (!method.getName().equals("render") || method.getParameterCount() != 6) {
                    continue;
                }
                Class<?> firstParam = method.getParameterTypes()[0];
                if (firstParam.isAssignableFrom(villager.getClass()) || firstParam.isAssignableFrom(Entity.class)) {
                    render = method;
                    break;
                }
            }

            if (render == null) {
                return false;
            }

            Object[] args = new Object[] { villager, 0.0F, partialTick, poseStack, bufferSource, packedLight };
            render.invoke(renderer, args);
            return true;
        }

        private static boolean invokeRender(Object renderer, Object renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) throws ReflectiveOperationException {
            Method render = null;
            for (Method method : renderer.getClass().getMethods()) {
                if (!method.getName().equals("render") || method.getParameterCount() != 5 && method.getParameterCount() != 4) {
                    continue;
                }
                Class<?> firstParam = method.getParameterTypes()[0];
                if (firstParam.isAssignableFrom(renderState.getClass()) || firstParam.getSimpleName().endsWith("RenderState")) {
                    render = method;
                    break;
                }
            }

            if (render == null) {
                return false;
            }

            if (render.getParameterCount() == 5) {
                render.invoke(renderer, renderState, poseStack, bufferSource, packedLight, packedOverlay);
            } else {
                render.invoke(renderer, renderState, poseStack, bufferSource, packedLight);
            }
            return true;
        }

        private static Method findMethod(Class<?> type, String name, int parameterCount) {
            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                for (Method method : current.getDeclaredMethods()) {
                    if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                        method.setAccessible(true);
                        return method;
                    }
                }
            }
            for (Method method : type.getMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                    return method;
                }
            }
            return null;
        }

        private static Object[] buildArgs(Method method, Villager villager, Object renderState, float partialTick) {
            Class<?>[] params = method.getParameterTypes();
            Object[] args = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                Class<?> param = params[i];
                if (param.isAssignableFrom(villager.getClass())) {
                    args[i] = villager;
                } else if (renderState != null && param.isAssignableFrom(renderState.getClass())) {
                    args[i] = renderState;
                } else if (param == float.class || param == Float.class) {
                    args[i] = partialTick;
                } else if (param == int.class || param == Integer.class) {
                    args[i] = 0;
                } else if (param == boolean.class || param == Boolean.class) {
                    args[i] = false;
                } else {
                    args[i] = null;
                }
            }
            return args;
        }
    }
}

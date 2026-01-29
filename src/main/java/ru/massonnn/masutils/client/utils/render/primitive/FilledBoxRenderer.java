package ru.massonnn.masutils.client.utils.render.primitive;

import org.joml.Matrix4f;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import ru.massonnn.masutils.client.utils.render.MasutilsRenderPipeline;
import ru.massonnn.masutils.client.utils.render.MatrixHelper;
import ru.massonnn.masutils.client.utils.render.Renderer;
import ru.massonnn.masutils.client.utils.render.state.FilledBoxRenderState;

public final class FilledBoxRenderer implements PrimitiveRenderer<FilledBoxRenderState> {
    static final FilledBoxRenderer INSTANCE = new FilledBoxRenderer();

    private FilledBoxRenderer() {}

    @Override
    public void submitPrimitives(FilledBoxRenderState state, CameraRenderState cameraState) {
        BufferBuilder buffer = Renderer.getBuffer(state.throughWalls ? MasutilsRenderPipeline.FILLED_THROUGH_WALLS : RenderPipelines.DEBUG_FILLED_BOX, 3f);
        Matrix4f positionMatrix = new Matrix4f()
                .translate((float) -cameraState.pos.x, (float) -cameraState.pos.y, (float) -cameraState.pos.z);
        MatrixStack matrices = MatrixHelper.toStack(positionMatrix);

        VertexRendering.drawFilledBox(matrices, buffer, state.minX, state.minY, state.minZ, state.maxX, state.maxY, state.maxZ, state.colourComponents[0], state.colourComponents[1], state.colourComponents[2], state.alpha);
    }
}
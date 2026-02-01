package ru.massonnn.masutils.client.utils.render.primitive;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.TextureSetup;
import org.joml.Matrix4f;
import ru.massonnn.masutils.client.utils.render.Renderer;
import ru.massonnn.masutils.client.utils.render.state.TextRenderState;

public final class TextPrimitiveRenderer implements PrimitiveRenderer<TextRenderState> {
    static final TextPrimitiveRenderer INSTANCE = new TextPrimitiveRenderer();

    private static final RenderPipeline SEE_THROUGH = RenderPipelines.RENDERTYPE_TEXT_SEETHROUGH;
    private static final RenderPipeline NORMAL = RenderPipelines.RENDERTYPE_TEXT;

    private static final RenderPipeline BACKGROUND = RenderPipelines.DEBUG_QUADS;

    private TextPrimitiveRenderer() {}

    @Override
    public void submitPrimitives(TextRenderState state, CameraRenderState cameraState) {
        RenderPipeline textPipeline = state.throughWalls ? SEE_THROUGH : NORMAL;

        Matrix4f positionMatrix = new Matrix4f()
                .translate((float) (state.pos.x - cameraState.pos.x),
                        (float) (state.pos.y - cameraState.pos.y),
                        (float) (state.pos.z - cameraState.pos.z))
                .rotate(cameraState.orientation)
                .scale(state.scale, -state.scale, state.scale);

        drawBackground(positionMatrix, state);

        state.glyphs.draw(new TextRenderer.GlyphDrawer() {
            @Override
            public void drawGlyph(TextDrawable glyph) {
                TextureSetup textureSetup = TextureSetup.withoutGlTexture(glyph.textureView());
                BufferBuilder textBuffer = Renderer.getBuffer(textPipeline, textureSetup);
                glyph.render(positionMatrix, textBuffer, LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE, false);
            }

            @Override
            public void drawRectangle(TextDrawable bakedGlyph) {
                drawGlyph(bakedGlyph);
            }
        });
    }

    private void drawBackground(Matrix4f matrix, TextRenderState state) {
        float width = state.glyphs.getScreenRect().width();
        float height = state.glyphs.getScreenRect().height();

        float yOffsetCorrection = - height * 0.05f;

        float paddingX = 0.4f;
        float paddingY = 0.4f;

        float left   = -paddingX;
        float right  = width + paddingX;
        float top    = -paddingY;
        float bottom = height + paddingY;

        float centerX = width / 2f;
        left -= centerX;
        right -= centerX;

        float r = 0.0f, g = 0.0f, b = 0.0f, a = 0.6f;
        float z = -0.01f;

        BufferBuilder bgBuffer = Renderer.getBuffer(BACKGROUND);

        bgBuffer.vertex(matrix, left,   top,    z).color(r, g, b, a);
        bgBuffer.vertex(matrix, right,  top,    z).color(r, g, b, a);
        bgBuffer.vertex(matrix, right,  bottom, z).color(r, g, b, a);
        bgBuffer.vertex(matrix, left,   bottom, z).color(r, g, b, a);
    }
}
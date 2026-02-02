package ru.massonnn.masutils.client.utils.render.primitive;

import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.StructureSpawns;

public interface PrimitiveCollector {

    void submitOutlinedBox(Box box, float[] colourComponents, float alpha, float lineWidth, boolean throughWalls);

    void submitText(Text text, Vec3d pos, boolean throughWalls);

    void submitText(Text text, Vec3d pos, float scale, boolean throughWalls);

    void submitText(Text text, Vec3d pos, float scale, float yOffset, boolean throughWalls);

    void submitCursorLine(Vec3d pos, float[] colourComponents, float alpha, float lineWidth);
}

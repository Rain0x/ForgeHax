package com.matt.forgehax.util;

import com.matt.forgehax.ForgeHaxBase;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

public class VectorUtils extends ForgeHaxBase {
    // thanks Gregor
    static Matrix4f viewMatrix = new Matrix4f();
    static Matrix4f projectionMatrix = new Matrix4f();

    public static Vector3f Vec3TransformCoordinate(Vector3f vec, Matrix4f matrix) {
        Vector3f vOutput = new Vector3f(0, 0, 0);

        vOutput.x = (vec.x * matrix.m00) + (vec.y * matrix.m10) + (vec.z * matrix.m20) + matrix.m30;
        vOutput.y = (vec.x * matrix.m01) + (vec.y * matrix.m11) + (vec.z * matrix.m21) + matrix.m31;
        vOutput.z = (vec.x * matrix.m02) + (vec.y * matrix.m12) + (vec.z * matrix.m22) + matrix.m32;
        float w = 1 / ((vec.x * matrix.m03) + (vec.y * matrix.m13) + (vec.z * matrix.m23) + matrix.m33);

        vOutput.x *= w;
        vOutput.y *= w;
        vOutput.z *= w;

        return vOutput;
    }

    public static ScreenPos toScreen(double x, double y, double z) {
        Entity view = MC.getRenderViewEntity();

        Vec3d viewNormal = view.getLook(MC.getRenderPartialTicks()).normalize();

        Vec3d camPos = ActiveRenderInfo.getPosition();
        Vec3d eyePos = ActiveRenderInfo.projectViewFromEntity(view, MC.getRenderPartialTicks());

        float vecX = (float) ((camPos.xCoord + eyePos.xCoord) - x);
        float vecY = (float) ((camPos.yCoord + eyePos.yCoord) - y);
        float vecZ = (float) ((camPos.zCoord + eyePos.zCoord) - z);

        Vector3f pos = new Vector3f(vecX, vecY, vecZ);

        viewMatrix.load(ActiveRenderInfo.MODELVIEW.asReadOnlyBuffer());
        projectionMatrix.load(ActiveRenderInfo.PROJECTION.asReadOnlyBuffer());

        pos = Vec3TransformCoordinate(pos, viewMatrix);
        pos = Vec3TransformCoordinate(pos, projectionMatrix);

        ScaledResolution scaledRes = new ScaledResolution(MC);
        pos.x = (float) ((scaledRes.getScaledWidth() * (pos.x + 1.0)) / 2.0);
        pos.y = (float) (scaledRes.getScaledHeight() * (1.0 - ((pos.y + 1.0) / 2.0)));

        boolean bVisible = false;

        double dot = viewNormal.xCoord * vecX + viewNormal.yCoord * vecY + viewNormal.zCoord * vecZ;

        if (dot < 0) // We only want vectors that are in front of the player
            bVisible = true;

        if(pos.x < 0 || pos.y < 0 || pos.x > scaledRes.getScaledWidth() || pos.y > scaledRes.getScaledHeight())
            bVisible = false;

        return new ScreenPos(pos.x, pos.y, bVisible);
    }

    public static Angle vectorToAngle(Vec3d vecIn) {
        double pitch = Math.toDegrees(Math.asin(-vecIn.zCoord));
        double yaw = Math.toDegrees(Math.atan2(vecIn.xCoord, vecIn.yCoord));
        return new Angle(pitch, yaw);
    }

    public static class ScreenPos {
        public final int x;
        public final int y;
        public final boolean isVisible;

        public ScreenPos(double x, double y, boolean isVisible) {
            this.x = (int)x;
            this.y = (int)y;
            this.isVisible = isVisible;
        }
    }
}
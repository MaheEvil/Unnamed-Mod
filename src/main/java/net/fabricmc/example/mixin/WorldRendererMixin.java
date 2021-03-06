package net.fabricmc.example.mixin;

//import com.mojang.blaze3d.systems.RenderSystem;
//import cutefulmod.IChatScreen;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.example.config.Configs;
import net.fabricmc.example.IChatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.CameraView;
import net.minecraft.client.util.math.Matrix4f;
//import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL41;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.opengl.GL11;

@Mixin(GameRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    private MinecraftClient client;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.BEFORE
                    //target = "Lnet/minecraft/client/render/BackgroundRenderer;method_23792()V"
            )
    )
    private void onRenderInjectBeforeRenderParticles(float tickDelta, long nanoTime, CallbackInfo ci) {
        Screen currentScreen = client.currentScreen;
        if (currentScreen instanceof ChatScreen) {
            String[] args = ((IChatScreen)currentScreen).getMessage().split(" ");
            if ((args[0].equals("/fill") || args[0].equals("/clone")) && args.length >= 7) {
                BlockPos pos1;
                BlockPos pos2;
                pos1 = getBlockPosFromStrings(args[1], args[2], args[3]);
                pos2 = getBlockPosFromStrings(args[4], args[5], args[6]);
                if (pos1 != null && pos2 != null) {
                    drawBoxWithOutline(pos1, pos2, 1, 1, 1, 0.4F, 0, 0, 0);
                    if (args[0].equals("/clone") && args.length >= 10) {
                        BlockPos pos3;
                        pos3 = getBlockPosFromStrings(args[7], args[8], args[9]);
                        if (pos3 != null) {
                            int x1 = Math.abs(pos1.getX() - pos2.getX());
                            int y1 = Math.abs(pos1.getY() - pos2.getY());
                            int z1 = Math.abs(pos1.getZ() - pos2.getZ());
                            BlockPos pos4 = new BlockPos(pos3.getX() + x1, pos3.getY() + y1, pos3.getZ() + z1);

                            drawBoxWithOutline(pos3, pos4, 0.16F, 0.5F, 0.45F, 0.4F, 0, 0, 0);
                        }
                    }
                }
            } else if ((args[0].equals("/setblock")) && args.length >= 4) {
                BlockPos pos;
                pos = getBlockPosFromStrings(args[1], args[2], args[3]);
                if (pos != null) {
                    drawBoxWithOutline(pos, pos, 1, 1, 1, 0.4F, 0, 0, 0);
                }
            }
        }
    }

    public BlockPos getBlockPosFromStrings(String x, String y, String z) {
        BlockPos pos;
        int posX;
        int posY;
        int posZ;

        assert MinecraftClient.getInstance().player != null;
        BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();

        try {
            if (x.startsWith("~")) {
                x = x.substring(1);
                int offset = 0;
                if (!x.isEmpty()) {
                    offset = Integer.parseInt(x);
                }
                posX = playerPos.getX() + offset;
            } else {
                posX = Integer.parseInt(x);
            }
            if (y.startsWith("~")) {
                y = y.substring(1);
                int offset = 0;
                if (!y.isEmpty()) {
                    offset = Integer.parseInt(y);
                }
                posY = playerPos.getY() + offset;
            } else {
                posY = Integer.parseInt(y);
            }
            if (z.startsWith("~")) {
                z = z.substring(1);
                int offset = 0;
                if (!z.isEmpty()) {
                    offset = Integer.parseInt(z);
                }
                posZ = playerPos.getZ() + offset;
            } else {
                posZ = Integer.parseInt(z);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        pos = new BlockPos(posX, posY, posZ);
        return pos;
    }

    public void drawBoxWithOutline(BlockPos pos1, BlockPos pos2, float fillred, float fillgreen, float fillblue, float fillalpha, float outlinered, float outlinegreen, float outlineblue) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        BlockPos posOrigin = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));

        //CameraEntity camera = MinecraftClient.getInstance().worldRenderer;


        Vec3d cameraPos = this.client.getCameraEntity().getPos();

        //matrices.push();
        GlStateManager.pushMatrix();
        //matrices.translate(posOrigin.getX() - cameraPos.getX(), posOrigin.getY() - cameraPos.getY(), posOrigin.getZ() - cameraPos.getZ());
        //GL11.glTranslated(posOrigin.getX() - cameraPos.x, posOrigin.getY() - cameraPos.y, posOrigin.getZ() - cameraPos.z);
        GlStateManager.translated(posOrigin.getX() - cameraPos.x,posOrigin.getY() - cameraPos.y,posOrigin.getZ() - cameraPos.z);
        //GL11.glScaled(posOrigin.getX() / cameraPos.x, posOrigin.getY() - cameraPos.y, posOrigin.getZ() - cameraPos.z);



        //Matrix4f model = GlStateManager; //.peek().getModel();


        GlStateManager.enableBlend();
        //GlStateManager.defaultBlendFunc();
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);
        //RenderSystem;
        GlStateManager.disableDepthTest();

        bufferBuilder.begin(GL11.GL_LINES, VertexFormats.POSITION_COLOR);
        drawBox(bufferBuilder, pos1, pos2, outlinered, outlinegreen, outlineblue, 1, true);
        tessellator.draw();

        bufferBuilder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        drawBox(bufferBuilder, pos1, pos2, fillred, fillgreen, fillblue, fillalpha, false);
        tessellator.draw();

        //matrices.pop();
        GlStateManager.popMatrix();

        //RenderSystem.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    public void drawBox(BufferBuilder bufferBuilder, BlockPos pos1, BlockPos pos2, float red, float green, float blue, float alpha, boolean outline) {
        float x1 = Math.abs(pos1.getX() - pos2.getX()) + 1.002F;
        float y1 = Math.abs(pos1.getY() - pos2.getY()) + 1.002F;
        float z1 = Math.abs(pos1.getZ() - pos2.getZ()) + 1.002F;

        float c0 = -0.002F;
        // is slightly outside to avoid z-fighting

        // Back Face
        bufferBuilder.vertex(c0, c0, c0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(c0, c0, z1).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(c0, y1, z1).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(c0, y1, c0).color(red, green, blue, alpha).next();
        if (outline) {
            bufferBuilder.vertex(c0, c0, c0).color(red, green, blue, alpha).next();
        }

        // Front Face
        bufferBuilder.vertex(x1, c0, c0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(x1, y1, c0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(x1, y1, z1).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(x1, c0, z1).color(red, green, blue, alpha).next();
        if(outline) {
            bufferBuilder.vertex(x1, c0, c0).color(red, green, blue, alpha).next();
        }

        // Right Face
        bufferBuilder.vertex(c0, c0, c0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(c0, y1, c0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(x1, y1, c0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(x1, c0, c0).color(red, green, blue, alpha).next();
        if (outline) {
            bufferBuilder.vertex(c0, c0, c0).color(red, green, blue, alpha).next();
        }

        // Left Face
        bufferBuilder.vertex( c0, c0, z1).color(red, green, blue, alpha).next();
        bufferBuilder.vertex( x1, c0, z1).color(red, green, blue, alpha).next();
        bufferBuilder.vertex( x1, y1, z1).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(c0, y1, z1).color(red, green, blue, alpha).next();
        if (outline) {
            bufferBuilder.vertex(c0, c0, z1).color(red, green, blue, alpha).next();
        }

        // Bottom Face
        bufferBuilder.vertex( c0, c0, c0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(x1, c0, c0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(x1, c0, z1).color(red, green, blue, alpha).next();
        bufferBuilder.vertex(c0, c0, z1).color(red, green, blue, alpha).next();
        if (outline) {
            bufferBuilder.vertex(c0, c0, c0).color(red, green, blue, alpha).next();
        }

        // Top Face
        bufferBuilder.vertex( c0, y1, c0).color(red, green, blue, alpha).next();
        bufferBuilder.vertex( c0, y1, z1).color(red, green, blue, alpha).next();
        bufferBuilder.vertex( x1, y1, z1).color(red, green, blue, alpha).next();
        bufferBuilder.vertex( x1, y1, c0).color(red, green, blue, alpha).next();
        if (outline) {
            bufferBuilder.vertex( c0, y1, c0).color(red, green, blue, alpha).next();
        }
    }
}

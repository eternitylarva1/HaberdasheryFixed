package com.esotericsoftware.spine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.esotericsoftware.spine.attachments.*;
import com.megacrit.cardcrawl.core.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BonePickerSkeletonRendererDebug
{
    private static final Color boneLineColor = Color.RED;

    private static final Color boneOriginColor = Color.GREEN;

    private static final Color attachmentLineColor = new Color(0.0F, 0.0F, 1.0F, 0.5F);

    private static final Color triangleLineColor = new Color(1.0F, 0.64F, 0.0F, 0.5F);

    private static final Color aabbColor = new Color(0.0F, 1.0F, 0.0F, 0.5F);

    private static final Color hoverColor = Color.CYAN;

    private static final float hoverScale = 2f;

    private final ShapeRenderer shapes;

    private boolean drawBones = true, drawRegionAttachments = true, drawBoundingBoxes = true;

    private boolean drawMeshHull = true;

    private boolean drawMeshTriangles = true;

    private boolean drawPaths = true;

    private final SkeletonBounds bounds = new SkeletonBounds();

    private final FloatArray temp = new FloatArray();

    private float scale = 1.0F;

    private float boneWidth = 2.0F;

    private boolean premultipliedAlpha;

    private static Bone hoveredBone = null;

    public BonePickerSkeletonRendererDebug()
    {
        this.shapes = new ShapeRenderer();
    }

    public BonePickerSkeletonRendererDebug(ShapeRenderer shapes)
    {
        this.shapes = shapes;
    }

    public void draw(Skeleton skeleton)
    {
        hoveredBone = null;
        if (drawBones) {
            Array<Bone> bones = skeleton.getBones();
            List<Bone> hovered = new ArrayList<>();
            for (int i = 0; i < bones.size; ++i) {
                Bone bone = bones.get(i);
                if (isHoveringBone(bone)) {
                    hovered.add(bone);
                }
            }
            if (hovered.size() > 0) {
                hoveredBone = hovered.get(0);
                float shortestDistance = distToCursor(hoveredBone);

                for (int i = 1; i < hovered.size(); ++i) {
                    float dist = distToCursor(hovered.get(i));
                    if (dist < shortestDistance) {
                        shortestDistance = dist;
                        hoveredBone = hovered.get(i);
                    }
                }
            }
        }

        float skeletonX = skeleton.getX();
        float skeletonY = skeleton.getY();
        Gdx.gl.glEnable(3042);
        int srcFunc = this.premultipliedAlpha ? 1 : 770;
        Gdx.gl.glBlendFunc(srcFunc, 771);
        ShapeRenderer shapes = this.shapes;
        Array<Bone> bones = skeleton.getBones();
        if (this.drawBones) {
            shapes.setColor(boneLineColor);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0, n = bones.size; i < n; i++) {
                Bone bone = (Bone) bones.get(i);
                if (bone.parent != null) {
                    float x = skeletonX + bone.data.length * bone.a + bone.worldX;
                    float y = skeletonY + bone.data.length * bone.c + bone.worldY;
                    if (bone == hoveredBone) {
                        shapes.setColor(hoverColor);
                    } else {
                        shapes.setColor(boneLineColor);
                    }
                    shapes.rectLine(skeletonX + bone.worldX, skeletonY + bone.worldY, x, y, this.boneWidth * this.scale * (bone == hoveredBone ? hoverScale : 1f));
                }
            }
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.x(skeletonX, skeletonY, 4.0F * this.scale);
        } else {
            shapes.begin(ShapeRenderer.ShapeType.Line);
        }
        if (this.drawRegionAttachments) {
            shapes.setColor(attachmentLineColor);
            Array<Slot> slots = skeleton.getSlots();
            for (int i = 0, n = slots.size; i < n; i++) {
                Slot slot = (Slot) slots.get(i);
                Attachment attachment = slot.attachment;
                if (attachment instanceof RegionAttachment) {
                    RegionAttachment regionAttachment = (RegionAttachment) attachment;
                    float[] vertices = regionAttachment.updateWorldVertices(slot, false);
                    shapes.line(vertices[0], vertices[1], vertices[5], vertices[6]);
                    shapes.line(vertices[5], vertices[6], vertices[10], vertices[11]);
                    shapes.line(vertices[10], vertices[11], vertices[15], vertices[16]);
                    shapes.line(vertices[15], vertices[16], vertices[0], vertices[1]);
                }
            }
        }
        if (this.drawMeshHull || this.drawMeshTriangles) {
            Array<Slot> slots = skeleton.getSlots();
            for (int i = 0, n = slots.size; i < n; i++) {
                Slot slot = (Slot) slots.get(i);
                Attachment attachment = slot.attachment;
                if (attachment instanceof MeshAttachment) {
                    MeshAttachment mesh = (MeshAttachment) attachment;
                    mesh.updateWorldVertices(slot, false);
                    float[] vertices = mesh.getWorldVertices();
                    short[] triangles = mesh.getTriangles();
                    int hullLength = mesh.getHullLength();
                    if (this.drawMeshTriangles) {
                        shapes.setColor(triangleLineColor);
                        for (int ii = 0, nn = triangles.length; ii < nn; ii += 3) {
                            int v1 = triangles[ii] * 5, v2 = triangles[ii + 1] * 5, v3 = triangles[ii + 2] * 5;
                            shapes.triangle(vertices[v1], vertices[v1 + 1], vertices[v2], vertices[v2 + 1], vertices[v3], vertices[v3 + 1]);
                        }
                    }
                    if (this.drawMeshHull && hullLength > 0) {
                        shapes.setColor(attachmentLineColor);
                        hullLength = (hullLength >> 1) * 5;
                        float lastX = vertices[hullLength - 5], lastY = vertices[hullLength - 4];
                        for (int ii = 0, nn = hullLength; ii < nn; ii += 5) {
                            float x = vertices[ii], y = vertices[ii + 1];
                            shapes.line(x, y, lastX, lastY);
                            lastX = x;
                            lastY = y;
                        }
                    }
                }
            }
        }
        if (this.drawBoundingBoxes) {
            SkeletonBounds bounds = this.bounds;
            bounds.update(skeleton, true);
            shapes.setColor(aabbColor);
            shapes.rect(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
            Array<FloatArray> polygons = bounds.getPolygons();
            Array<BoundingBoxAttachment> boxes = bounds.getBoundingBoxes();
            for (int i = 0, n = polygons.size; i < n; i++) {
                FloatArray polygon = (FloatArray) polygons.get(i);
                shapes.setColor(((BoundingBoxAttachment) boxes.get(i)).getColor());
                shapes.polygon(polygon.items, 0, polygon.size);
            }
        }
        if (this.drawPaths) {
            Array<Slot> slots = skeleton.getSlots();
            for (int i = 0, n = slots.size; i < n; i++) {
                Slot slot = (Slot) slots.get(i);
                Attachment attachment = slot.attachment;
                if (attachment instanceof PathAttachment) {
                    PathAttachment path = (PathAttachment) attachment;
                    int nn = path.getWorldVerticesLength();
                    float[] world = this.temp.setSize(nn);
                    path.computeWorldVertices(slot, world);
                    Color color = path.getColor();
                    float x1 = world[2], y1 = world[3], x2 = 0.0F, y2 = 0.0F;
                    if (path.getClosed()) {
                        shapes.setColor(color);
                        float cx1 = world[0], cy1 = world[1], cx2 = world[nn - 2], cy2 = world[nn - 1];
                        x2 = world[nn - 4];
                        y2 = world[nn - 3];
                        shapes.curve(x1, y1, cx1, cy1, cx2, cy2, x2, y2, 32);
                        shapes.setColor(Color.LIGHT_GRAY);
                        shapes.line(x1, y1, cx1, cy1);
                        shapes.line(x2, y2, cx2, cy2);
                    }
                    nn -= 4;
                    for (int ii = 4; ii < nn; ii += 6) {
                        float cx1 = world[ii], cy1 = world[ii + 1], cx2 = world[ii + 2], cy2 = world[ii + 3];
                        x2 = world[ii + 4];
                        y2 = world[ii + 5];
                        shapes.setColor(color);
                        shapes.curve(x1, y1, cx1, cy1, cx2, cy2, x2, y2, 32);
                        shapes.setColor(Color.LIGHT_GRAY);
                        shapes.line(x1, y1, cx1, cy1);
                        shapes.line(x2, y2, cx2, cy2);
                        x1 = x2;
                        y1 = y2;
                    }
                }
            }
        }
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        if (this.drawBones) {
            shapes.setColor(boneOriginColor);
            for (int i = 0, n = bones.size; i < n; i++) {
                Bone bone = (Bone) bones.get(i);
                shapes.setColor(Color.GREEN);
                float radius = 3f;
                if (bone == hoveredBone) {
                    radius *= hoverScale;
                }
                shapes.circle(skeletonX + bone.worldX, skeletonY + bone.worldY, radius * this.scale, 8);
            }
        }
        shapes.end();
    }

    public ShapeRenderer getShapeRenderer()
    {
        return this.shapes;
    }

    public void setBones(boolean bones)
    {
        this.drawBones = bones;
    }

    public void setScale(float scale)
    {
        this.scale = scale;
    }

    public void setRegionAttachments(boolean regionAttachments)
    {
        this.drawRegionAttachments = regionAttachments;
    }

    public void setBoundingBoxes(boolean boundingBoxes)
    {
        this.drawBoundingBoxes = boundingBoxes;
    }

    public void setMeshHull(boolean meshHull)
    {
        this.drawMeshHull = meshHull;
    }

    public void setMeshTriangles(boolean meshTriangles)
    {
        this.drawMeshTriangles = meshTriangles;
    }

    public void setPaths(boolean paths)
    {
        this.drawPaths = paths;
    }

    public void setPremultipliedAlpha(boolean premultipliedAlpha)
    {
        this.premultipliedAlpha = premultipliedAlpha;
    }

    private float distToCursor(Bone bone)
    {
        float skeletonX = bone.skeleton.getX();
        float skeletonY = bone.skeleton.getY();
        Vector2 dist = new Vector2(skeletonX + bone.worldX, skeletonY + bone.worldY)
                .sub(Gdx.input.getX(), Settings.HEIGHT - Gdx.input.getY());
        return dist.len2();
    }

    private boolean isHoveringBone(Bone bone)
    {
        if (Arrays.stream(bone.skeleton.getIkConstraints().items)
                .anyMatch(ikConstraint -> ikConstraint.getTarget().getData().name.equals(bone.getData().name))) {
            return false;
        }
        return distToCursor(bone) < 100;
    }

    public Bone getHoveredBone()
    {
        return hoveredBone;
    }
}

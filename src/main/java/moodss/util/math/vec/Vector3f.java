package moodss.util.math.vec;

import com.google.common.base.Objects;
import com.google.common.primitives.Floats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Quaternion;

/**
 * @author Moodss
 * Private copy from a util project of mine.
 */
public class Vector3f
{
    public static Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);
    public static Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
    public static Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);
    public static Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
    public static Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);
    public static Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);
    public static Vector3f ZERO = new Vector3f(0.0F, 0.0F, 0.0F);

    private float x, y, z;

    /**
     * @param x The x argument.
     * @param y The y argument.
     * @param z The z argument.
     */
    public Vector3f(float x, float y, float z)
    {
        this.x = Floats.isFinite(x) ? x : 0F;
        this.y = Floats.isFinite(y) ? y : 0F;
        this.z = Floats.isFinite(z) ? z : 0F;
    }

    /**
     * @param rotation The rotation argument.
     * @return A quaternion based off this vector and rotates it with the desired rotation provided.
     */
    public Quaternion rotation(float rotation)
    {
        float halvedRotation = rotation / 2F;
        float offset = MathHelper.sin(halvedRotation);
        return new Quaternion(this.x * offset, this.y * offset, this.z * offset, MathHelper.cos(halvedRotation));
    }

    /**
     * @param rotation The rotation argument.
     * @return A quaternion based off this vector and rotates it in degrees with the desired rotation provided.
     */
    public Quaternion rotationDegrees(float rotation)
    {
        rotation *= ((float)Math.PI / 180F);
        float halvedRotation = rotation / 2F;
        float offset = MathHelper.sin(halvedRotation);
        return new Quaternion(this.x * offset, this.y * offset, this.z * offset, MathHelper.cos(halvedRotation));
    }

    public Vector3f multiply(float scale)
    {
        return this.multiply(scale, scale, scale);
    }

    public Vector3f multiply(float scaleX, float scaleY, float scaleZ)
    {
        this.x *= scaleX;
        this.y *= scaleY;
        this.z *= scaleZ;

        return this;
    }

    @SideOnly(Side.CLIENT)
    public org.lwjgl.util.vector.Vector3f toVanilla()
    {
        return new org.lwjgl.util.vector.Vector3f(this.x, this.y, this.z);
    }

    public float getX()
    {
        return this.x;
    }

    public float getY()
    {
        return this.y;
    }

    public float getZ()
    {
        return this.z;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3f vector3f = (Vector3f) o;
        return Float.compare(vector3f.x, x) == 0 && Float.compare(vector3f.y, y) == 0 && Float.compare(vector3f.z, z) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(x, y, z);
    }
}

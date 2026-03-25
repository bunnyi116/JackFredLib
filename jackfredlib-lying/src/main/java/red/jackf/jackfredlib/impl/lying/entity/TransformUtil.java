package red.jackf.jackfredlib.impl.lying.entity;

import com.mojang.math.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TransformUtil {
    public static Transformation update(Transformation original,
                                        @Nullable Vector3f translation,
                                        @Nullable Quaternionf leftRotation,
                                        @Nullable Vector3f scale,
                                        @Nullable Quaternionf rightRotation) {
        return new Transformation(
                translation != null ? translation : original.translation(),
                leftRotation != null ? leftRotation : original.leftRotation(),
                scale != null ? scale : original.scale(),
                rightRotation != null ? rightRotation : original.rightRotation()
        );
    }
}

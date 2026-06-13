package red.jackf.jackfredlib.client.mixins.gps;

import net.minecraft.client.gui.Hud;
import net.minecraft.world.scores.PlayerScoreEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Comparator;

@Mixin(Hud.class)
public interface GuiAccessor {

    @Accessor("SCORE_DISPLAY_ORDER")
    static Comparator<PlayerScoreEntry> getScoreDisplayOrder() {
        throw new AssertionError("Mixin not applied correctly");
    }
}

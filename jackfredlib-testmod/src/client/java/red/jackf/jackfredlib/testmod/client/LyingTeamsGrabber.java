package red.jackf.jackfredlib.testmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

public class LyingTeamsGrabber {
    public static void setup() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("jackfredlib-testmod", "lying_teams_grabber"),
                (GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) -> {
                    var level = Minecraft.getInstance().level;
                    if (level == null) return;
                    int i = 35;
                    graphics.text(Minecraft.getInstance().font,
                            "Current teams (according to client):",
                            10, 25, 0xFF_FFFFFF);
                    for (var team : level.getScoreboard().getPlayerTeams()) {
                        graphics.text(Minecraft.getInstance().font,
                                team.getName(),
                                10, i, team.getColor().isColor() ? team.getColor().getColor() : 0xFF_FFFFFF);
                        i += 10;
                    }
                });
    }
}

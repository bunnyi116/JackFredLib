package red.jackf.jackfredlib.testmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.world.scores.TeamColor;

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
                        int color = team.getColor()
                                .map(TeamColor::rgb)
                                .orElse(0xFFFFFFFF);

                        graphics.text(Minecraft.getInstance().font,
                                team.getName(),
                                10, i, color);
                        i += 10;
                    }
                });
    }
}

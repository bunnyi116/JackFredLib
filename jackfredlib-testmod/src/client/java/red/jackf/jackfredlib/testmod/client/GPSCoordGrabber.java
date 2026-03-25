package red.jackf.jackfredlib.testmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import red.jackf.jackfredlib.client.api.gps.Coordinate;

public class GPSCoordGrabber {
    public static void setup() {
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("jackfredlib-testmod", "gps_coord_grabber"),
                (GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) -> graphics.text(Minecraft.getInstance().font,
                        Coordinate.getCurrent().map(Coordinate::toString).orElse("<no coordinate>"),
                        10, 10, 0xFF_AAFFAA));
    }
}

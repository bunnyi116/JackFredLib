package red.jackf.jackfredlib.testmod.client.colour;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import red.jackf.jackfredlib.api.colour.Colour;
import red.jackf.jackfredlib.api.colour.Colours;
import red.jackf.jackfredlib.api.colour.Gradient;
import red.jackf.jackfredlib.client.api.colour.GradientUtils;

public class ClosestColourScreen extends Screen {
    private DyeColor current = DyeColor.WHITE;
    private Colour currentColour = Colours.WHITE;

    public ClosestColourScreen() {
        super(Component.literal("Closest Colour Screen"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        GradientUtils.drawHorizontalGradient(guiGraphics, 3, 0, 300, 3, Gradient.of(Colours.BLACK, Colours.RED), 0f, 1f);
        GradientUtils.drawVerticalGradient(guiGraphics, 0, 3, 3, 300, Gradient.of(Colours.BLACK, Colours.GREEN), 0f, 1f);

        GradientUtils.drawHorizontalGradient(guiGraphics, 3, 3, 300, 300, currentColour, 0f, 1f);

        GradientUtils.drawHorizontalGradient(guiGraphics, 3, 340, 300, 60, Gradient.of(Colours.BLACK, Colour.fromRGB(0, 0, 255)), 0f, 1f);

        if (3 <= mouseX && mouseX < 303) {
            if (mouseY >= 3 && mouseY <= 303) { //r, g
                var newRed = (float) (mouseX - 3) / 300;
                var newGreen = (float) (mouseY - 3) / 300;

                this.currentColour = Colour.fromRGB((int) (255 * newRed), (int) (255 * newGreen), currentColour.b());
            } else if (mouseY >= 340 && mouseY < 400) {
                var newBlue = (float) (mouseX - 3) / 300;

                this.currentColour = Colour.fromRGB(currentColour.r(), currentColour.g(), (int) (255 * newBlue));
            }

            this.current = this.currentColour.closestDyeColour();
        }

        guiGraphics.text(this.font, "Current: %06x".formatted(this.currentColour.toARGB() & 0xFFFFFF), 320, 50, 0xFFFFFFFF);
        guiGraphics.text(this.font, "Current: %02d, %02d, %02d".formatted(this.currentColour.r(), this.currentColour.g(), this.currentColour.b()), 20, 50, 0xFFFFFFFF);
        guiGraphics.item(dyeItemFor(current).getDefaultInstance(), 320, 20);
    }

    private static net.minecraft.world.item.Item dyeItemFor(DyeColor colour) {
        return switch (colour) {
            case WHITE -> Items.WHITE_DYE;
            case ORANGE -> Items.ORANGE_DYE;
            case MAGENTA -> Items.MAGENTA_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case LIME -> Items.LIME_DYE;
            case PINK -> Items.PINK_DYE;
            case GRAY -> Items.GRAY_DYE;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
            case CYAN -> Items.CYAN_DYE;
            case PURPLE -> Items.PURPLE_DYE;
            case BLUE -> Items.BLUE_DYE;
            case BROWN -> Items.BROWN_DYE;
            case GREEN -> Items.GREEN_DYE;
            case RED -> Items.RED_DYE;
            case BLACK -> Items.BLACK_DYE;
        };
    }
}

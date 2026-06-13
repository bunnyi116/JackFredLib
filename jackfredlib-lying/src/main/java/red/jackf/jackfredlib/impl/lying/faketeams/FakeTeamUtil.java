package red.jackf.jackfredlib.impl.lying.faketeams;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import red.jackf.jackfredlib.api.base.ServerTracker;
import red.jackf.jackfredlib.api.colour.Colour;

import java.util.Arrays;
import java.util.List;

public class FakeTeamUtil {
    private FakeTeamUtil() {}

    static final List<ChatFormatting> COLOURS = Arrays.stream(ChatFormatting.values())
            .filter(Colour::isColor)
            .toList();

    @Contract("null -> null; !null -> !null")
    public static @Nullable ChatFormatting ensureValidColour(@Nullable ChatFormatting colour) {
        if (colour == null) return null;
        if (!Colour.isColor(colour)) return ChatFormatting.WHITE;
        return colour;
    }

    @Nullable
    static ClientboundSetPlayerTeamPacket.Parameters createFakeParameters(ChatFormatting colour) {
        var server = ServerTracker.INSTANCE.getServer();
        if (server == null) return null;

        PlayerTeam fakeTeam = new PlayerTeam(null, "fake");

        fakeTeam.setDisplayName(Component.literal(getName(colour)));
        fakeTeam.setNameTagVisibility(Team.Visibility.ALWAYS);
        fakeTeam.setCollisionRule(Team.CollisionRule.ALWAYS);
        fakeTeam.setColor(null);

        return new ClientboundSetPlayerTeamPacket.Parameters(fakeTeam);
    }

    static String getName(ChatFormatting colour) {
        return "JFLIB_LYING_" + colour.name();
    }
}

package uk.da5h1n.nerfed.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class WalkingStickItem extends Item {

    public static Consumer<Player> clientTap = null;

    public WalkingStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() && clientTap != null) {
            clientTap.accept(player);
        }
        return InteractionResult.SUCCESS;
    }
}

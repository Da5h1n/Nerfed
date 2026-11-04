package uk.da5h1n.nerfed.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

public class LimbInteractionHandler {
    public static void register() {

        PlayerBlockBreakEvents.BEFORE.register( (world, player, pos, state, blockEntity) -> {
            if (player.isSpectator()) return true;

            return !DisabilityDataHelper.isMainArmDisabled(player);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator()) return InteractionResult.PASS;

            if (DisabilityDataHelper.isHandDisabled(player, hand)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player.isSpectator()) return InteractionResult.PASS;

            if (DisabilityDataHelper.isHandDisabled(player, hand)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player.isSpectator()) return InteractionResult.PASS;

            if (DisabilityDataHelper.isHandDisabled(player, hand)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
    }
}

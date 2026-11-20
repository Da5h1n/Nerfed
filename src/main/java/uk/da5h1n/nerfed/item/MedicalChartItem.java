package uk.da5h1n.nerfed.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import uk.da5h1n.nerfed.network.ConditionsViewer;

public class MedicalChartItem extends Item {

    public MedicalChartItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer viewer) {
            ConditionsViewer.open(viewer, viewer);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Player)) return InteractionResult.PASS;

        if (player instanceof ServerPlayer viewer && target instanceof  ServerPlayer targetPlayer) {
            ConditionsViewer.open(viewer, targetPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}

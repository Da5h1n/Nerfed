package uk.da5h1n.nerfed.client;

import net.minecraft.server.level.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import uk.da5h1n.nerfed.Nerfed;
import uk.da5h1n.nerfed.util.Condition;
import uk.da5h1n.nerfed.util.DisabilityDataHelper;

/**
 * Loaded only by Simple Voice Chat (via the "voicechat" entrypoint), so the mod stays optional.
 */
public class NerfedVoicechatPlugin implements VoicechatPlugin {

    /**
     * true  = the server also drops a muted player's voice entirely (can't be bypassed by a modified client).
     * false = muffling only.
     */
    private static final boolean BLOCK_ON_SERVER = false;

    @Override
    public String getPluginId() {
        return Nerfed.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        Nerfed.LOGGER.info("[Nerfed] Simple Voice Chat detected: deafness affects voices, mute muffles them");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        // Client: voices you hear (proximity, positional and group/static)
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, this::onSound);
        registration.registerEvent(ClientReceiveSoundEvent.LocationalSound.class, this::onSound);
        registration.registerEvent(ClientReceiveSoundEvent.StaticSound.class, this::onSound);

        registration.registerEvent(ClientSoundEvent.class, this::onOwnMicrophone);

        if (BLOCK_ON_SERVER) {
            registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
        }
    }

    private void onSound(ClientReceiveSoundEvent event) {
        short[] audio = event.getRawAudio();
        if (audio == null || audio.length == 0) return;

        DeafnessEffect.processVoice(event.getId(), audio);
        event.setRawAudio(audio);
    }

    private void onOwnMicrophone(ClientSoundEvent event) {
        if (!VoiceMuffle.isMuted()) return;

        short[] audio = event.getRawAudio();
        if (audio == null || audio.length == 0) return;

        VoiceMuffle.process(audio);
        event.setRawAudio(audio);
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        if (connection == null || connection.getPlayer() == null) return;

        if (connection.getPlayer().getPlayer() instanceof ServerPlayer sender && isMuted(sender)) {
            event.cancel();
        }
    }

    private static boolean isMuted(ServerPlayer player) {
        DisabilityDataHelper.DisabilityData data = player.getAttached(DisabilityDataHelper.DISABILITY_DATA);
        return data != null && data.has(Condition.MUTE);
    }
}
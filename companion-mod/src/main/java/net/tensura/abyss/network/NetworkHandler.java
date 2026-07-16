package net.tensura.abyss.network;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.tensura.abyss.TensuraAbyss;
import net.tensura.abyss.client.ClientPayloadHandler;

/**
 * Registriert die Custom-Payloads. Laeuft auf dem MOD-Bus.
 *
 * WICHTIG: Der Client-Handler (oeffnet Screens) wird NUR auf dem Client
 * verdrahtet — so wird die Client-Klasse auf einem Dedicated Server nie geladen
 * (kein NoClassDefFoundError). Die Payload-Typen selbst sind auf beiden Seiten
 * registriert, damit die Protokoll-Verhandlung passt.
 */
@EventBusSubscriber(modid = TensuraAbyss.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class NetworkHandler {
    private NetworkHandler() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Client -> Server: Einladung
        registrar.playToServer(
                ServerboundGuildInvitePacket.TYPE,
                ServerboundGuildInvitePacket.STREAM_CODEC,
                ServerGuildInviteHandler::handle
        );

        // Server -> Client: Gilden-GUI oeffnen (Client-Handler nur auf Client)
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registrar.playToClient(
                    ClientboundOpenGuildScreenPayload.TYPE,
                    ClientboundOpenGuildScreenPayload.STREAM_CODEC,
                    ClientPayloadHandler::handleOpenGuild
            );
        } else {
            registrar.playToClient(
                    ClientboundOpenGuildScreenPayload.TYPE,
                    ClientboundOpenGuildScreenPayload.STREAM_CODEC,
                    (payload, context) -> { /* Server empfaengt dies nie */ }
            );
        }
    }
}

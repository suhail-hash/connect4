package de.hsos.connectfour.websocket;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * Konfigurator für WebSocket-Endpunkte zur Übergabe der HTTP-Session.
 *
 * Diese Klasse ermöglicht es, die bestehende HttpSession aus dem
 * HTTP-Handshake in den WebSocket-Kontext zu übernehmen.
 * Dadurch können benutzerspezifische Informationen
 * im WebSocket-Endpunkt weiterverwendet werden.
 */
public class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {

    /**
     * Wird während des WebSocket-Handshakes aufgerufen.
     * Übernimmt die bestehende HttpSession und speichert sie
     * in den UserProperties des WebSocket-Endpunkts.
     *
     * @param config   ServerEndpoint-Konfiguration
     * @param request  eingehende Handshake-Anfrage
     * @param response Handshake-Antwort
     */
    @Override
    public void modifyHandshake(ServerEndpointConfig config,
                                HandshakeRequest request,
                                HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        System.out.println("HttpSessionConfigurator.modifyHandshake: httpSession = " + httpSession);
        if (httpSession != null) {
            System.out.println("HttpSessionConfigurator: sessionId=" + httpSession.getId());
            config.getUserProperties().put(HttpSession.class.getName(), httpSession);
        } else {
            System.out.println("HttpSessionConfigurator: httpSession is null (no JSESSIONID?)");
        }
    }
}
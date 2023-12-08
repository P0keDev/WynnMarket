package dev.p0ke.wynnmarket.minecraft.auth;

import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.auth.service.AuthenticationService;
import com.github.steveice10.mc.auth.service.SessionService;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import java.net.Proxy;

public class MinecraftAccount {
    private final String username;
    private final String password;
    private final int classIndex;

    private Session client;

    public MinecraftAccount(String username, String password, int classIndex) {
        this.username = username;
        this.password = password;
        this.classIndex = classIndex;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public Session getClient() {
        return client;
    }

    public void login(String clientId) {
        MinecraftProtocol protocol;
        try {
            AuthenticationService authService = new CustomAuthenticationService(clientId);
            authService.setUsername(username);
            authService.setPassword(password);
            authService.setProxy(Proxy.NO_PROXY);
            authService.login();

            protocol = new MinecraftProtocol(authService.getSelectedProfile(), authService.getAccessToken());
            System.out.println("Successfully authenticated user.");
        } catch (RequestException e) {
            e.printStackTrace();
            return;
        }


        SessionService sessionService = new SessionService();
        sessionService.setProxy(Proxy.NO_PROXY);

        client = new TcpClientSession("lobby.wynncraft.com", 25565, protocol, null);
        client.setFlag(MinecraftConstants.SESSION_SERVICE_KEY, sessionService);
    }

    public void logout() {
        if (client == null) return;
        client.disconnect("Finished");
    }

}

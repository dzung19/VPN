package com.example.androidvpn.data;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001$B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u000bJ\u0016\u0010\u0016\u001a\u00020\u00012\u0006\u0010\u0017\u001a\u00020\u0018H\u0086@\u00a2\u0006\u0002\u0010\u0019J\u000e\u0010\u001a\u001a\u00020\u0014H\u0086@\u00a2\u0006\u0002\u0010\u001bJ\u0010\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u0018H\u0002J\u001c\u0010\u001f\u001a\n !*\u0004\u0018\u00010 0 *\u00020 2\u0006\u0010\"\u001a\u00020#H\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006%"}, d2 = {"Lcom/example/androidvpn/data/TunnelManager;", "", "<init>", "()V", "TAG", "", "backend", "Lcom/wireguard/android/backend/Backend;", "currentTunnel", "Lcom/example/androidvpn/data/TunnelManager$InternalTunnel;", "appContext", "Landroid/content/Context;", "_tunnelState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/wireguard/android/backend/Tunnel$State;", "tunnelState", "Lkotlinx/coroutines/flow/StateFlow;", "getTunnelState", "()Lkotlinx/coroutines/flow/StateFlow;", "init", "", "context", "startTunnel", "config", "Lcom/example/androidvpn/model/ServerConfig;", "(Lcom/example/androidvpn/model/ServerConfig;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "stopTunnel", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "buildWireGuardConfig", "Lcom/wireguard/config/Config;", "serverConfig", "addAllowedLib", "Lcom/wireguard/config/Peer$Builder;", "kotlin.jvm.PlatformType", "network", "Lcom/wireguard/config/InetNetwork;", "InternalTunnel", "app_debug"})
public final class TunnelManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "TunnelManager";
    @org.jetbrains.annotations.Nullable()
    private static com.wireguard.android.backend.Backend backend;
    @org.jetbrains.annotations.Nullable()
    private static com.example.androidvpn.data.TunnelManager.InternalTunnel currentTunnel;
    @org.jetbrains.annotations.Nullable()
    private static android.content.Context appContext;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.MutableStateFlow<com.wireguard.android.backend.Tunnel.State> _tunnelState = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.flow.StateFlow<com.wireguard.android.backend.Tunnel.State> tunnelState = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.androidvpn.data.TunnelManager INSTANCE = null;
    
    private TunnelManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.wireguard.android.backend.Tunnel.State> getTunnelState() {
        return null;
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object startTunnel(@org.jetbrains.annotations.NotNull()
    com.example.androidvpn.model.ServerConfig config, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<java.lang.Object> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object stopTunnel(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final com.wireguard.config.Config buildWireGuardConfig(com.example.androidvpn.model.ServerConfig serverConfig) {
        return null;
    }
    
    private final com.wireguard.config.Peer.Builder addAllowedLib(com.wireguard.config.Peer.Builder $this$addAllowedLib, com.wireguard.config.InetNetwork network) {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\b\u0010\u0006\u001a\u00020\u0003H\u0016J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/example/androidvpn/data/TunnelManager$InternalTunnel;", "Lcom/wireguard/android/backend/Tunnel;", "name", "", "<init>", "(Ljava/lang/String;)V", "getName", "onStateChange", "", "newState", "Lcom/wireguard/android/backend/Tunnel$State;", "app_debug"})
    public static final class InternalTunnel implements com.wireguard.android.backend.Tunnel {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String name = null;
        
        public InternalTunnel(@org.jetbrains.annotations.NotNull()
        java.lang.String name) {
            super();
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String getName() {
            return null;
        }
        
        @java.lang.Override()
        public void onStateChange(@org.jetbrains.annotations.NotNull()
        com.wireguard.android.backend.Tunnel.State newState) {
        }
    }
}
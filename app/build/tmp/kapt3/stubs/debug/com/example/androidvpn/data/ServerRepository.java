package com.example.androidvpn.data;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u001b\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010\u0015\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010\u0016\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0010\u0010\u0017\u001a\u0004\u0018\u00010\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0018\u001a\u00020\u00122\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u0002J\u0010\u0010\u001a\u001a\u00020\u000f2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J\u0006\u0010\u001d\u001a\u00020\u001eJ\u0010\u0010\u001f\u001a\u0004\u0018\u00010\u000fH\u0086@\u00a2\u0006\u0002\u0010\u0010R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u000bX\u0082D\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/example/androidvpn/data/ServerRepository;", "", "context", "Landroid/content/Context;", "cloudflareService", "Lcom/example/androidvpn/data/CloudflareService;", "<init>", "(Landroid/content/Context;Lcom/example/androidvpn/data/CloudflareService;)V", "prefs", "Landroid/content/SharedPreferences;", "KEY_CONFIGS", "", "KEY_CURRENT_ID", "getConfigs", "", "Lcom/example/androidvpn/model/ServerConfig;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "addConfig", "", "config", "(Lcom/example/androidvpn/model/ServerConfig;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "removeConfig", "saveCurrentConfig", "getCurrentConfig", "saveConfigsList", "list", "parseConfig", "json", "Lorg/json/JSONObject;", "generateKeyPair", "Lcom/wireguard/crypto/KeyPair;", "createCloudflareConfig", "app_debug"})
public final class ServerRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.androidvpn.data.CloudflareService cloudflareService = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String KEY_CONFIGS = "saved_configs";
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String KEY_CURRENT_ID = "current_config_id";
    
    @javax.inject.Inject()
    public ServerRepository(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.example.androidvpn.data.CloudflareService cloudflareService) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getConfigs(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.androidvpn.model.ServerConfig>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object addConfig(@org.jetbrains.annotations.NotNull()
    com.example.androidvpn.model.ServerConfig config, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object removeConfig(@org.jetbrains.annotations.NotNull()
    com.example.androidvpn.model.ServerConfig config, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object saveCurrentConfig(@org.jetbrains.annotations.NotNull()
    com.example.androidvpn.model.ServerConfig config, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getCurrentConfig(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.androidvpn.model.ServerConfig> $completion) {
        return null;
    }
    
    private final void saveConfigsList(java.util.List<com.example.androidvpn.model.ServerConfig> list) {
    }
    
    private final com.example.androidvpn.model.ServerConfig parseConfig(org.json.JSONObject json) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.wireguard.crypto.KeyPair generateKeyPair() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createCloudflareConfig(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.androidvpn.model.ServerConfig> $completion) {
        return null;
    }
}
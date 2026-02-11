package com.example.androidvpn.ui;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\b\u0010\u001a\u001a\u00020\u001bH\u0002J\u0006\u0010\u001c\u001a\u00020\u001bJ\u000e\u0010\u001d\u001a\u00020\u001b2\u0006\u0010\u001e\u001a\u00020\u0013J\u000e\u0010\u001f\u001a\u00020\u001b2\u0006\u0010\u001e\u001a\u00020\u0013J\u0016\u0010 \u001a\u00020\u001b2\u0006\u0010!\u001a\u00020\u000f2\u0006\u0010\"\u001a\u00020\u000fJ\u0006\u0010#\u001a\u00020\u001bJ\b\u0010(\u001a\u00020\u001bH\u0002J\b\u0010)\u001a\u00020\u001bH\u0002J\b\u0010*\u001a\u00020\u001bH\u0002J\u0010\u0010+\u001a\u00020\u000f2\u0006\u0010,\u001a\u00020\'H\u0002J\b\u0010-\u001a\u0004\u0018\u00010.R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0016\u0010\u0012\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00130\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0014\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00130\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\fR\u001a\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u00170\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u00170\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\fR\u0010\u0010$\u001a\u0004\u0018\u00010%X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020\'X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006/"}, d2 = {"Lcom/example/androidvpn/ui/HomeViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "repository", "Lcom/example/androidvpn/data/ServerRepository;", "application", "Landroid/app/Application;", "<init>", "(Lcom/example/androidvpn/data/ServerRepository;Landroid/app/Application;)V", "vpnState", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/wireguard/android/backend/Tunnel$State;", "getVpnState", "()Lkotlinx/coroutines/flow/StateFlow;", "connectionDuration", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "getConnectionDuration", "()Lkotlinx/coroutines/flow/MutableStateFlow;", "_currentConfig", "Lcom/example/androidvpn/model/ServerConfig;", "currentConfig", "getCurrentConfig", "_configs", "", "configs", "getConfigs", "loadData", "", "createCloudflareConfig", "addConfig", "config", "selectConfig", "parseAndAddConfig", "name", "configText", "toggleVpn", "timerJob", "Lkotlinx/coroutines/Job;", "startTime", "", "startTimerObserver", "startTimer", "stopTimer", "formatDuration", "millis", "checkVpnPermission", "Landroid/content/Intent;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HomeViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.androidvpn.data.ServerRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.wireguard.android.backend.Tunnel.State> vpnState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> connectionDuration = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.androidvpn.model.ServerConfig> _currentConfig = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.androidvpn.model.ServerConfig> currentConfig = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.androidvpn.model.ServerConfig>> _configs = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.androidvpn.model.ServerConfig>> configs = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job timerJob;
    private long startTime = 0L;
    
    @javax.inject.Inject()
    public HomeViewModel(@org.jetbrains.annotations.NotNull()
    com.example.androidvpn.data.ServerRepository repository, @org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.wireguard.android.backend.Tunnel.State> getVpnState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> getConnectionDuration() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.androidvpn.model.ServerConfig> getCurrentConfig() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.androidvpn.model.ServerConfig>> getConfigs() {
        return null;
    }
    
    private final void loadData() {
    }
    
    public final void createCloudflareConfig() {
    }
    
    public final void addConfig(@org.jetbrains.annotations.NotNull()
    com.example.androidvpn.model.ServerConfig config) {
    }
    
    public final void selectConfig(@org.jetbrains.annotations.NotNull()
    com.example.androidvpn.model.ServerConfig config) {
    }
    
    public final void parseAndAddConfig(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    java.lang.String configText) {
    }
    
    public final void toggleVpn() {
    }
    
    private final void startTimerObserver() {
    }
    
    private final void startTimer() {
    }
    
    private final void stopTimer() {
    }
    
    private final java.lang.String formatDuration(long millis) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.content.Intent checkVpnPermission() {
        return null;
    }
}
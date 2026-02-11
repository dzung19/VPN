package com.example.androidvpn.data;

@kotlin.Metadata(mv = {2, 3, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u00020\u00032\b\b\u0001\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0007\u00c0\u0006\u0003"}, d2 = {"Lcom/example/androidvpn/data/CloudflareApi;", "", "register", "Lcom/example/androidvpn/data/RegistrationResponse;", "body", "Lcom/example/androidvpn/data/RegistrationRequest;", "(Lcom/example/androidvpn/data/RegistrationRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface CloudflareApi {
    
    @retrofit2.http.POST(value = "v0a2404/reg")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object register(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.androidvpn.data.RegistrationRequest body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.example.androidvpn.data.RegistrationResponse> $completion);
}
package com.parkshare.frontend.utils;

public interface RepositoryCallback<T> {
    void onSuccess(T data);

    void onError(String message);
}

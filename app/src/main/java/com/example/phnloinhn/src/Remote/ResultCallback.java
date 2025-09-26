// ResultCallback.java
package com.example.phnloinhn.src.Remote;

public interface ResultCallback<T> {
    void onSuccess(T result);
    void onFailure(Exception e);
}

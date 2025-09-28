package com.example.phnloinhn.src.Remote;

import android.util.Log;

import com.example.phnloinhn.src.Model.GrowingMethod;
import com.example.phnloinhn.src.Model.History;
import com.example.phnloinhn.src.Model.LonganVariant;
import com.google.firebase.firestore.*;

import java.util.*;

public class FirestoreHelper {

    private static final String TAG = "FirestoreHelper";
    private final FirebaseFirestore db;
    private String uid;

    public FirestoreHelper(String uid) {
        this.db = FirebaseFirestore.getInstance();
        this.uid = uid;
    }

    // Fetch all variants with growing methods
    public void getAllVariants(ResultCallback<Map<String, LonganVariant>> callback) {
        Map<String, LonganVariant> longanData = new HashMap<>();

        db.collection("longan_variants").get()
                .addOnSuccessListener(variantDocs -> {
                    for (QueryDocumentSnapshot variantDoc : variantDocs) {
                        String variantId = variantDoc.getId();
                        LonganVariant variant = variantDoc.toObject(LonganVariant.class);

                        db.collection("longan_variants")
                                .document(variantId)
                                .collection("growing_methods")
                                .get()
                                .addOnSuccessListener(methodDocs -> {
                                    Map<String, GrowingMethod> methodsData = new HashMap<>();
                                    for (QueryDocumentSnapshot methodDoc : methodDocs) {
                                        methodsData.put(methodDoc.getId(), methodDoc.toObject(GrowingMethod.class));
                                    }
                                    variant.setGrowingMethods(methodsData);
                                    longanData.put(variantId, variant);

                                    callback.onSuccess(longanData);
                                })
                                .addOnFailureListener(callback::onFailure);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getAllHistory(ResultCallback<List<History>> callback) {
        db.collection("users")
                .document(uid)
                .collection("history")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<History> historyList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        History history = doc.toObject(History.class);
                        historyList.add(history);
                    }

                    callback.onSuccess(historyList);
                })
                .addOnFailureListener(callback::onFailure);
    }
    public void addHistory(History history, ResultCallback<Boolean> callback){
        String docName = history.getVariantName() + "_" + history.getTimestamp();
        db.collection("users")
                .document(uid)
                .collection("history")
                .document(docName)
                .set(history)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "History added for user " + uid);
                    callback.onSuccess(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add history", e);
                    callback.onFailure(e);
                });
    }

    // FirestoreHelper.java
    public void deleteHistory(History history, ResultCallback<Boolean> callback) {
        String docName = history.getVariantName() + "_" + history.getTimestamp();
        db.collection("users")
                .document(uid)
                .collection("history")
                .document(docName)  // use timestamp as the docId
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(callback::onFailure);
    }

}

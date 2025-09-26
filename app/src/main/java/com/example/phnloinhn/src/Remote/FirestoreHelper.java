package com.example.phnloinhn.src.Remote;

import com.example.phnloinhn.src.Model.GrowingMethod;
import com.example.phnloinhn.src.Model.LonganVariant;
import com.google.firebase.firestore.*;

import java.util.*;

public class FirestoreHelper {

    private static final String TAG = "FirestoreHelper";
    private final FirebaseFirestore db;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
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

    // Fetch one variant by ID
    public void getVariantById(String id, ResultCallback<LonganVariant> callback) {
        db.collection("longan_variants").document(id).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        LonganVariant variant = doc.toObject(LonganVariant.class);

                        db.collection("longan_variants")
                                .document(id)
                                .collection("growing_methods")
                                .get()
                                .addOnSuccessListener(methodDocs -> {
                                    Map<String, GrowingMethod> methodsData = new HashMap<>();
                                    for (QueryDocumentSnapshot methodDoc : methodDocs) {
                                        methodsData.put(methodDoc.getId(), methodDoc.toObject(GrowingMethod.class));
                                    }
                                    if (variant != null) {
                                        variant.setGrowingMethods(methodsData);
                                    }
                                    callback.onSuccess(variant);
                                });
                    } else {
                        callback.onFailure(new Exception("Variant not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}

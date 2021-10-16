package org.elvincode.fall_detector_v2.repositories;

import android.content.Intent;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.elvincode.fall_detector_v2.models.HistoryLogDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HistoryLogRepositories {

    private static HistoryLogRepositories instance;
    private static Integer id_num = 0;
    private static String userId;
    private ArrayList<HistoryLogDisplay> dataSet = new ArrayList<>();
    private MutableLiveData<Integer> dataSetSize = new MutableLiveData<>();
    public ArrayList<HistoryLogDisplay> getDataSet() { return dataSet; }
    public MutableLiveData<Boolean> isUpdating = new MutableLiveData<>();

    public void setIsUpdating(Boolean state){
        isUpdating.setValue(state);
    }

    public LiveData<Boolean> getIsUpdating() { return isUpdating; }

    public static HistoryLogRepositories getInstance(){
        if(instance == null){
            instance = new HistoryLogRepositories();
        }
        return instance;
    }

    public static HistoryLogRepositories getInstance(String _userId){
        if(instance == null && userId != _userId){
            instance = new HistoryLogRepositories();
            userId = _userId;
        }
        return instance;
    }

    public LiveData<Integer> getDataSetSize(){
        dataSetSize.setValue(dataSet.size());
        return dataSetSize;
    }

    public MutableLiveData<List<HistoryLogDisplay>> getHistoryLogs() {
        isUpdating.setValue(false);
        setHistoryLog();
        MutableLiveData<List<HistoryLogDisplay>> data = new MutableLiveData<>();
        data.setValue(dataSet);
        return data;
    }

    private void setHistoryLog(){
        if(dataSet.size() > 0) { dataSet.clear(); }
        CollectionReference colRef = FirebaseFirestore.getInstance().collection("users").document(userId).collection("history_logs");
        colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        dataSet.add(new HistoryLogDisplay(doc.getString("dateTime"), doc.getString("fallState"), doc.getString("location")));
                    }
                }
            }
        });
    }

}

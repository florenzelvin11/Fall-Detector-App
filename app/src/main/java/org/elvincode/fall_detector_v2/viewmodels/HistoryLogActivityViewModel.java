package org.elvincode.fall_detector_v2.viewmodels;

import android.os.Handler;
import android.os.Looper;
import android.text.BoringLayout;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.elvincode.fall_detector_v2.models.HistoryLogDisplay;
import org.elvincode.fall_detector_v2.repositories.HistoryLogRepositories;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryLogActivityViewModel extends ViewModel {

    private MutableLiveData<List<HistoryLogDisplay>> mHistoryLogs;
    private HistoryLogRepositories mRepo;
    private MutableLiveData<Boolean> mIsUpdating = new MutableLiveData<>();
    private MutableLiveData<Integer> mDataSetSize = new MutableLiveData<>();

    public void init(String userId){
        if(mHistoryLogs != null){
            return;
        }

        mRepo = HistoryLogRepositories.getInstance(userId);
        mRepo.setIsUpdating(false);
    }

    public LiveData<List<HistoryLogDisplay>> getHistoryLogs() { return mRepo.getHistoryLogs(); }

    public LiveData<Boolean> getIsUpdating() {return mRepo.isUpdating; }

    public LiveData<Integer> getDataSetSize() { return mRepo.getDataSetSize(); }

}

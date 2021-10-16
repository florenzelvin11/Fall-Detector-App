package org.elvincode.fall_detector_v2.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Looper;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import org.elvincode.fall_detector_v2.R;
import org.elvincode.fall_detector_v2.models.User;
import org.elvincode.fall_detector_v2.services.BluetoothService;
import org.elvincode.fall_detector_v2.viewmodels.MainActivityViewModel;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;

public class HomePageRVAdapter extends RecyclerView.Adapter<HomePageRVAdapter.ViewHolder>{

    private static final String TAG = "homePageRVAdapter";

    private final Context mContext;
    private ArrayList<User> users = new ArrayList<>();

    public HomePageRVAdapter(Context context) {
        this.mContext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private final CardView cardView;
        private final TextView txtStateHealth;
        private final TextView txtProfileName;
        private final TextView txtAge;
        private final TextView txtGender;
        private final TextView txtCondition;
        private final LinearLayout ll_extraInfo;
        private final RelativeLayout ll_extraImg;
        private final ImageView btnArrowUp;
        private final ImageView btnArrowDown;
        private final ImageView imgProfile;

        private BluetoothService mService;
        private MainActivityViewModel mViewModel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.parent);
            txtStateHealth = itemView.findViewById(R.id.statusCard_txtHealthState);
            txtProfileName = itemView.findViewById(R.id.statusCard_txtProfileName);
            txtAge = itemView.findViewById(R.id.statusCard_txtAge);
            txtGender = itemView.findViewById(R.id.statusCard_txtGender);
            txtCondition = itemView.findViewById(R.id.statusCard_txtCondition);
            ll_extraInfo = itemView.findViewById(R.id.statusCard_linearLayout_extra_info);
            ll_extraImg = itemView.findViewById(R.id.statusCard_layout_profile_img);
            btnArrowDown = itemView.findViewById(R.id.btn_arrowDown);
            btnArrowUp = itemView.findViewById(R.id.btn_arrowUp);
            imgProfile = itemView.findViewById(R.id.statusCard_imgProfileDp);

            btnArrowUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = users.get(getAdapterPosition());
                    user.setExpanded(!user.isExpanded());
                    notifyItemChanged(getAdapterPosition());
                }
            });

            btnArrowDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = users.get(getAdapterPosition());
                    user.setExpanded(!user.isExpanded());
                    notifyItemChanged(getAdapterPosition());
                }
            });

        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_page_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomePageRVAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Called");

        String name = users.get(position).getFirst_name() + " " + users.get(position).getLast_name();
        String healthState = "State: " + users.get(position).getHealth_state();
        String mAge = "Age: " + users.get(position).getStringAge();
        String mGender = "Gender: " + users.get(position).getGender();
        String mCondition = "Condition: " + users.get(position).getCondition();

        holder.txtProfileName.setText(name);
        holder.txtStateHealth.setText(healthState);
        holder.txtAge.setText(mAge);
        holder.txtGender.setText(mGender);
        holder.txtCondition.setText(mCondition);

        Uri tmpImg = users.get(position).getImgProfileDp();
        if(tmpImg != null){
            Picasso.with((Activity )mContext).load(users.get(position).getImgProfileDp().toString()).into(holder.imgProfile);
        }

        // Expands the Card View Holder
        if(!users.get(position).isExpanded()){
            TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
            holder.btnArrowDown.setVisibility(View.VISIBLE);
            holder.ll_extraImg.setVisibility(View.GONE);
            holder.ll_extraInfo.setVisibility(View.GONE);
        }else{
            TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
            holder.ll_extraImg.setVisibility(View.VISIBLE);
            holder.ll_extraInfo.setVisibility(View.VISIBLE);
            holder.btnArrowDown.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
        notifyDataSetChanged();
    }
}

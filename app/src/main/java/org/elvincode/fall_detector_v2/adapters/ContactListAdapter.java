package org.elvincode.fall_detector_v2.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.telephony.SmsManager;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import org.elvincode.fall_detector_v2.R;
import org.elvincode.fall_detector_v2.models.ContactsModel;
import org.elvincode.fall_detector_v2.models.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ViewHolder>{
    private static final String TAG = "ContactListAdapter";

    private Context mContext;
    private List<ContactsModel> mContacts = new ArrayList<>();
    private String number;
    private static final int REQUEST_CALL = 1;
    private String current_fav;
    private static final String MAX_FAV = "1";
    private DocumentReference docRef;

    public ContactListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView txtName, txtNumber;
        private ImageView btnArrowUp, btnArrowDown, btnFave;
        private Button btnSMSAlert, btnCall;
        private LinearLayout ll_extraInfo;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.parent);
            txtName = itemView.findViewById(R.id.contactCard_txtProfileName);
            txtNumber = itemView.findViewById(R.id.contactCard_txtNumber);
            btnArrowUp = itemView.findViewById(R.id.btn_arrowUp);
            btnArrowDown = itemView.findViewById(R.id.btn_arrowDown);
            btnSMSAlert = itemView.findViewById(R.id.btn_sms_alert);
            btnCall = itemView.findViewById(R.id.btn_call);
            ll_extraInfo = itemView.findViewById(R.id.contactCard_linearLayout_extra_info);
            btnFave = itemView.findViewById(R.id.btn_fav_state);

            if(docRef != null){
                docRef.addSnapshotListener((Activity) mContext, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                        String tmpVal = value.getString("fav_num");
                        if(tmpVal == null){
                            docRef.update("fav_num", "0");
                        }else if(tmpVal != null && tmpVal == "0" ){
                            current_fav = tmpVal;
                        }else if(tmpVal != null && tmpVal == "1"){
                            current_fav = tmpVal;
                        }
                    }
                });
            }
            btnFave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactsModel contact = mContacts.get(getAdapterPosition());
                    DocumentReference contactRef =  docRef.collection("contacts").document(contact.getName());
                    if(current_fav != MAX_FAV && !contact.getFavourite()){
                        docRef.update("fav_num", "1");
                        docRef.update("emergencyPhoneNum", contact.getPhoneNumber());
                        contactRef.update("favourite", true);
                        contact.setFavourite(true);
                    }else if(current_fav == MAX_FAV && contact.getFavourite()){
                        contact.setFavourite(false);
                        docRef.update("fav_num", "0");
                        docRef.update("emergencyPhoneNum", "");
                        contactRef.update("favourite", false);
                    }else{
                        contact.setFavourite(false);
                    }
                    notifyItemChanged(getAdapterPosition());
                }
            });

            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    number = mContacts.get(getAdapterPosition()).getPhoneNumber();
                    makePhoneCall();
                }
            });

            btnSMSAlert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    number = mContacts.get(getAdapterPosition()).getPhoneNumber();
                    validateSMS();
                }
            });

            btnArrowUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactsModel contact = mContacts.get(getAdapterPosition());
                    contact.setExpanded(!contact.getExpanded());
                    notifyItemChanged(getAdapterPosition());
                }
            });

            btnArrowDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactsModel contact = mContacts.get(getAdapterPosition());
                    contact.setExpanded(!contact.getExpanded());
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_page_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ContactListAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Called");

        String name = mContacts.get(position).getName();
        String number = mContacts.get(position).getPhoneNumber();

        holder.txtName.setText(name);
        holder.txtNumber.setText(number);

        if(mContacts.get(position).getFavourite()){
            Glide.with(mContext).load(R.drawable.fav_contact_select).placeholder(R.drawable.fav_contact_select).into(holder.btnFave);
        }else{
            Glide.with(mContext).load(R.drawable.fav_contact_unselect).placeholder(R.drawable.fav_contact_unselect).into(holder.btnFave);
        }

        // Expands the Card View Holder
        if(!mContacts.get(position).getExpanded()){
            TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
            holder.btnArrowDown.setVisibility(View.VISIBLE);
            holder.ll_extraInfo.setVisibility(View.GONE);
        }else{
            TransitionManager.beginDelayedTransition(holder.cardView, new AutoTransition());
            holder.ll_extraInfo.setVisibility(View.VISIBLE);
            holder.btnArrowDown.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public void addContacts(ContactsModel contacts){
        mContacts.add(contacts);
        notifyDataSetChanged();
    }

    public void setContacts(ArrayList<ContactsModel> contact){
        this.mContacts = contact;
        notifyDataSetChanged();
    }

    public void setDocRef(DocumentReference _docRef){
        this.docRef = _docRef;
    }

    private void makePhoneCall(){
        if(number == null) { return; }

        if(number.trim().length() > 0){
            if(ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            }else{
                String dial  = "tel:" + number;
                mContext.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        }else{
            Toast.makeText(mContext, "No Phone Number", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateSMS(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
                sendSMS();
            }else{
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.SEND_SMS},1);
            }
        }
    }

    private void sendSMS(){
        if(number == null) { return; }
        String msg = "Requesting Check Up!";
        try{
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, msg, null, null);
            Toast.makeText((Activity) mContext, "MessageSent", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText((Activity) mContext, "Failed to send msg", Toast.LENGTH_SHORT).show();
        }
    }
}

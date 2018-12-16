package cgodin.qc.ca.projetfinal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {

    private Context mContext;
    private  List<String> username;
    private List<String> avatar;
    int selected_position = -1;



    public Adapter(Context mContext, List<String> username, List<String> avatar) {
        this.mContext = mContext;
        this.username = username;
        this.avatar = avatar;
    }



    @Override
    public int getItemCount() {
        return username.size();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.model, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
       if(position>=0 && avatar.size() > position && username.size() > position && avatar.size() > 0 && username.size() > 0){
           byte[] decodedString = Base64.decode(avatar.get(position), Base64.DEFAULT);
           Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0,decodedString.length);

           holder.imgViewCompte.setImageBitmap(bmp);
           holder.txtViewCompte.setText(username.get(position));
           holder.itemView.setBackgroundColor(selected_position == position ? Color.GREEN : Color.TRANSPARENT);
           holder.parentLayout.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   notifyItemChanged(selected_position);
                   selected_position = position;
                   notifyItemChanged(selected_position);
                   Intent intent = new Intent("custom-message");
                   intent.putExtra("username",username.get(position));
                   LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
               }
           });
       }

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgViewCompte;
        TextView txtViewCompte;
        LinearLayout parentLayout;
       public MyViewHolder(View itemView){
           super(itemView);
           imgViewCompte = itemView.findViewById(R.id.imageView);
           txtViewCompte = itemView.findViewById(R.id.textView);
           parentLayout = itemView.findViewById(R.id.parent_layout);

       }

    }

}
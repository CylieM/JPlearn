package com.example.jlearnn;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


public class LessonItemAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private Context context;
    private List<LessonItemDataClass> dataList;

    public LessonItemAdapter(Context context, List<LessonItemDataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reecycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.recJapaneseChar.setText(dataList.get(position).getJapaneseChar());
        holder.recRomaji.setText(dataList.get(position).getDataRomaji());
        holder.recDesc.setText(dataList.get(position).getDataDesc());
        // holder.recExample.setText(dataList.get(position).getDataExample());

        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LessonItemDetailActivity.class);
                intent.putExtra("JapaneseChar", dataList.get(holder.getAdapterPosition()).getJapaneseChar());
                intent.putExtra("Description", dataList.get(holder.getAdapterPosition()).getDataDesc());
                intent.putExtra("Romaji", dataList.get(holder.getAdapterPosition()).getDataRomaji());
                intent.putExtra("Key", dataList.get(holder.getAdapterPosition()).getKey());
                intent.putExtra("Example", dataList.get(holder.getAdapterPosition()).getDataExample());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void searchDataList(ArrayList<LessonItemDataClass> searchList) {
        dataList = searchList;
        notifyDataSetChanged();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {
    TextView recJapaneseChar, recRomaji, recDesc;
    CardView recCard;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        recJapaneseChar = itemView.findViewById(R.id.recJapaneseChar);
        recCard = itemView.findViewById(R.id.recCard);
        recDesc = itemView.findViewById(R.id.recDesc);
        recRomaji = itemView.findViewById(R.id.recRomaji);
        // recExample = itemView.findViewById(R.id.recExample);
    }
}



package com.almacen.alamacen202.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.almacen.alamacen202.R;
import com.almacen.alamacen202.SetterandGetters.EnvTraspasos;
import com.almacen.alamacen202.SetterandGetters.Traspasos;

import java.util.ArrayList;

public class AdaptadorEnvTraspasos extends RecyclerView.Adapter<AdaptadorEnvTraspasos.ViewHolderEnvTraspasos> {

    private ArrayList<EnvTraspasos> datos;
    private int index;
    public AdaptadorEnvTraspasos(ArrayList<EnvTraspasos> datos) {
        this.datos = datos;
    }//constructor

    @Override
    public AdaptadorEnvTraspasos.ViewHolderEnvTraspasos onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_env_traspaso,
                null, false);
        return new AdaptadorEnvTraspasos.ViewHolderEnvTraspasos(view);
    }//oncreateViewHolder

    @Override
    public void onBindViewHolder(AdaptadorEnvTraspasos.ViewHolderEnvTraspasos holder, int position) {
        holder.n.setText(datos.get(position).getNum());
        holder.tvItemP.setText(datos.get(position).getProducto());
        holder.tvItemU.setText(datos.get(position).getUbic());
        holder.tvItemC.setText(datos.get(position).getCantidad());
        holder.tvItemE.setText(datos.get(position).getExistencia());
        holder.tvItemS.setText(datos.get(position).getCantSurt());

        if(index==position){
            holder.lyaoutEnv.setBackgroundResource(R.color.ColorTenue);

        }else{
            holder.lyaoutEnv.setBackgroundColor(0);
        }

        if(datos.get(position).isSincronizado()){
            holder.n.setTextColor(Color.parseColor("#043B72"));
            holder.tvItemP.setTextColor(Color.parseColor("#000000"));
            holder.tvItemU.setTextColor(Color.parseColor("#043B72"));
            holder.tvItemC.setTextColor(Color.parseColor("#ECBF15"));
            holder.tvItemE.setTextColor(Color.parseColor("#1E739A"));
            holder.tvItemS.setTextColor(Color.parseColor("#32997C"));
        }else{
            holder.n.setTextColor(Color.parseColor("#223CCA"));
            holder.tvItemP.setTextColor(Color.parseColor("#223CCA"));
            holder.tvItemU.setTextColor(Color.parseColor("#223CCA"));
            holder.tvItemC.setTextColor(Color.parseColor("#223CCA"));
            holder.tvItemE.setTextColor(Color.parseColor("#223CCA"));
            holder.tvItemS.setTextColor(Color.parseColor("#223CCA"));
        }
    }//onBindViewHolder

    public int index(int index){
        this.index=index;
        return index;
    }
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }//getItemType

    @Override
    public int getItemCount() {
        return datos.size();
    }

    public static class ViewHolderEnvTraspasos extends RecyclerView.ViewHolder {
        TextView n,tvItemP, tvItemU,tvItemC,tvItemE,tvItemS;
        LinearLayout lyaoutEnv;
        public ViewHolderEnvTraspasos (View itemView) {
            super(itemView);
            n= itemView.findViewById(R.id.tvN);
            tvItemP =  itemView.findViewById(R.id.tvItemP);
            tvItemU =  itemView.findViewById(R.id.tvItemU);
            tvItemC = itemView.findViewById(R.id.tvItemC);
            tvItemE = itemView.findViewById(R.id.tvItemE);
            tvItemS = itemView.findViewById(R.id.tvItemS);
            lyaoutEnv  = itemView.findViewById(R.id.lyaoutEnv);
        }//constructor
    }//AdapterTraspasosViewHolder class
}//principal

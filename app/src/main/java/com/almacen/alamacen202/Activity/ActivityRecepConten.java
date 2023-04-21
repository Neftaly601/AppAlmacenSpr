package com.almacen.alamacen202.Activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.almacen.alamacen202.Adapter.AdaptadorRecepConten;
import com.almacen.alamacen202.Adapter.AdaptadorTraspasos;
import com.almacen.alamacen202.Adapter.AdapterDifUbiExi;
import com.almacen.alamacen202.MainActivity;
import com.almacen.alamacen202.R;
import com.almacen.alamacen202.SetterandGetters.RecepConten;
import com.almacen.alamacen202.SetterandGetters.RecepListSucCont;
import com.almacen.alamacen202.SetterandGetters.Traspasos;
import com.almacen.alamacen202.Sqlite.ConexionSQLiteHelper;
import com.almacen.alamacen202.XML.XMLRecepConsul;
import com.almacen.alamacen202.XML.XMLRecepMultSuc;
import com.almacen.alamacen202.includes.HttpHandler;
import com.almacen.alamacen202.includes.MyToolbar;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import dmax.dialog.SpotsDialog;

public class ActivityRecepConten extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private SharedPreferences preference;
    private boolean revisa=false;
    private float existMtz=0,existCdmx=0,existCul=0,existMty=0,existRep=0,repMtz=0,repCdmx=0,repCul=0,repMty=0;
    private float faltMtz=0,faltCdmx=0,faltCul=0,faltMty=0;
    private float demBMtz=0,demBCdmx=0,demBCul=0,demBMty=0;
    private int posicion=0,escaneados=0;

    private String strusr,strpass,strbran,strServer,codeBar,mensaje,folio="",producto="",clasf="",sucursalSelec="";
    private ArrayList<RecepConten> listaRecep = new ArrayList<>();
    private ArrayList<RecepListSucCont> listaSucRecep = new ArrayList<>();
    private LinearLayout lyMatrz,lyCdmx,lyCul,lyMty;
    private EditText txtBfolio,txtProdR,txtSuc,txtRest,txtEscan,txtProdVi;
    private ImageView ivProdR;
    private TextView tvRepMatr,tvRepCdmx,tvRepCul,tvRepMty;
    private Button btnBuscaFolio,btnAtras,btnAdelante,btnSave;
    private CheckBox chbManual;
    private RecyclerView rvRecep;
    private AdaptadorRecepConten adapter;
    private AlertDialog mDialog;
    private ConexionSQLiteHelper conn;
    private SQLiteDatabase db;
    private InputMethodManager keyboard;
    private String urlImagenes,extImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recep_conten);

        MyToolbar.show(this, "Recep. Conten. x prod.", true);
        preference = getSharedPreferences("Login", Context.MODE_PRIVATE);
        strusr = preference.getString("user", "null");
        strpass = preference.getString("pass", "null");
        strbran = preference.getString("codBra", "null");
        strServer = preference.getString("Server", "null");
        codeBar = preference.getString("codeBar", "null");
        urlImagenes=preference.getString("urlImagenes", "null");
        extImg=preference.getString("ext", "null");

        mDialog = new SpotsDialog.Builder().setContext(ActivityRecepConten.this).
                setMessage("Espere un momento...").build();
        mDialog.setCancelable(false);

        progressDialog = new ProgressDialog(ActivityRecepConten.this);//parala barra de
        progressDialog.setMessage("Procesando datos....");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        lyMatrz         = findViewById(R.id.lyMatrz);
        lyCdmx          = findViewById(R.id.lyCdmx);
        lyCul           = findViewById(R.id.lyCul);
        lyMty           = findViewById(R.id.lyMty);
        txtBfolio       = findViewById(R.id.txtBfolio);
        txtProdR        = findViewById(R.id.txtProdR);
        txtSuc          = findViewById(R.id.txtSuc);
        txtRest         = findViewById(R.id.txtRest);
        txtEscan        = findViewById(R.id.txtEscan);
        btnBuscaFolio   = findViewById(R.id.btnBuscaFolio);
        tvRepMatr       = findViewById(R.id.tvRepMatr);
        tvRepCdmx       = findViewById(R.id.tvRepCdmx);
        tvRepCul        = findViewById(R.id.tvRepCul);
        tvRepMty        = findViewById(R.id.tvRepMty);
        ivProdR         = findViewById(R.id.ivProdR);
        chbManual       = findViewById(R.id.chbManual);
        btnSave         = findViewById(R.id.btnSave);
        txtProdVi       = findViewById(R.id.txtProdVi);


        btnAtras    = findViewById(R.id.btnBack);
        btnAdelante =findViewById(R.id.btnNext);

        conn = new ConexionSQLiteHelper(ActivityRecepConten.this, "bd_INVENTARIO", null, 1);
        db = conn.getReadableDatabase();//apertura de la base de datos interna
        rvRecep    = findViewById(R.id.rvRecep);
        rvRecep.setLayoutManager(new LinearLayoutManager(ActivityRecepConten.this));
        adapter = new AdaptadorRecepConten(listaRecep);
        keyboard = (InputMethodManager) getSystemService(ActivityRecepConten.INPUT_METHOD_SERVICE);

        chbManual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                txtProdR.setText("");
                txtProdVi.setText("");
                posicion=-1;
                mostrarLista();
                seleccionaSuc();
                if(b==true){
                    txtProdR.requestFocus();
                    txtEscan.setEnabled(true);
                    btnSave.setEnabled(true);
                    btnSave.setBackgroundTintList(ColorStateList.
                            valueOf(getResources().getColor(R.color.colorPrimary)));
                }else{
                    txtEscan.setEnabled(false);
                    btnSave.setEnabled(false);
                    btnSave.setBackgroundTintList(ColorStateList.
                            valueOf(getResources().getColor(R.color.ColorGris)));
                    txtProdR.requestFocus();
                    //mostrarDatosProd();
                }
            }//oncheckedchange
        });//chbManual

        btnBuscaFolio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                folio=txtBfolio.getText().toString();
                if(!folio.equals("")){
                    new AsyncRecepCon().execute();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepConten.this);
                    builder.setMessage("Campo Vacío");
                    builder.setCancelable(false);
                    builder.setTitle("AVISO");
                    builder.setNegativeButton("OK", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }//else
            }//onclick
        });//btnBuscaFolio

        txtProdR.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                producto=editable.toString();
                if(!editable.toString().equals("")){
                    txtProdVi.setText(producto);
                    if (codeBar.equals("Zebra")) {
                        if(chbManual.isChecked()){//si esta en modo manual
                            txtEscan.requestFocus();
                            keyboard.showSoftInput(txtEscan, InputMethodManager.SHOW_IMPLICIT);
                        }else{
                            escanear(producto,0,false,1);
                            txtProdR.setText("");
                        }//else
                    }else{
                        for (int i = 0; i < editable.length(); i++) {
                            char ban;
                            ban = editable.charAt(i);
                            if (ban == '\n') {
                                if(chbManual.isChecked()){//si esta en modo manual
                                    txtEscan.requestFocus();
                                    keyboard.showSoftInput(txtEscan, InputMethodManager.SHOW_IMPLICIT);
                                }else{
                                    escanear(producto,0,false,1);
                                    txtProdR.setText("");
                                }//else
                                break;
                            }//if
                        }//for
                    }//else
                }//if es diferente a vacio
            }//after
        });//txtProd textchange
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!txtEscan.getText().equals("") && !producto.equals("")){
                    int c=Integer.parseInt(txtEscan.getText().toString());
                    escanear(producto,c,true,0);
                    keyboard.hideSoftInputFromWindow(txtEscan.getWindowToken(), 0);
                    if(revisa=false){//cuando se realizo el cambio correctamente
                        txtProdR.setText("");
                        txtProdVi.setText("");
                        txtProdR.requestFocus();
                        producto="";
                        posicion=-1;
                        mostrarLista();
                    }else{
                        mostrarDatosProd();
                    }//else
                }else{
                    Toast.makeText(ActivityRecepConten.this, "Campos vacíos", Toast.LENGTH_SHORT).show();
                }//else
            }//onclick
        });//btnSave

        btnAdelante.setOnClickListener(new View.OnClickListener() {//boton adelante
            @Override
            public void onClick(View view) {
                posicion++;
                seleccionaProd();
            }//onclick
        });//btnadelante setonclicklistener

        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posicion--;
                seleccionaProd();
            }//onclick
        });//btnatras setonclicklistener
    }//onCreate
    public void escanear(String producto,int cantidad,boolean sumarOno,int sum){
        revisa=false;
        if(!producto.equals("")){
            boolean find=false;
            escaneados=0;
            for(int i=0;i<listaRecep.size();i++){
                if(listaRecep.get(i).getProducto().equals(producto)){
                    posicion=i;
                    find=true;
                    switch (sucursalSelec){
                        case "Matriz":
                            if(sumarOno==true){//determina si se modifica o se suma
                                escaneados=cantidad;
                            }else{
                                escaneados=Integer.parseInt(listaRecep.get(i).getEscanMtrz())+sum;
                            }//else
                            if(escaneados<=repMtz){
                                listaRecep.get(i).setEscanMtrz(escaneados+"");
                            }else{
                                revisa=true;
                                Toast.makeText(this, "Excede cantidad a repartir", Toast.LENGTH_SHORT).show();
                            }//else
                            break;
                        case "CDMX.":
                            if(sumarOno==true){//determina si se modifica o se suma
                                escaneados=cantidad;
                            }else{
                                escaneados=Integer.parseInt(listaRecep.get(i).getEscanCdmx())+sum;
                            }//else
                            if(escaneados<=repCdmx){
                                listaRecep.get(i).setEscanCdmx(escaneados+"");
                            }else{
                                revisa=true;
                                Toast.makeText(this, "Excede cantidad a repartir", Toast.LENGTH_SHORT).show();
                            }//else
                            break;
                        case "Culiacan":
                            if(sumarOno==true){//determina si se modifica o se suma
                                escaneados=cantidad;
                            }else{
                                escaneados=Integer.parseInt(listaRecep.get(i).getEscanCul())+sum;
                            }//else
                            if(escaneados<=repCul){
                                listaRecep.get(i).setEscanCul(escaneados+"");
                            }else{
                                revisa=true;
                                Toast.makeText(this, "Excede cantidad a repartir", Toast.LENGTH_SHORT).show();
                            }//else
                            break;
                        case "Monterrey":
                            if(sumarOno==true){//determina si se modifica o se suma
                                escaneados=cantidad;
                            }else{
                                escaneados=Integer.parseInt(listaRecep.get(i).getEscanMty())+sum;
                            }//else
                            if(escaneados<=repMty){
                                listaRecep.get(i).setEscanMty(escaneados+"");
                            }else{
                                revisa=true;
                                Toast.makeText(this, "Excede cantidad a repartir", Toast.LENGTH_SHORT).show();
                            }//else
                            break;
                        default :break;
                    }//switch

                    break;
                }//if
            }//for
            if(find==false){
                posicion=-1;
                Toast.makeText(this, "No existe producto", Toast.LENGTH_SHORT).show();
            }//if
        }//if
    }//escanear

    public void limiteBotDir(){
        if(posicion==0){
            btnAdelante.setEnabled(true);
            btnAdelante.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.colorPrimary)));
            btnAtras.setEnabled(false);
            btnAtras.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));

        }else if(posicion+1==listaRecep.size()){
            btnAtras.setEnabled(true);
            btnAtras.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.colorPrimary)));
            btnAdelante.setEnabled(false);
            btnAdelante.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
        }else{
            btnAtras.setEnabled(true);
            btnAtras.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.colorPrimary)));
            btnAdelante.setEnabled(true);
            btnAdelante.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.colorPrimary)));
        }//else
    }//cambiaProd

    public void onClickSuc(View v){
        if(!producto.equals("")){
            sucursalSelec="";
            switch(v.getId()){
                case R.id.lyMatrz:
                    sucursalSelec="Matriz";
                    break;
                case R.id.lyCdmx:
                    sucursalSelec="CDMX.";
                    break;
                case R.id.lyCul:
                    sucursalSelec="Culiacan";
                    break;
                case R.id.lyMty:
                    sucursalSelec="Monterrey";
                    break;
            }//switch
            seleccionaSuc();
        }//si hay producto seleccionado
    }//onClickSuc

    public void seleccionaSuc(){
        if(!producto.equals("")){
            switch(sucursalSelec){
                case "Matriz":
                    escaneados=Integer.parseInt(listaRecep.get(posicion).getEscanMtrz());
                    tvRepMatr.setBackgroundResource(R.color.ColorGris);
                    tvRepCdmx.setBackgroundResource(0);
                    tvRepCul.setBackgroundResource(0);
                    tvRepMty.setBackgroundResource(0);
                    break;
                case "CDMX.":
                    escaneados=Integer.parseInt(listaRecep.get(posicion).getEscanCdmx());
                    tvRepCdmx.setBackgroundResource(R.color.ColorGris);
                    tvRepMatr.setBackgroundResource(0);
                    tvRepCul.setBackgroundResource(0);
                    tvRepMty.setBackgroundResource(0);
                    break;
                case "Culiacan":
                    escaneados=Integer.parseInt(listaRecep.get(posicion).getEscanCul());
                    tvRepCul.setBackgroundResource(R.color.ColorGris);
                    tvRepMatr.setBackgroundResource(0);
                    tvRepCdmx.setBackgroundResource(0);
                    tvRepMty.setBackgroundResource(0);
                    break;
                case "Monterrey":
                    escaneados=Integer.parseInt(listaRecep.get(posicion).getEscanMty());
                    tvRepMty.setBackgroundResource(R.color.ColorGris);
                    tvRepMatr.setBackgroundResource(0);
                    tvRepCdmx.setBackgroundResource(0);
                    tvRepCul.setBackgroundResource(0);
                    break;
                default:
                    escaneados=0;
                    tvRepMatr.setBackgroundResource(0);
                    tvRepCdmx.setBackgroundResource(0);
                    tvRepCul.setBackgroundResource(0);
                    tvRepMty.setBackgroundResource(0);
                    break;
            }//switch
            txtSuc.setText(sucursalSelec);
            txtEscan.setText(escaneados+"");
            int totalEsc=Integer.parseInt(listaRecep.get(posicion).getEscanMtrz())+
                    Integer.parseInt(listaRecep.get(posicion).getEscanCdmx())+
                    Integer.parseInt(listaRecep.get(posicion).getEscanCul())+
                    Integer.parseInt(listaRecep.get(posicion).getEscanMty());
            txtRest.setText((Integer.parseInt(listaRecep.get(posicion).getCantidad())-totalEsc)+"");
        }//si hay producto seleccionado
        else{
            limpiarCampos();
        }//else
    }//seleccionaSuc

    public void limpiarCampos(){
        txtProdVi.setText("");
        txtSuc.setText("");
        txtRest.setText("0");
        txtEscan.setText("0");
        tvRepMatr.setText("0");
        tvRepCdmx.setText("0");
        tvRepCul.setText("0");
        tvRepMty.setText("0");
        escaneados=0;
        tvRepMatr.setBackgroundResource(0);
        tvRepCdmx.setBackgroundResource(0);
        tvRepCul.setBackgroundResource(0);
        tvRepMty.setBackgroundResource(0);
        ivProdR.setImageResource(R.drawable.logokepler);
    }//limpiarCampos

    public void onClickListaR(View v){//cada vez que se seleccione un producto en la lista
        posicion = rvRecep.getChildPosition(rvRecep.findContainingItemView(v));
        seleccionaProd();
    }//onClickLista

    public void seleccionaProd(){
        //seleccionaSuc(null);
        producto=listaRecep.get(posicion).getProducto();
        adapter.index(posicion);
        adapter.notifyDataSetChanged();
        rvRecep.scrollToPosition(posicion);
        new AsyncRecepXProd().execute();
    }//seleccionaProd

    public void mostrarDatosProd(){
        repMtz=0;repCdmx=0;repCul=0;repMty=0;
        txtProdVi.setText(producto);
        Picasso.with(getApplicationContext()).
                load(urlImagenes+producto+extImg)
                .error(R.drawable.aboutlogo)
                .fit()
                .centerInside()
                .into(ivProdR);
        asignarReparticiones();
        limiteBotDir();
    }//mostrarDatosProd

    public float calculoDem(float demanda,String clasf){//
        float dem=0;
        if(clasf.equals("A")){
            dem=Float.parseFloat((demanda*2.5)+"");
        }else if(clasf.equals("B")){
            dem=Float.parseFloat((demanda*1.5)+"");
        }else{
            dem=Float.parseFloat(demanda+"");
        }
        return dem;
    }//calculoDem
    public float calculoFalt(float demanda,float exist){//
        float falt=demanda-exist;
        if(falt<0){falt=0;}
        return falt;
    }//calculoFalt

    public float calculoExistBal(String suc,float exist,float falt){
        float existBal=0;
        if(!suc.equals("01")){
            if(falt>0){
                existBal=exist;
            }
        }else{
            existBal=exist;
        }//else
        return existBal;
    }//calculoExistBal

    public float calculoDemBal(String suc,float demanda,float falt){
        float demBal=demanda;
        if(!suc.equals("01")){
            demBal=0;
            if(falt>0){
                demBal=demanda;
            }
        }else{
            demBal=demanda;
        }//else
        return demBal;
    }//calculoDemBal

    public float redondeo(float op){
        float redo=0,result=0;
        if(op>1){//REDONDEO MENOS
            redo=Math.round(op);
            if(redo>op){//es para redondear  menos, si el redondeo es mayor al original entonces se le resta uno para que sea redondeo menos
                result=redo-1;
            }else{
                result=redo;
            }
        }else{//REDONDEO MAS
            redo=Math.round(op);
            result=redo;
        }//else
        return result;
    }

    public float calculoRepartir(float ind,float demBal,float exist,float falt){
        float op1=0,op2=0;
        if(ind<1){
            op1=(demBal*ind);
            op1=op1-exist;
        }else{
            op1=falt;
        }//else

        if(op1<0){
            op2=0;
        }else{
            op2=op1;
        }//else
        return redondeo(op2);
    }

    public void calculosFinal(float sumDemBal){
        float indice=0;
        repMtz=0;repCdmx=0;repCul=0;repMty=0;
        if(existMtz!=0){
            indice=Float.parseFloat((existRep/sumDemBal)+"");
            repCdmx=calculoRepartir(indice,demBCdmx,existCdmx,faltCdmx);
            repCul=calculoRepartir(indice,demBCul,existCul,faltCul);
            repMty=calculoRepartir(indice,demBMty,existMty,faltMty);
            repMtz=existMtz-repCdmx-repCul-repMty;
        }//else

        tvRepMatr.setText(Math.round(repMtz)+"");
        tvRepCdmx.setText(Math.round(repCdmx)+"");
        tvRepCul.setText(Math.round(repCul)+"");
        tvRepMty.setText(Math.round(repMty)+"");
    }//calculosFinal


    public void asignarReparticiones(){
        float compr=0,trans=0,existB=0,demXClasf=0,exist,faltante,demB,sumDemBal=0,demanda=0;
        existMtz=0;existCdmx=0;existCul=0;existMty=0;
        faltMtz=0;faltCdmx=0;faltCul=0;faltMty=0;demBMtz=0;demBCdmx=0;demBCul=0;demBMty=0;existRep=0;
        String suc;
        for(int i=0;i<listaSucRecep.size();i++){
            compr=0;trans=0;exist=0;demanda=0;faltante=0;existB=0;demB=0;
            suc=listaSucRecep.get(i).getSucursal();
            clasf=listaSucRecep.get(i).getClasif();
            exist=Integer.parseInt(listaSucRecep.get(i).getExist());
            compr=Integer.parseInt(listaSucRecep.get(i).getCompr());
            trans=Integer.parseInt(listaSucRecep.get(i).getTrans());
            exist=(exist-compr)+trans;
            demXClasf=Integer.parseInt(listaSucRecep.get(i).getDem());
            demanda=calculoDem(demXClasf,clasf);
            faltante=calculoFalt(demanda,exist);
            existB=calculoExistBal(suc,exist,faltante);
            demB=calculoDemBal(suc,demanda,faltante);
            sumDemBal=sumDemBal+demB;
            existRep=existRep+existB;
            switch (suc){
                case "01":
                    existMtz=exist;
                    faltMtz=faltante;
                    demBMtz=demB;
                    break;
                case "06":
                    existCdmx=exist;
                    faltCdmx=faltante;
                    demBCdmx=demB;
                    break;
                case "07":
                    existCul=exist;
                    faltCul=faltante;
                    demBCul=demB;
                    break;
                case "08":
                    existMty=exist;
                    faltMty=faltante;
                    demBMty=demB;
                    break;
                default:limpiarCampos();break;
            }//switch
        }//for

        calculosFinal(sumDemBal);
        escanear(producto,0,false,0);
    }//asignarReparticiones

    public void mostrarLista(){
        adapter= new AdaptadorRecepConten(listaRecep);
        rvRecep.setAdapter(adapter);
        adapter.index(posicion);
        adapter.notifyDataSetChanged();
        rvRecep.scrollToPosition(posicion);
    }//mostrar lista



    private class AsyncRecepCon extends AsyncTask<Void, Boolean, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            mensaje="";
            posicion=-1;
            limpiarCampos();
            listaRecep.clear();
            rvRecep.setAdapter(null);
        }//onPreExecute

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            String parametros="k_folio="+folio;
            String url = "http://"+strServer+"/"+getString(R.string.resRecepConten)+"?"+parametros;
            String jsonStr = sh.makeServiceCall(url,strusr,strpass);
            //Log.e(TAG, "Respuesta de la url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Obtener array de datos
                    JSONArray jsonArray = jsonObj.getJSONArray("Response");
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject dato = jsonArray.getJSONObject(i);//Conjunto de datos
                        listaRecep.add(new RecepConten((i+1)+"",dato.getString("k_prod"),dato.getString("k_cant"),
                                dato.getString("k_paletCaja"),dato.getString("k_prio"),"0","0","0","0"));
                        mensaje="";
                    }//for
                } catch (final JSONException e) {
                    //Log.e(TAG, "Error al convertir Json: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mensaje="Puede que el folio no exista";
                        }//run
                    });
                }//catch JSON EXCEPTION
            }else {
                //Log.e(TAG, "Problemas al traer datos");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mensaje="No fue posible obtener datos del servidor";
                    }//run
                });//runUniTthread
            }//else
            return null;
        }//doInBackground

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mDialog.dismiss();
            if (listaRecep.size()==0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepConten.this);
                builder.setTitle("AVISO");
                builder.setMessage(mensaje);
                builder.setCancelable(false);
                builder.setNegativeButton("OK",null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                posicion=-1;
                mostrarLista();
                txtProdR.requestFocus();
                txtProdR.setInputType(InputType.TYPE_NULL);
                btnAtras.setEnabled(true);
                btnAdelante.setEnabled(true);
            }//else
        }//onPost
    }//AsyncRecepCon

    private class AsyncRecepXProd extends AsyncTask<Void, Boolean, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            mensaje="";
            sucursalSelec="";
            listaSucRecep.clear();
            limpiarCampos();
        }//onPreExecute

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();//separar párametros con &
            String parametros="k_folio="+folio+"&k_producto="+producto;
            String url = "http://"+strServer+"/"+getString(R.string.resRecepXProd)+"?"+parametros;
            String jsonStr = sh.makeServiceCall(url,strusr,strpass);
            //Log.e(TAG, "Respuesta de la url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Obtener array de datos
                    JSONArray jsonArray = jsonObj.getJSONArray("Response");
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject dato = jsonArray.getJSONObject(i);//Conjunto de datos
                        listaSucRecep.add(new RecepListSucCont(dato.getString("k_suc"),dato.getString("k_clasf"),
                                dato.getString("k_exist"),dato.getString("k_compr"),dato.getString("k_trans"),
                                dato.getString("k_dem")));
                        mensaje="";
                    }//for
                    switch (listaSucRecep.get(0).getSucursal()){
                        case "01":
                            sucursalSelec="Matriz";
                            break;
                        case "06":
                            sucursalSelec="CDMX.";
                            break;
                        case "07":
                            sucursalSelec="Culiacan";
                            break;
                        case "08":
                            sucursalSelec="Monterrey";
                            break;
                    }
                } catch (final JSONException e) {
                    //Log.e(TAG, "Error al convertir Json: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mensaje="Error al traer datos";
                        }//run
                    });
                }//catch JSON EXCEPTION
            } else {
                //Log.e(TAG, "Problemas al traer datos");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mensaje="No fue posible obtener datos del servidor";
                    }//run
                });//runUniTthread
            }//else
            return null;
        }//doInBackground

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mDialog.dismiss();
            if(listaSucRecep.size()==0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepConten.this);
                builder.setTitle("AVISO");
                builder.setMessage(mensaje);
                builder.setCancelable(false);
                builder.setNegativeButton("OK",null);
                AlertDialog dialog = builder.create();
                dialog.show();limpiarCampos();
            }else{
                mostrarDatosProd();
                seleccionaSuc();
            }//else
        }//onPost
    }//AsyncRecepCon
}//ActivityRecepConten

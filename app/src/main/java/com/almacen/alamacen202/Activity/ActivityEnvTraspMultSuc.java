package com.almacen.alamacen202.Activity;

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
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.almacen.alamacen202.Adapter.AdaptadorCajas;
import com.almacen.alamacen202.Adapter.AdaptadorCajaxProd;
import com.almacen.alamacen202.Adapter.AdaptadorEnvTraspasos;
import com.almacen.alamacen202.Adapter.AdaptadorListAlmacenes;
import com.almacen.alamacen202.Adapter.AdaptadorTraspasos;
import com.almacen.alamacen202.Adapter.AdapterDifUbiExi;
import com.almacen.alamacen202.Adapter.AdapterListaCajas;
import com.almacen.alamacen202.R;
import com.almacen.alamacen202.SetterandGetters.CAJASSANDG;
import com.almacen.alamacen202.SetterandGetters.CajaXProd;
import com.almacen.alamacen202.SetterandGetters.EnvTraspasos;
import com.almacen.alamacen202.SetterandGetters.Traspasos;
import com.almacen.alamacen202.Sqlite.ConexionSQLiteHelper;
import com.almacen.alamacen202.XML.XMLRecepConsul;
import com.almacen.alamacen202.XML.XMLRecepMultSuc;
import com.almacen.alamacen202.includes.HttpHandler;
import com.almacen.alamacen202.includes.MyToolbar;
import com.google.android.material.textfield.TextInputLayout;
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
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class ActivityEnvTraspMultSuc extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private SharedPreferences preference;
    private int posicion=0,posicion2=0,posG=0,CAJAACT=1,posCaja=0,cantCajaOr,pC=0;
    private String strusr,strpass,strbran,strServer,codeBar,mensaje;
    private String Producto="",serv,Folio="",prodSelectCaj="",cajaActAl="";
    private ArrayList<EnvTraspasos> lista = new ArrayList<>();
    private ArrayList<CAJASSANDG> listaCajas = new ArrayList<>();
    private ArrayList<CAJASSANDG> listaCajasXProd = new ArrayList<>();
    private ArrayList <String> listaLineas= new ArrayList<>();
    private ArrayList<String> nomCajas= new ArrayList<>();
    private EditText txtFolBusq,txtCantidad,txtCantSurt,txtProducto,tvCaja,txtEnv;
    private AutoCompleteTextView spLineas;
    private ImageView ivProd;
    private TextView tvProd;
    private Button btnBuscarFol,btnAtras,btnAdelante,btnAggCaja,btnListaCajas,btnVerCajas;
    private RecyclerView rvEnvTrasp;
    private AdaptadorEnvTraspasos adapter;
    private AdapterListaCajas adapListCaj = new AdapterListaCajas(listaCajas);
    private AlertDialog mDialog;
    private InputMethodManager keyboard;
    private String urlImagenes,extImg;
    //private boolean cajaGuard=false;
    private int sonido_correcto,sonido_error;
    private SoundPool bepp;

    //VARIABLES PARA AALERT DE LISTA DE PROD
    private RecyclerView rvListaCajas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_env_trasp_mult);

        MyToolbar.show(this, "Envío Traspasos Multisucursal", true);
        preference = getSharedPreferences("Login", Context.MODE_PRIVATE);
        strusr = preference.getString("user", "null");
        strpass = preference.getString("pass", "null");
        strbran = preference.getString("codBra", "null");
        strServer = preference.getString("Server", "null");
        codeBar = preference.getString("codeBar", "null");
        urlImagenes=preference.getString("urlImagenes", "null");
        extImg=preference.getString("ext", "null");

        mDialog = new SpotsDialog.Builder().setContext(ActivityEnvTraspMultSuc.this).
                setMessage("Espere un momento...").build();
        mDialog.setCancelable(false);

        progressDialog = new ProgressDialog(ActivityEnvTraspMultSuc.this);//parala barra de
        progressDialog.setMessage("Procesando datos....");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        txtFolBusq    = findViewById(R.id.txtFolBusq);
        txtCantidad = findViewById(R.id.txtCantidad);
        txtCantSurt = findViewById(R.id.txtCantSurt);
        tvProd      = findViewById(R.id.tvProd);
        btnBuscarFol  = findViewById(R.id.btnBuscarFol);
        btnAtras    = findViewById(R.id.btnAtras);
        btnAdelante =findViewById(R.id.btnAdelante);
        ivProd      = findViewById(R.id.ivProd);
        txtProducto = findViewById(R.id.txtProducto);
        tvCaja = findViewById(R.id.tvCaja);
        btnAggCaja = findViewById(R.id.btnAggCaja);
        btnListaCajas = findViewById(R.id.btnListaCajas);
        btnVerCajas = findViewById(R.id.btnVerCajas);
        txtEnv = findViewById(R.id.txtEnv);
        spLineas = findViewById(R.id.spLineas);

        rvEnvTrasp    = findViewById(R.id.rvEnvTrasp);
        rvEnvTrasp.setLayoutManager(new LinearLayoutManager(ActivityEnvTraspMultSuc.this));
        adapter = new AdaptadorEnvTraspasos(lista);
        keyboard = (InputMethodManager) getSystemService(ActivityEnvTraspMultSuc.INPUT_METHOD_SERVICE);

        bepp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        sonido_correcto = bepp.load(ActivityEnvTraspMultSuc.this, R.raw.sonido_correct, 1);
        sonido_error = bepp.load(ActivityEnvTraspMultSuc.this, R.raw.error, 1);

        txtFolBusq.requestFocus();
        txtProducto.setInputType(InputType.TYPE_NULL);

        txtFolBusq.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().equals("")){
                    Folio= editable.toString();
                }//if es diferente a vacio
            }//after
        });//txtProd textchange

        txtProducto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                Producto=editable.toString();
                if(!editable.toString().equals("")){
                    if (codeBar.equals("Zebra")) {
                        posicion2=posG;
                        cambio(Producto,true,false);
                        txtProducto.setText("");
                    }else{
                        for (int i = 0; i < editable.length(); i++) {
                            char ban;
                            ban = editable.charAt(i);
                            if (ban == '\n') {
                                posicion2=posG;
                                cambio(Producto,true,false);
                                txtProducto.setText("");
                                break;
                            }//if
                        }//for
                    }//else
                }//if es diferente a vacio
            }//after
        });//txtProd textchange

        btnBuscarFol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Folio.equals("")){
                    Folio=folio(Folio);
                    new AsyncConsulEnvTrasp(strbran,Folio).execute();
                }else{
                    Toast.makeText(ActivityEnvTraspMultSuc.this, "Folio vacío", Toast.LENGTH_SHORT).show();
                }

            }//onclick
        });//btnGuardar setonclick


        btnAdelante.setOnClickListener(new View.OnClickListener() {//boton adelante
            @Override
            public void onClick(View view) {
                posicion2=posicion;
                cambio("next",false,false);
            }//onclick
        });//btnadelante setonclicklistener

        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posicion2=posicion;
                cambio("back",false,false);
            }//onclick
        });//btnatras setonclicklistener

        btnAggCaja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean t=false;
                for(int j=0;j<lista.size();j++){
                    if(lista.get(j).isSincronizado()==false){
                        t=true;
                        posicion2=j;
                        cambio(lista.get(j).getProducto(),false,true);
                        break;
                    }//if
                }//for
                if(t==false){
                    mensajeAggCaja();
                }//else
            }//onclick
        });

        btnListaCajas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncConsultCA(strbran,Folio).execute();
            }
        });//btnListaCajas

        btnVerCajas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncConsultCP(strbran,Folio,tvProd.getText().toString()).execute();
            }
        });//btnVerCajas

        spLineas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!listaLineas.get(i).equals("TODAS LAS LÍNEAS")){

                }else{

                }
            }//onItemClick
        });//setonitemclick
    }//onCreate

    public void mensajeAggCaja(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEnvTraspMultSuc.this);
        builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                CAJAACT++;
                mostrarDetalleProd();
            }
        });
        builder.setNegativeButton("CANCELAR",null);
        builder.setCancelable(false);
        builder.setTitle("AVISO").setMessage("¿Desea agregar caja?").create().show();
    }//mensajeAggCaja

    public String folio(String folio){
        if (folio.length() < 7) {
            int fo = folio.length();
            switch (fo) {
                case 1:
                    folio = "000000" + folio;
                    break;
                case 2:
                    folio = "00000" + folio;
                    break;
                case 3:
                    folio ="0000" + folio;
                    break;
                case 4:
                    folio ="000" + folio;
                    break;
                case 5:
                    folio ="00" + folio;
                    break;
                case 6:
                    folio = "0" + folio;
                    break;
                default:
                    folio=folio;
                    break;
            }//switch
        }//if
        return folio;
    }

    public void verLista(){
        adapter = new AdaptadorEnvTraspasos(lista);
        rvEnvTrasp.setAdapter(adapter);
        txtProducto.setEnabled(true);
        txtProducto.requestFocus();
        posicion=0;
        mostrarDetalleProd();
    }
    public void mostrarDetalleProd(){//detalle por producto seleccionado
        adapter.index(posicion);
        adapter.notifyDataSetChanged();
        rvEnvTrasp.scrollToPosition(posicion);
        //Producto=listaTrasp.get(posicion).getProducto();
        tvProd.setText(lista.get(posicion).getProducto());
        txtCantidad.setText(lista.get(posicion).getCantidad());
        txtCantSurt.setText(lista.get(posicion).getCantSurt());
        txtEnv.setText(lista.get(posicion).getAlmEnv());

        if(Integer.parseInt(lista.get(posicion).getCantidad())==Integer.parseInt(lista.get(posicion).getCantSurt())){
            txtCantSurt.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }else{
            txtCantSurt.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.ColorGris)));
        }
        tvCaja.setText(CAJAACT+"");
        cambiaProd();

        Picasso.with(getApplicationContext()).
                load(urlImagenes +
                        tvProd.getText().toString() + extImg)
                .error(R.drawable.aboutlogo)
                .fit()
                .centerInside()
                .into(ivProd);
        posG=posicion;
    }//mostrarDetalleProd

    public void cambiaProd(){
        if(posicion==0 && lista.size()>1){
            btnAdelante.setEnabled(true);
            btnAdelante.setBackgroundTintList(null);
            btnAdelante.setBackgroundResource(R.drawable.btn_background3);
            btnAtras.setEnabled(false);
            btnAtras.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));

        }else if(posicion+1==lista.size() && lista.size()>1){
            btnAtras.setEnabled(true);
            btnAtras.setBackgroundTintList(null);
            btnAtras.setBackgroundResource(R.drawable.btn_background3);
            btnAdelante.setEnabled(false);
            btnAdelante.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
        }else if(lista.size()==1){
            btnAtras.setEnabled(false);
            btnAtras.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
            btnAdelante.setEnabled(false);
            btnAdelante.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
        }else{
            btnAtras.setEnabled(true);
            btnAtras.setBackgroundTintList(null);
            btnAtras.setBackgroundResource(R.drawable.btn_background3);
            btnAdelante.setEnabled(true);
            btnAdelante.setBackgroundTintList(null);
            btnAdelante.setBackgroundResource(R.drawable.btn_background3);
        }//else
    }//cambiaProd

    public void onClickListaE(View v){//cada vez que se seleccione un producto en la lista
        if(posicion>=0 && lista.get(posicion).isSincronizado()==false){
            posicion2=posG;
            Producto=lista.get(rvEnvTrasp.getChildPosition(rvEnvTrasp.findContainingItemView(v))).getProducto();
        }else{
            posicion2 = rvEnvTrasp.getChildPosition(rvEnvTrasp.findContainingItemView(v));
        }
        cambio("change",false,false);
    }//onClickLista

    public void cambio(String var,boolean sumar,boolean aggCaja){
        if(!lista.get(posicion2).getProducto().equals(Producto) && posG!=-1 && lista.get(posicion2).isSincronizado()==false){//identificando que prod anterior no se sincronizó
            new AsyncInsertCajasE(strbran,Folio,lista.get(posicion2).getProducto(),
                    lista.get(posicion2).getCantSurt(),CAJAACT+"",
                    lista.get(posicion2).getPartida(),strusr,var,sumar,Producto,aggCaja).execute();
        }else{//cuando se escanea o por botones de adelante, atras y onclick en lista
            if(sumar==true){//al escanear
                evaluarEscaneo(Producto);
            }else{
                tipoCambio(var);
                mostrarDetalleProd();
            }
        }//else
    }//alert

    public void tipoCambio(String var){
        switch (var){
            case "next":
                posicion++;break;
            case "back":
                posicion--;break;
            case "change":
                posicion=posicion2;
                posicion2=0;break;
            default:posicion=encontrarPosEnLista(var);break;
        }
    }//tipoCambio

    public int encontrarPosEnLista(String prod){
        int p=posG;
        for(int i=0;i<lista.size();i++){
            if(lista.get(i).getProducto().equals(prod)){
                p=i;
                break;
            }//if
        }
        return p;
    }

    public void evaluarEscaneo(String prod){
        limpiar();
        boolean existe=false;
        for(int i=0;i<lista.size();i++){
            if(lista.get(i).getProducto().equals(prod)){
                posicion=i;
                existe=true;
                int exi=Integer.parseInt(lista.get(i).getExistencia());
                int cant=Integer.parseInt(lista.get(i).getCantidad());
                int cantS=Integer.parseInt(lista.get(i).getCantSurt());
                if((cantS+1)<=cant){
                    if(exi<cantS){
                        Toast.makeText(this, "Existencia es menor a cantidad a surtir", Toast.LENGTH_SHORT).show();
                    }
                    cantS++;
                    lista.get(i).setCantSurt(cantS+"");
                    lista.get(i).setSincronizado(false);
                    if(cantS==cant){
                        posicion2=i;
                        new AsyncInsertCajasE(strbran,Folio,prod,
                                cantS+"",CAJAACT+"",
                                lista.get(posicion2).getPartida(),strusr,"change",false,Producto,false).execute();
                    }
                }else{
                    Toast.makeText(this, "Excede cantidad", Toast.LENGTH_SHORT).show();
                }
                break;
            }//if
        }
        if(existe==false){
            Toast.makeText(this, "No existe "+prod+" en la lista", Toast.LENGTH_SHORT).show();
        }
        mostrarDetalleProd();
    }//evaluar

    public void limpiar(){
        txtCantidad.setText("");
        txtCantSurt.setText("");
        txtEnv.setText("");
        ivProd.setImageResource(R.drawable.logokepler);
        btnAtras.setEnabled(false);
        btnAtras.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.ColorGris)));
        btnAdelante.setEnabled(false);
        btnAdelante.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.ColorGris)));
        posG=-1;
    }//limpiar


    public boolean firtMet() {//firtMet
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {//si hay conexion a internet
            return true;
        } else {
            return false;
        }//else
    }//FirtMet saber si hay conexion a internet
    public void inFinBt(boolean var){
        if(var==true){

            btnAggCaja.setEnabled(false);
            btnListaCajas.setEnabled(false);
            btnVerCajas.setEnabled(false);
            btnAggCaja.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
            btnListaCajas.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
            btnVerCajas.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
        }else{
            btnAggCaja.setEnabled(true);
            btnListaCajas.setEnabled(true);
            btnVerCajas.setEnabled(true);
            btnAggCaja.setBackgroundTintList(null);
            btnAggCaja.setBackgroundResource(R.drawable.btn_background4);

            btnListaCajas.setBackgroundTintList(null);
            btnListaCajas.setBackgroundResource(R.drawable.btn_background2);

            btnVerCajas.setBackgroundTintList(null);
            btnVerCajas.setBackgroundResource(R.drawable.btn_background1);
        }//else
    }//inFinBt

    public int obtenerCantXcaja(String prod,String caja){
        int val=0;
        for (int i=0;i<listaCajas.size();i++){
            if(listaCajas.get(i).getClavedelProdcuto().equals(prod) &&
                    listaCajas.get(i).getNumCajas().equals(caja)){
                val=Integer.parseInt(listaCajas.get(i).getCantidadUnidades());
                break;
            }//if
        }//for
        return val;
    }//obtenerCantXcaja


    private class AsyncConsulEnvTrasp extends AsyncTask<Void, Void, Void> {
        private String suc,folio;
        private boolean conn;
        public AsyncConsulEnvTrasp(String suc, String folio) {
            this.suc = suc;
            this.folio = folio;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            limpiar();
            listaLineas.clear();
            mensaje="";posicion=-1;lista.clear();
            CAJAACT=1;rvEnvTrasp.setAdapter(null);inFinBt(true);
            //cajaGuard=false;
        }//onPreExecute

        @Override
        protected Void doInBackground(Void... voids) {
            conn=firtMet();
            if(conn==true){
                HttpHandler sh = new HttpHandler();
                String parametros="k_suc="+suc+"&k_fol="+folio+"";
                String url = "http://"+strServer+"/"+getString(R.string.resConsEnvTrasp)+"?"+parametros;
                String jsonStr = sh.makeServiceCall(url,strusr,strpass);
                //Log.e(TAG, "Respuesta de la url: " + jsonStr);
                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        // Obtener array de datos
                        JSONArray jsonArray = jsonObj.getJSONArray("Response");
                        int num=1;
                        String linea="TODAS LAS LÍNEAS",linComp="";
                        listaLineas.add(linea);
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject dato = jsonArray.getJSONObject(i);//Conjunto de datos
                            linComp=dato.getString("k_linea");
                            if(!linComp.equals(linea)){
                                linea=linComp;
                                listaLineas.add(linComp);
                            }
                            lista.add(new EnvTraspasos(num+"",dato.getString("k_prod"),dato.getString("k_ubi"),
                                    dato.getString("k_exist"),dato.getString("k_cant"),dato.getString("k_part"),
                                    dato.getString("k_cants"),dato.getString("k_env"),linComp,true));

                            num++;mensaje="";
                        }//for
                    } catch (final JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mensaje="Puede que el folio no exista o no haya datos";
                            }//run
                        });
                    }//catch JSON EXCEPTION
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mensaje="No fue posible obtener datos del servidor";
                        }//run
                    });//runUniTthread
                }//else
                return null;
            }else{
                mensaje="Problemas de conexión";
                return null;
            }
        }//doInBackground

        @Override
        protected void onPostExecute(Void aBoolean) {
            super.onPostExecute(aBoolean);
            mDialog.dismiss();
            if (lista.size()==0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEnvTraspMultSuc.this);
                builder.setTitle("AVISO");
                builder.setMessage(mensaje);
                builder.setCancelable(false);
                builder.setNegativeButton("OK",null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                inFinBt(false);
                txtFolBusq.setText(Folio);
                keyboard.hideSoftInputFromWindow(txtFolBusq.getWindowToken(), 0);
                ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                        ActivityEnvTraspMultSuc.this,R.layout.drop_down_item,listaLineas);
                spLineas.setAdapter(adaptador);
                spLineas.setText(listaLineas.get(0),false);
                verLista();
            }//else
        }//onPost
    }//AsyncConsulEnvTrasp

    private class AsyncConsultCP extends AsyncTask<Void, Void, Void> {
        private String suc,folio,producto;
        private ArrayList<CajaXProd> listaCajaXProd= new ArrayList<>();
        private boolean conn;
        public AsyncConsultCP(String suc, String folio,String producto) {
            this.suc = suc;
            this.folio = folio;
            this.producto=producto;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            mensaje="";
        }//onPreExecute

        @Override
        protected Void doInBackground(Void... voids) {
            conn=firtMet();
            if(conn==true){
                HttpHandler sh = new HttpHandler();
                String parametros="k_Folio="+folio+"&k_Sucursal="+suc+"&k_Producto="+producto+"";
                String url = "http://"+strServer+"/ConsultCP?"+parametros;
                String jsonStr = sh.makeServiceCall(url,strusr,strpass);
                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        // Obtener array de datos
                        JSONArray jsonArray = jsonObj.getJSONArray("Response");
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject dato = jsonArray.getJSONObject(i);//Conjunto de datos
                            listaCajaXProd.add(new CajaXProd(dato.getString("k_NumeroCajas"),dato.getString("k_Cantidad")));
                            mensaje="";
                        }//for
                    } catch (final JSONException e) {
                        //Log.e(TAG, "Error al convertir Json: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mensaje="No se ha guardado este producto en cajas";
                            }//run
                        });
                    }//catch JSON EXCEPTION
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mensaje="Problema al consultar cajas";
                        }//run
                    });//runUniTthread
                }//else
                return null;
            }else{
                mensaje="Problemas de conexión";
                return null;
            }//else
        }//doInBackground

        @Override
        protected void onPostExecute(Void aBoolean) {
            super.onPostExecute(aBoolean);
            mDialog.dismiss();
            if (listaCajaXProd.size()==0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEnvTraspMultSuc.this);
                builder.setTitle("AVISO");
                builder.setMessage(mensaje);
                builder.setCancelable(false);
                builder.setNegativeButton("OK",null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                AlertDialog.Builder alert = new AlertDialog.Builder(ActivityEnvTraspMultSuc.this);
                LayoutInflater inflater = ActivityEnvTraspMultSuc.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_cajaxprod, null);
                alert.setView(dialogView);
                alert.setCancelable(false);
                alert.setNegativeButton("ACEPTAR",null);

                RecyclerView rvCaja =  dialogView.findViewById(R.id.rvCaja);
                GridLayoutManager gl = new GridLayoutManager(ActivityEnvTraspMultSuc.this, 1);
                rvCaja.setLayoutManager(gl);

                AdaptadorCajaxProd adap  = new AdaptadorCajaxProd(listaCajaXProd);
                rvCaja.setAdapter(adap);
                AlertDialog mm = alert.create();
                mm.show();
            }//else
        }//onPost
    }//AsyncConsultCP

    private class AsyncConsultCA extends AsyncTask<Void, Void, Void> {
        private String suc,folio;
        private boolean conn;
        public AsyncConsultCA(String suc, String folio) {
            this.suc = suc;
            this.folio = folio;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!mDialog.isShowing()){mDialog.show();}
            listaCajas.clear();
            listaCajasXProd.clear();
            mensaje="";
        }//onPreExecute

        @Override
        protected Void doInBackground(Void... voids) {
            conn=firtMet();
            if(conn==true){
                HttpHandler sh = new HttpHandler();
                String parametros="k_Folio="+folio+"&k_Sucursal="+suc+"";
                String url = "http://"+strServer+"/ConsultCA?"+parametros;
                String jsonStr = sh.makeServiceCall(url,strusr,strpass);
                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        JSONArray jsonArray = jsonObj.getJSONArray("Response");
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject dato = jsonArray.getJSONObject(i);//Conjunto de datos
                            listaCajas.add(new CAJASSANDG("","","",
                                    dato.getString("k_Producto"),dato.getString("k_Cantidad"),
                                    dato.getString("k_NumeroCajas")));
                        }//for
                    } catch (final JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mensaje="No hay lista de cajas guardadas";
                            }//run
                        });
                    }//catch JSON EXCEPTION
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mensaje="Problema al consultar lista de cajas";
                        }//run
                    });//runUniTthread
                }//else
                return null;
            }else{
                mensaje="Problemas de conexión";
                return null;
            }//else
        }//doInBackground

        @Override
        protected void onPostExecute(Void aBoolean) {
            super.onPostExecute(aBoolean);
            mDialog.dismiss();
            if (listaCajas.size()==0) {
                mDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEnvTraspMultSuc.this);
                builder.setTitle("AVISO");
                builder.setMessage(mensaje);
                builder.setCancelable(false);
                builder.setNegativeButton("OK",null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                //MOSTAR LISTA DE PRODUCTOS CON CAJAS
                mostrarEnAlertListaCajas();
            }//else
        }//onPost
    }//AsyncConsultCA

    private class AsyncCambiarCajas extends AsyncTask<Void, Void, Void> {

        private String suc, folio,producto,cantidad,caja1,caja2;
        private AlertDialog alert1,alert2;
        private boolean conn;
        public AsyncCambiarCajas(String suc, String folio,String producto,
                                 String cantidad,String caja1,String caja2,
                                 AlertDialog alert1,AlertDialog alert2) {
            this.suc = suc;
            this.folio = folio;
            this.producto=producto;
            this.cantidad=cantidad;
            this.caja1=caja1;
            this.caja2=caja2;
            this.alert1 = alert1;
            this.alert2 = alert2;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }//onPreExecute

        @Override
        protected Void doInBackground(Void... voids) {
            conn=firtMet();
            if(conn==true){
                HttpHandler sh = new HttpHandler();
                String parametros = "sucursal=" + suc + "&folio=" + folio + "&producto=" +
                        producto +  "&cantidad=" + cantidad + "&numCaja1=" + caja1 +"&numCaja2=" + caja2+"";
                String url = "http://" + strServer + "/CambiarPC?" + parametros;
                String jsonStr = sh.makeServiceCall(url, strusr, strpass);
                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        JSONArray jsonArray = jsonObj.getJSONArray("Response");
                        mensaje=jsonArray.getString(0);

                    } catch (final JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mensaje="Problemas al cambiar caja";
                            }//run
                        });
                    }//catch JSON EXCEPTION
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mensaje="Problemas con el servidor";
                        }//run
                    });//runUniTthread
                }//else
                return null;
            }else{
                mensaje="Problemas de conexión";
                return null;
            }//else
        }//doInBackground

        @Override
        protected void onPostExecute(Void aBoolean) {
            super.onPostExecute(aBoolean);
            mDialog.dismiss();
            if(mensaje.equals("Registro Exitoso") || mensaje.equals("Actualizacion Exitosa")){
                alert1.dismiss();
                alert2.dismiss();
                new AsyncConsultCA(strbran,Folio).execute();
            }else{
                Toast.makeText(ActivityEnvTraspMultSuc.this, mensaje, Toast.LENGTH_SHORT).show();
            }//else
        }//onPost
    }//CambiarCajas

    private class AsyncInsertCajasE extends AsyncTask<Void, Void, Void> {

        private String suc,folio,producto,cant,numCajas,part,usu,var,ProductoActual;
        private boolean conn,sumar,aggCaj;

        public AsyncInsertCajasE(String suc, String folio, String producto, String cant,
                                 String numCajas, String part, String usu,String var,
                                 boolean sumar,String ProductoActual,boolean aggCaj) {
            this.suc = suc;
            this.folio = folio;
            this.producto = producto;
            this.cant = cant;
            this.numCajas = numCajas;
            this.part = part;
            this.usu = usu;
            this.var=var;
            this.sumar=sumar;
            this.ProductoActual=ProductoActual;
            this.aggCaj =aggCaj;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            mensaje="";
        }//onPreExecute

        @Override
        protected Void doInBackground(Void... voids) {
            conn=firtMet();
            if(conn==true){
                String parametros="k_Sucursal="+suc+"&k_Folio="+folio+
                        "&k_Producto="+producto+"&k_Cantidad="+cant+
                        "&k_NumCajas="+numCajas+"&k_partida="+part+""+"&k_UUsuario="+usu;
                String url = "http://"+strServer+"/InsertCajasE?"+parametros;
                String jsonStr = new HttpHandler().makeServiceCall(url,strusr,strpass);
                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        JSONArray jsonArray = jsonObj.getJSONArray("Response");
                        mensaje=jsonArray.getString(0);
                    } catch (final JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mensaje="No hay lista de cajas guardadas";
                            }//run
                        });
                    }//catch JSON EXCEPTION
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mensaje="Problema al consultar lista de cajas";
                        }//run
                    });//runUniTthread
                }//else
                return null;
            }else{
                mensaje="Problemas de conexión";
                return null;
            }//else
        }//doInBackground

        @Override
        protected void onPostExecute(Void aBoolean) {
            super.onPostExecute(aBoolean);
            mDialog.dismiss();
            if(conn==true && mensaje.equals("Insertado Exitosa") || mensaje.equals("Actualizacion Exitosa")) {
                //cajaGuard=true;
                Toast.makeText(ActivityEnvTraspMultSuc.this, "Sincronizado", Toast.LENGTH_SHORT).show();
                bepp.play(sonido_correcto, 1, 1, 1, 0, 0);
                lista.get(posicion2).setSincronizado(true);
                if(sumar==true){
                    evaluarEscaneo(ProductoActual);
                }else{
                    tipoCambio(var);
                    mostrarDetalleProd();
                }
                //CUANDO SE PREIONE AGREGAR CAJA SE SINCRONIZARÁ Y LUEGO MANDARÁ MENSAJE PARA CAMBIAR CAJA
                if(aggCaj==true){
                    mensajeAggCaja();
                }
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityEnvTraspMultSuc.this);
                builder.setTitle("AVISO");
                builder.setMessage(mensaje);
                builder.setCancelable(false);
                builder.setNegativeButton("OK",null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }//else
        }//onPost
    }//AsyncInsertCajasE

    public void mostrarEnAlertListaCajas(){
        AlertDialog.Builder alert = new AlertDialog.Builder(ActivityEnvTraspMultSuc.this);
        LayoutInflater inflater = ActivityEnvTraspMultSuc.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_lista_cajas, null);
        alert.setView(dialogView);
        alert.setCancelable(false);
        alert.setNegativeButton("CANCELAR",null);

        TextView tvCajaNom = dialogView.findViewById(R.id.tvCajaNom);
        rvListaCajas =  dialogView.findViewById(R.id.rvListaCajas);
        Button btnBack = dialogView.findViewById(R.id.btnBack);
        Button btnNext = dialogView.findViewById(R.id.btnNext);
        Button btnCambiar = dialogView.findViewById(R.id.btnCambiar);
        GridLayoutManager gl = new GridLayoutManager(ActivityEnvTraspMultSuc.this, 1);
        rvListaCajas.setLayoutManager(gl);
        AlertDialog mm1 = alert.create();

        //INICIA
        posCaja =0;
        prodSelectCaj="";
        cantCajaOr=0;

        //CREAR LISTA DE CAJAS
        nomCajas.clear();
        cajaActAl=listaCajas.get(0).getNumCajas();
        nomCajas.add(cajaActAl);
        for(int i=0;i<listaCajas.size();i++){
            if(!cajaActAl.equals(listaCajas.get(i).getNumCajas())){
                nomCajas.add(listaCajas.get(i).getNumCajas());
                cajaActAl=listaCajas.get(i).getNumCajas();
            }//if
        }//for
        listaCajasXProd=listaXCaja(cajaActAl);
        pC=0;//para contador de listaCajasXProd
        prodSelectCaj=listaCajasXProd.get(0).getClavedelProdcuto();//se situa en el primer producto asi que le asigno valores
        cantCajaOr=Integer.parseInt(listaCajasXProd.get(0).getCantidadUnidades());

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaCajasXProd.clear();
                pC--;
                cajaActAl =nomCajas.get(pC);
                tvCajaNom.setText(cajaActAl);
                tablaXcaja(btnBack,btnNext,pC);
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listaCajasXProd.clear();
                pC++;
                cajaActAl =nomCajas.get(pC);
                tvCajaNom.setText(cajaActAl);
                cajaActAl =nomCajas.get(pC);
                tvCajaNom.setText(cajaActAl);
                tablaXcaja(btnBack,btnNext,pC);
            }
        });
        btnCambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiarCajasAlert(prodSelectCaj,cantCajaOr, cajaActAl,nomCajas,mm1);
            }
        });//btnCambiar
        cajaActAl =nomCajas.get(pC);
        tvCajaNom.setText(cajaActAl);
        tablaXcaja(btnBack,btnNext,posCaja);
        mm1.show();
    }//mostrarListaCajas

    public ArrayList<CAJASSANDG> listaXCaja(String cajaSelect){
        ArrayList<CAJASSANDG> lista= new ArrayList<>();
        for(int j=0;j<listaCajas.size();j++){
            if(listaCajas.get(j).getNumCajas().equals(cajaSelect)){
                lista.add(new CAJASSANDG("","","",
                        listaCajas.get(j).getClavedelProdcuto(),
                        listaCajas.get(j).getCantidadUnidades(),listaCajas.get(j).getNumCajas()));
            }//if
        }//for
        return lista;
    }//lista


    public void tablaXcaja (Button back, Button next,int posActCaja){
        if(posActCaja==0 && nomCajas.size()>1){
            next.setEnabled(true);
            next.setBackgroundTintList(null);
            next.setBackgroundResource(R.drawable.btn_background3);
            back.setEnabled(false);
            back.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
        }else if(nomCajas.size()==1){
            back.setEnabled(false);
            back.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
            next.setEnabled(false);
            next.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
        } else if((posActCaja+1)==nomCajas.size() && nomCajas.size()>1){
            back.setEnabled(true);
            back.setBackgroundTintList(null);
            back.setBackgroundResource(R.drawable.btn_background1);
            next.setEnabled(false);
            next.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
        }else{
            next.setEnabled(true);
            next.setBackgroundTintList(null);
            next.setBackgroundResource(R.drawable.btn_background1);
            back.setEnabled(false);
            back.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
        }//else
        rvListaCajas.setAdapter(null);
        listaCajasXProd.clear();
        listaCajasXProd =listaXCaja(cajaActAl);
        adapListCaj= new AdapterListaCajas(listaCajasXProd);
        rvListaCajas.setAdapter(adapListCaj);
        posCaja=0;
        prodSelectCaj=listaCajasXProd.get(posCaja).getClavedelProdcuto();
        cantCajaOr=Integer.parseInt(listaCajasXProd.get(posCaja).getCantidadUnidades());
    }//cambiaCaja

    public void onClickEnListaCaja(View v){
        posCaja = rvListaCajas.getChildPosition(rvListaCajas.findContainingItemView(v));
        prodSelectCaj=listaCajasXProd.get(posCaja).getClavedelProdcuto();
        cantCajaOr=Integer.parseInt(listaCajasXProd.get(posCaja).getCantidadUnidades());
        adapListCaj.index(posCaja);
        adapListCaj.notifyDataSetChanged();
    }//onclickEnListaCaja

    public void cambiarCajasAlert(String prod,int cantEnCaja,String origen,ArrayList<String> nomCajas,
                                  AlertDialog alert1){
        AlertDialog.Builder alert = new AlertDialog.Builder(ActivityEnvTraspMultSuc.this);
        LayoutInflater inflater = ActivityEnvTraspMultSuc.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_info_cajascambio, null);
        alert.setView(dialogView);
        alert.setCancelable(false);
        alert.setNegativeButton("CANCELAR",null);

        Button btncambiar =  dialogView.findViewById(R.id.btnCambiar);
        EditText txtCajaProd = dialogView.findViewById(R.id.txtCajaProd);
        EditText txtCajaOrigen =dialogView.findViewById(R.id.txtCajaOrigen);
        EditText txtCajaCant = dialogView.findViewById(R.id.txtCajaCant);
        EditText txtCajaDestino =  dialogView.findViewById(R.id.txtCajaDestino);
        EditText txtCantidad =  dialogView.findViewById(R.id.txtCantidad);
        LinearLayout contP = dialogView.findViewById(R.id.contP);

        contP.setVisibility(View.VISIBLE);
        txtCajaOrigen.setText(origen);
        txtCajaOrigen.setEnabled(false);
        txtCajaProd.setText(prod);
        txtCajaCant.setText(cantEnCaja+"");
        AlertDialog alert2 = alert.create();

        ArrayList<String> nomCajas2=new ArrayList<>();
        for(int k=0;k<nomCajas.size();k++){
            if(!nomCajas.get(k).equals(origen)){
                nomCajas2.add(nomCajas.get(k));
            }
        }//for


        btncambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Caja1ori= txtCajaOrigen.getText().toString();
                String Caja2des=txtCajaDestino.getText().toString();
                String cantidapro=txtCantidad.getText().toString();
                if(Caja1ori.equals(Caja2des)){
                    Toast.makeText(ActivityEnvTraspMultSuc.this, "Caja de origen igual a caja destino", Toast.LENGTH_SHORT).show();
                }else if (cantidapro.equals("") || Integer.parseInt(cantidapro)==0 || Caja2des.equals("")){
                    Toast.makeText(ActivityEnvTraspMultSuc.this,
                            "Campos vacios o en 0", Toast.LENGTH_SHORT).show();
                }else if(Integer.parseInt(cantidapro)>cantEnCaja){
                    Toast.makeText(ActivityEnvTraspMultSuc.this, "Excede cantidad de caja origen", Toast.LENGTH_SHORT).show();
                }else{
                    new AsyncCambiarCajas(strbran,Folio,prod,cantidapro,Caja1ori,Caja2des,alert1,alert2).execute();
                }//else
            }//onclick
        });

        alert2.show();
    }//cambiarCajas

}//Activity
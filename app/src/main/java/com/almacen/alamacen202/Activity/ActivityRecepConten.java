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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.almacen.alamacen202.Adapter.AdaptadorRecepConten;
import com.almacen.alamacen202.Adapter.AdaptadorTraspasos;
import com.almacen.alamacen202.Adapter.AdapterDifUbiExi;
import com.almacen.alamacen202.Adapter.AdapterInventario;
import com.almacen.alamacen202.MainActivity;
import com.almacen.alamacen202.R;
import com.almacen.alamacen202.SetterandGetters.Inventario;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import dmax.dialog.SpotsDialog;

public class ActivityRecepConten extends AppCompatActivity {
    private SharedPreferences preference,preference2;
    private SharedPreferences.Editor editor;
    private boolean revisa=false,seleccion=false,tipoBusq=false;
    private float existMtz=0,existCdmx=0,existCul=0,existMty=0,existRep=0,repMtz=0,repCdmx=0,repCul=0,repMty=0,repAct=0;
    private float faltMtz=0,faltCdmx=0,faltCul=0,faltMty=0;
    private float demBMtz=0,demBCdmx=0,demBCul=0,demBMty=0;
    private int posicion=0,escaneados=0;
    private ArrayAdapter<String> adapterLv;

    private String strusr,strpass,strbran,strServer,codeBar,mensaje,producto="",clasf="";
    private String sucursalSelec="",tipoEscan="",folioAct="",folio1,folio2,folio3,sqlW="",ff="";
    private String[] folios;
    private ArrayList<RecepConten> listaRecep = new ArrayList<>();
    private ArrayList<RecepListSucCont> listaSucRecep = new ArrayList<>();
    private LinearLayout lyMatrz,lyCdmx,lyCul,lyMty;
    private ListView lvFolios;
    private EditText txtProdR,txtSuc,txtRest,txtEscan,txtProdVi;
    private ImageView ivProdR;
    private TextView tvRepMatr,tvRepCdmx,tvRepCul,tvRepMty;
    private Button btnBuscaFolio,btnAtras,btnAdelante,btnSave,btnAct;
    private CheckBox chbManual,chBxProd,chBpalet;
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

        preference2= getSharedPreferences("FoliosGuarda", Context.MODE_PRIVATE);//para guardar folio
        editor = preference2.edit();
        folio1=preference2.getString("folio1","");
        folio2=preference2.getString("folio2","");
        folio3=preference2.getString("folio3","");

        urlImagenes=preference.getString("urlImagenes", "null");
        extImg=preference.getString("ext", "null");

        mDialog = new SpotsDialog.Builder().setContext(ActivityRecepConten.this).
                setMessage("Espere un momento...").build();
        mDialog.setCancelable(false);

        lvFolios        = findViewById(R.id.lvFolios);
        lyMatrz         = findViewById(R.id.lyMatrz);
        lyCdmx          = findViewById(R.id.lyCdmx);
        lyCul           = findViewById(R.id.lyCul);
        lyMty           = findViewById(R.id.lyMty);
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
        chBxProd        = findViewById(R.id.chBxProd);
        chBpalet        = findViewById(R.id.chBpalet);


        btnAtras    = findViewById(R.id.btnBack);
        btnAdelante =findViewById(R.id.btnNext);
        btnAct      =findViewById(R.id.btnAct);

        conn = new ConexionSQLiteHelper(ActivityRecepConten.this, "bd_INVENTARIO", null, 1);
        db = conn.getReadableDatabase();//apertura de la base de datos interna
        rvRecep    = findViewById(R.id.rvRecep);
        rvRecep.setLayoutManager(new LinearLayoutManager(ActivityRecepConten.this));
        adapter = new AdaptadorRecepConten(listaRecep);
        keyboard = (InputMethodManager) getSystemService(ActivityRecepConten.INPUT_METHOD_SERVICE);


        chBxProd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b==true){
                    chBpalet.setChecked(false);
                }
            }//onckeckedchange
        });
        chBpalet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b==true){
                    chBxProd.setChecked(false);
                }
            }//oncheckedchange
        });//chBpalet
        lvFolios.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.setSelected(true);
                txtProdR.setText("");
                posicion=-1;
                limpiarCampos();
                if(i>0){//cuando hay folio seleccionado
                    folioAct=folios[i];
                    ff="";
                    sqlW="SELECT R.PRODUCTO,SUM(R.CANTIDAD),R.PRIORIDAD,SUM(R.ESCANMTRZ),SUM(R.ESCANCDMX),"+
                            "SUM(R.ESCANCUL),SUM(R.ESCANMTY),GROUP_CONCAT(P.NAMEPALET,',') FROM RECEPCONT R " +
                            "LEFT JOIN(SELECT PRODUCTO,NAMEPALET FROM PALET WHERE " +
                            "FOLIO='"+folioAct+"' GROUP BY PRODUCTO,NAMEPALET) P ON(P.PRODUCTO=R.PRODUCTO) " +
                            "WHERE R.FOLIO='"+folioAct+"' GROUP BY R.PRODUCTO,R.PRIORIDAD ORDER BY R.PRODUCTO ";
                    tipoBusq=true;
                    consultaSql(sqlW,ff);
                }else{//cuando es todos los folios
                    folioAct="";
                    ff="FOLIO";
                    sqlW="SELECT R.PRODUCTO,R.CANTIDAD,R.PRIORIDAD,R.ESCANMTRZ,R.ESCANCDMX,"+
                            "R.ESCANCUL,R.ESCANMTY,(SELECT GROUP_CONCAT(NAMEPALET) FROM PALET WHERE PRODUCTO=R.PRODUCTO GROUP BY NAMEPALET),R.FOLIO FROM RECEPCONT R " +
                            "LEFT JOIN(SELECT PRODUCTO,NAMEPALET FROM PALET " +
                            "GROUP BY PRODUCTO,NAMEPALET) P ON(P.PRODUCTO=R.PRODUCTO) " +
                            "ORDER BY R.PRODUCTO ";
                    tipoBusq=false;
                    consultaSql(sqlW,ff);
                }//else
            }
        });




        chbManual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                txtProdR.setText("");
                txtProdVi.setText("");
                posicion=-1;
                consultaSql(sqlW,ff);
                if(b==true){
                    txtProdR.requestFocus();
                    txtEscan.setEnabled(true);
                    btnSave.setEnabled(false);
                    btnSave.setBackgroundTintList(ColorStateList.
                            valueOf(getResources().getColor(R.color.ColorGris)));
                }else{
                    txtEscan.setEnabled(false);
                    btnSave.setEnabled(false);
                    btnSave.setBackgroundTintList(ColorStateList.
                            valueOf(getResources().getColor(R.color.ColorGris)));
                    txtProdR.requestFocus();
                }
            }//oncheckedchange
        });//chbManual
        txtEscan.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(chbManual.isChecked()){
                    if(editable.toString().equals("") || txtProdVi.getText().equals("") /*|| escaneados>0 && repAct==escaneados*/){
                        btnSave.setEnabled(false);
                        btnSave.setBackgroundTintList(ColorStateList.
                                valueOf(getResources().getColor(R.color.ColorGris)));
                    }else{
                        btnSave.setEnabled(true);
                        btnSave.setBackgroundTintList(ColorStateList.
                                valueOf(getResources().getColor(R.color.colorPrimary)));
                    }//else
                }//if
            }
        });//txtEscan change

        btnAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!folio1.equals("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepConten.this);
                    builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new AsyncRecepCon(folio1,folio2,folio3).execute();
                        }
                    });
                    builder.setCancelable(false);
                    builder.setTitle("AVISO").setMessage("Volver a hacer consulta con folios actuales").create().show();
                }
            }//onclick
        });//btnAct

        btnBuscaFolio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncRecepFolios().execute();
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
                            txtEscan.setEnabled(true);
                            txtEscan.requestFocus();
                            keyboard.showSoftInput(txtEscan, InputMethodManager.SHOW_IMPLICIT);
                        }else{
                            buscarEnSql(tipoBusq,folioAct,producto,tipoEscan,0,false,1);
                            txtProdR.setText("");
                        }//else
                    }else{
                        for (int i = 0; i < editable.length(); i++) {
                            char ban;
                            ban = editable.charAt(i);
                            if (ban == '\n') {
                                if(chbManual.isChecked()){//si esta en modo manual
                                    txtEscan.setEnabled(true);
                                    txtEscan.requestFocus();
                                    keyboard.showSoftInput(txtEscan, InputMethodManager.SHOW_IMPLICIT);
                                }else{
                                    buscarEnSql(tipoBusq,folioAct,producto,tipoEscan,0,false,1);
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
                if(!txtEscan.getText().equals("") && !txtProdVi.getText().equals("")){
                    int c=Integer.parseInt(txtEscan.getText().toString());
                    buscarEnSql(tipoBusq,folioAct,producto,tipoEscan,c,true,0);
                    keyboard.hideSoftInputFromWindow(txtEscan.getWindowToken(), 0);
                    if(revisa==false){//cuando se realizo el cambio correctamente
                        limpiarCampos();
                        mostrarDatosProd();
                        txtProdR.requestFocus();
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

        if(!folio1.equals("")){
            int tam=2;
            if(!folio3.equals("")){
                tam=4;
            }if(!folio2.equals("")){
                tam=3;
            }//else if
            folios= new String[tam];
            folios[0]="--Todos--";
            folios[1]=folio1;
            if(!folio2.equals("")){
                folios[2]=folio2;
            }else if(!folio3.equals("")){
                folios[3]=folio3;
            }//else
            listaFolios();
            consultaSql(sqlW,ff);
        }
    }//onCreate

    public void listaFolios(){
        adapterLv = new ArrayAdapter<String>(ActivityRecepConten.this, android.R.layout.simple_list_item_1,folios);
        lvFolios.setAdapter(adapterLv);
        lvFolios.requestFocusFromTouch();
        lvFolios.performItemClick(lvFolios, 0, 0);
    }//listaFolios

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

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

    @SuppressLint("ResourceAsColor")
    public void seleccionaSuc(){
        tipoEscan="";
        if(!producto.equals("")){
            switch(sucursalSelec){
                case "Matriz":
                    tipoEscan="ESCANMTRZ";
                    repAct=repMtz;
                    escaneados=Integer.parseInt(listaRecep.get(posicion).getEscanMtrz());
                    tvRepMatr.setBackgroundResource(R.color.ColorGris);
                    tvRepCdmx.setBackgroundResource(R.drawable.drawable_border);
                    tvRepCul.setBackgroundResource(R.drawable.drawable_border);
                    tvRepMty.setBackgroundResource(R.drawable.drawable_border);
                    break;
                case "CDMX.":
                    tipoEscan="ESCANCDMX";
                    repAct=repCdmx;
                    escaneados=Integer.parseInt(listaRecep.get(posicion).getEscanCdmx());
                    tvRepCdmx.setBackgroundResource(R.color.ColorGris);
                    tvRepMatr.setBackgroundResource(R.drawable.drawable_border);
                    tvRepCul.setBackgroundResource(R.drawable.drawable_border);
                    tvRepMty.setBackgroundResource(R.drawable.drawable_border);
                    break;
                case "Culiacan":
                    tipoEscan="ESCANCUL";
                    repAct=repCul;
                    escaneados=Integer.parseInt(listaRecep.get(posicion).getEscanCul());
                    tvRepCul.setBackgroundResource(R.color.ColorGris);
                    tvRepMatr.setBackgroundResource(R.drawable.drawable_border);
                    tvRepCdmx.setBackgroundResource(R.drawable.drawable_border);
                    tvRepMty.setBackgroundResource(R.drawable.drawable_border);
                    break;
                case "Monterrey":
                    tipoEscan="ESCANMTY";
                    repAct=repMty;
                    escaneados=Integer.parseInt(listaRecep.get(posicion).getEscanMty());
                    tvRepMty.setBackgroundResource(R.color.ColorGris);
                    tvRepMatr.setBackgroundResource(R.drawable.drawable_border);
                    tvRepCdmx.setBackgroundResource(R.drawable.drawable_border);
                    tvRepCul.setBackgroundResource(R.drawable.drawable_border);
                    break;
            }//switch
            txtSuc.setText(sucursalSelec);
            txtEscan.setText(escaneados+"");
            int totalEsc=Integer.parseInt(listaRecep.get(posicion).getEscanMtrz())+
                    Integer.parseInt(listaRecep.get(posicion).getEscanCdmx())+
                    Integer.parseInt(listaRecep.get(posicion).getEscanCul())+
                    Integer.parseInt(listaRecep.get(posicion).getEscanMty());
            txtRest.setText((Integer.parseInt(listaRecep.get(posicion).getCantidad())-totalEsc)+"");
            if(Integer.parseInt(txtRest.getText().toString())==0){
                txtRest.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.ColorFinalizado)));
            }//if
        }//si hay producto seleccionado
        else{
            limpiarCampos();
        }//else
    }//seleccionaSuc

    public void limpiarCampos(){
        escaneados=0;
        repMtz=0;repCdmx=0;repCul=0;repMty=0;
        existMtz=0;existCdmx=0;existCul=0;existMty=0;existRep=0;repMtz=0;repCdmx=0;repCul=0;repMty=0;
        faltMtz=0;faltCdmx=0;faltCul=0;faltMty=0;
        demBMtz=0;demBCdmx=0;demBCul=0;demBMty=0;
        txtProdVi.setText("");
        txtSuc.setText("");
        txtRest.setText("0");
        txtEscan.setText("0");
        tvRepMatr.setText("0");
        tvRepCdmx.setText("0");
        tvRepCul.setText("0");
        tvRepMty.setText("0");
        tvRepMatr.setTextColor(Color.BLACK);
        tvRepCdmx.setTextColor(Color.BLACK);
        tvRepCul.setTextColor(Color.BLACK);
        tvRepMty.setTextColor(Color.BLACK);
        txtRest.setTextColor(Color.BLACK);
        tvRepMatr.setBackgroundResource(R.drawable.drawable_border);
        tvRepCdmx.setBackgroundResource(R.drawable.drawable_border);
        tvRepCul.setBackgroundResource(R.drawable.drawable_border);
        tvRepMty.setBackgroundResource(R.drawable.drawable_border);
        ivProdR.setImageResource(R.drawable.logokepler);
    }//limpiarCampos

    public void onClickListaR(View v){//cada vez que se seleccione un producto en la lista
        posicion = rvRecep.getChildPosition(rvRecep.findContainingItemView(v));
        seleccionaProd();
    }//onClickLista

    public void seleccionaProd(){
        if(!txtProdVi.getText().equals("")){
            String f=listaRecep.get(posicion).getFolio();
            producto=listaRecep.get(posicion).getProducto();
            adapter.index(posicion);
            adapter.notifyDataSetChanged();
            rvRecep.scrollToPosition(posicion);
            if(f.equals("")){
                f=folioAct;
            }
            new AsyncRecepXProd(f,producto).execute();
        }
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
        seleccionaSuc();
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

    @SuppressLint("ResourceAsColor")
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

        if(Integer.parseInt(listaRecep.get(posicion).getEscanMtrz())==repMtz){
            tvRepMatr.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.ColorFinalizado)));
        }if(Integer.parseInt(listaRecep.get(posicion).getEscanCdmx())==repCdmx){
            tvRepCdmx.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.ColorFinalizado)));
        }if(Integer.parseInt(listaRecep.get(posicion).getEscanCul())==repCul){
            tvRepCul.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.ColorFinalizado)));
        }if(Integer.parseInt(listaRecep.get(posicion).getEscanMty())==repMty){
            tvRepMty.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.ColorFinalizado)));
        }//
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
            demXClasf=Float.parseFloat(listaSucRecep.get(i).getDem());
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
    }//asignarReparticiones

    public void mostrarLista(){
        adapter= new AdaptadorRecepConten(listaRecep);
        rvRecep.setAdapter(adapter);
        adapter.index(posicion);
        adapter.notifyDataSetChanged();
        rvRecep.scrollToPosition(posicion);
    }//mostrar lista

    public void consultaSql(String sql,String folio){
        try{
            listaRecep.clear();
            rvRecep.setAdapter(null);
            int j=0;
            String f;
            @SuppressLint("Recycle") Cursor fila = db.rawQuery(sql, null);
            if (fila != null && fila.moveToFirst()) {
                int n=fila.getColumnCount();
                if(n==9){
                    f=fila.getString(8);
                }else{
                    f="";
                }
                do {
                    j++;
                    if(producto.equals(fila.getString(0))){
                        posicion=j-1;
                    }
                    listaRecep.add(new RecepConten(j+"",fila.getString(0),fila.getString(1),fila.getString(2),fila.getString(3),
                            fila.getString(4),fila.getString(5),fila.getString(6),f,fila.getString(7)));
                } while (fila.moveToNext());
                mostrarLista();
            }//if
            fila.close();
        }catch(Exception e){
            Toast.makeText(ActivityRecepConten.this,
                    "Error al consultar datos de la base de datos interna", Toast.LENGTH_SHORT).show();
        }//catch
    }//consultaSql

    public void buscarEnSql(boolean tipoBusq,String folio,String prod,String tipoEscan,int cantidad,boolean sumarOno,int sum){
        try{
            String sqlB,escan,cant,sumEscan;
            revisa=false;
            if(tipoBusq==true){//cuando es por folio
                sqlB="SELECT "+tipoEscan+",CANTIDAD,(ESCANMTRZ+ESCANCDMX+ESCANCUL+ESCANMTY) FROM RECEPCONT WHERE PRODUCTO='"+prod+"' AND FOLIO='"+folio+"'";
            }else{//cuando no es por folio
                sqlB="SELECT SUM("+tipoEscan+"),SUM(CANTIDAD),SUM(ESCANMTRZ+ESCANCDMX+ESCANCUL+ESCANMTY) FROM RECEPCONT WHERE PRODUCTO='"+prod+"' ";
            }//else
            @SuppressLint("Recycle") Cursor fila = db.rawQuery(sqlB, null);
            if (fila != null && fila.moveToFirst()) {
                escan=fila.getString(0);cant=fila.getString(1);
                sumEscan=fila.getString(2);
                if(sumarOno==true){//determina si se modifica o se suma
                    escaneados=cantidad;
                }else{
                    escaneados=Integer.parseInt(escan)+sum;
                }//else
                //Para que no sobrepase la cantidad a repartir y las cantidades
                int restN=(Integer.parseInt(cant)-(Integer.parseInt(sumEscan)-Integer.parseInt(escan)));
                if(escaneados<=repAct && escaneados<=restN){
                    actualizarSql(folio,prod,tipoEscan,escaneados+"");
                }else{
                    revisa=true;
                    escaneados=Integer.parseInt(escan);
                    Toast.makeText(this, "Excede cantidad a repartir", Toast.LENGTH_SHORT).show();
                }//else
            }//if
            fila.close();
        }catch(Exception e){
            Toast.makeText(ActivityRecepConten.this,"Error al consultar producto", Toast.LENGTH_SHORT).show();
        }//catch
        consultaSql(sqlW,folio);
    }//consultaSql

    public boolean insertarSqlPalet(String folio,String prod,String palet){
        boolean res=false;
            try {
                ContentValues valores;
                String[] paltCu = palet.split(",");
                for(int i=0;i< paltCu.length;i++){
                    valores = new ContentValues();
                    valores.put("FOLIO", folio);
                    valores.put("PRODUCTO", prod);
                    valores.put("NAMEPALET", paltCu[i]);
                    db.insertOrThrow("PALET",null,valores);
                    res=true;
                }//for
            }catch (SQLException sqlException){
                sqlException=sqlException;
            }catch (Exception e){
        }
        return res;
    }//insertarSqlPalet


    public boolean insertarSql(String folio,String prod,String cant,String prior,String escanMtrz,String escanCdmx,String escanCul,String escanMty,String palet){
        boolean res=false;
        try{
            ContentValues valores = new ContentValues();
            valores.put("FOLIO", folio);
            valores.put("PRODUCTO", prod);
            valores.put("CANTIDAD", cant);
            valores.put("PRIORIDAD", prior);
            //valores.put("PALET", palet);
            valores.put("ESCANMTRZ", escanMtrz);
            valores.put("ESCANCDMX", escanCdmx);
            valores.put("ESCANCUL", escanCul);
            valores.put("ESCANMTY", escanMty);
            db.insertOrThrow("RECEPCONT", null, valores);
            res=true;
        }catch(SQLException sqlException){
            sqlException=sqlException;
        } catch(Exception e){
        }
        return res;
    }//insertarSql

    public boolean actualizarSql(String folio,String prod,String tipoEscaner,String cantEscaner){
        boolean res=false;
        try{
            ContentValues valores = new ContentValues();
            valores.put(tipoEscaner, cantEscaner);
            db.update("RECEPCONT", valores, "FOLIO='"+folio+"' AND PRODUCTO='"+prod+"'", null);
            res=true;
        }catch (SQLException sqlException){} catch(Exception e){}
        return res;
    }//actualizarSql

    public boolean eliminarSql(String where) {
        //"FOLIO='"+folio+"' AND PRODUCTO='"+prod+"'"
        boolean res=false;
        try{
            SQLiteDatabase db = conn.getWritableDatabase();
            db.delete("RECEPCONT",where,null);
            db.delete("PALET",null,null);
            res=true;
        }catch(SQLException sqlException){}catch(Exception e){}
        return res;
    }//eliminarSql


    private class AsyncRecepCon extends AsyncTask<Void, Void, Void> {

        private String folio1,folio2,folio3;

        public AsyncRecepCon(String folio1, String folio2, String folio3) {
            this.folio1 = folio1;
            this.folio2 = folio2;
            this.folio3 = folio3;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            mensaje="";
            editor.clear().commit();
            posicion=-1;
            listaRecep.clear();
            rvRecep.setAdapter(null);
            limpiarCampos();
            eliminarSql("");
        }//onPreExecute

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            String parametros="k_folio1="+folio1+"&k_folio2="+folio2+"&k_folio3="+folio3+"";
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
                        insertarSql(dato.getString("k_fol"),dato.getString("k_prod"),dato.getString("k_cant"),dato.getString("k_prio"),
                                "0","0","0","0",dato.getString("k_paletCaja"));
                        insertarSqlPalet(dato.getString("k_fol"),dato.getString("k_prod"),dato.getString("k_paletCaja"));
                        listaRecep.add(new RecepConten((i+1)+"",dato.getString("k_prod"),dato.getString("k_cant"),
                                dato.getString("k_prio"),"0","0","0","0",
                                dato.getString("k_fol"),dato.getString("k_paletCaja")));
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
        protected void onPostExecute(Void aBoolean) {
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
                editor.putString("folio1", folio1);
                editor.putString("folio2", folio2);
                editor.putString("folio3", folio3);
                editor.commit();
                posicion=-1;
                //mostrarLista();
                consultaSql(sqlW,"FOLIO");
                txtProdR.requestFocus();
                txtProdR.setInputType(InputType.TYPE_NULL);
                btnAtras.setEnabled(true);
                btnAdelante.setEnabled(true);
            }//else
        }//onPost
    }//AsyncRecepCon

    private class AsyncRecepXProd extends AsyncTask<Void, Boolean, Boolean> {
        private String folio;
        private String producto;

        public AsyncRecepXProd(String folio,String producto) {
            this.folio=folio;
            this.producto=producto;
        }

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
                }catch (final JSONException e) {
                    //Log.e(TAG, "Error al convertir Json: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mensaje="No se pudieron traer datos de este producto";
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
                dialog.show();
                txtProdVi.setText("");
            }else{
                mostrarDatosProd();
            }//else
        }//onPost
    }//AsyncRecepCon

    private class AsyncRecepFolios extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            mensaje="";
            folios = new String[12];
            limpiarCampos();
        }//onPreExecute

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();//separar párametros con &
            String url = "http://"+strServer+"/"+getString(R.string.resRecepFolios)+"";
            String jsonStr = sh.makeServiceCall(url,strusr,strpass);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    // Obtener array de datos
                    JSONArray jsonArray = jsonObj.getJSONArray("Response");
                    for(int i=0;i<jsonArray.length();i++){
                        JSONObject dato = jsonArray.getJSONObject(i);//Conjunto de datos
                        folios[i]=dato.getString("k_folio")+":"+dato.getString("k_fecha");
                        mensaje="";
                    }//for
                }catch (final JSONException e) {
                    //Log.e(TAG, "Error al convertir Json: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mensaje="No se pudieron traer datos de folios";
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
        }//doInBackground

        @Override
        protected void onPostExecute(Void aBoolean) {
            super.onPostExecute(aBoolean);
            mDialog.dismiss();
            if(folios.length==0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepConten.this);
                builder.setTitle("AVISO");
                builder.setMessage(mensaje);
                builder.setCancelable(false);
                builder.setNegativeButton("OK",null);
                AlertDialog dialog = builder.create();
                dialog.show();
                txtProdVi.setText("");
            }else{
                ArrayList <String>selectedItems = new ArrayList();
                folio1 = "";folio2="";folio3="";
                AlertDialog.Builder alert = new AlertDialog.Builder(ActivityRecepConten.this);
                alert.setTitle("Lista de Folios").setMultiChoiceItems(folios, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        if (b){
                            String[] sp = folios[i].split(":");
                            if((selectedItems.size()+1)<4){
                                selectedItems.add(sp[0]);
                            }else{
                                ((AlertDialog) dialogInterface).getListView().setItemChecked(i, false);
                                Toast.makeText(ActivityRecepConten.this, "3 folios máximo", Toast.LENGTH_SHORT).show();
                            }//else
                        }else if (selectedItems.contains(i)) {
                            selectedItems.remove(i);
                        }
                    }//setmultiitems
                }).setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        folios = new String[selectedItems.size()+1];
                        for(int j=0;j<selectedItems.size()+1;j++){
                            if(j==0){
                                folios[j]="--Todos--";
                            }else{
                                folios[j]=selectedItems.get(j-1);
                                if(folio1.equals("")){folio1=folios[j];}
                                else{
                                    if(folio2.equals("")){folio2=folios[j];}
                                    else{
                                        folio3=folios[j];
                                    }//else
                                }//else
                            }//else
                        }//for
                        listaFolios();
                        new AsyncRecepCon(folio1,folio2,folio3).execute();
                    }//onclick
                }).setNegativeButton("Cancelar",null);
                alert.create();
                AlertDialog dialog = alert.create();
                dialog.show();
            }//else

        }//onPost
    }//AsyncRecepCon

}//ActivityRecepConten

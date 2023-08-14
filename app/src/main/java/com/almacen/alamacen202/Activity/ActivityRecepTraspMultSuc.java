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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.almacen.alamacen202.Adapter.AdaptadorTraspasos;
import com.almacen.alamacen202.Adapter.AdapterInventario;
import com.almacen.alamacen202.R;
import com.almacen.alamacen202.SetterandGetters.Folios;
import com.almacen.alamacen202.SetterandGetters.Inventario;
import com.almacen.alamacen202.SetterandGetters.Traspasos;
import com.almacen.alamacen202.Sqlite.ConexionSQLiteHelper;
import com.almacen.alamacen202.XML.XMLActualizaInv;
import com.almacen.alamacen202.XML.XMLFolios;
import com.almacen.alamacen202.XML.XMLRecepConsul;
import com.almacen.alamacen202.XML.XMLRecepMultSuc;
import com.almacen.alamacen202.XML.XMLlistInv;
import com.almacen.alamacen202.includes.MyToolbar;
import com.squareup.picasso.Picasso;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class ActivityRecepTraspMultSuc extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private SharedPreferences preference;
    private boolean datos=false,modificados=false;
    private int posicion=0,posicion2=0,posG=-1;
    private String strusr,strpass,strbran,strServer,codeBar,mensaje,Producto="",serv;
    private ArrayList<Traspasos> listaTrasp = new ArrayList<>();
    private EditText txtProd,txtCantidad,txtCantSurt;
    private ImageView ivProd;
    private TextView tvProd;
    private Button btnBuscar,btnAtras,btnAdelante,btnCorr;
    private RecyclerView rvTraspasos;
    private AdaptadorTraspasos adapter;
    private AlertDialog mDialog;
    private InputMethodManager keyboard;
    private String urlImagenes,extImg;
    private int sonido_correcto,sonido_error;
    private SoundPool bepp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recep_trasp_mult_suc);

        MyToolbar.show(this, "Recepción Traspasos Multisucursal", true);
        preference = getSharedPreferences("Login", Context.MODE_PRIVATE);
        strusr = preference.getString("user", "null");
        strpass = preference.getString("pass", "null");
        strbran = preference.getString("codBra", "null");
        strServer = preference.getString("Server", "null");
        codeBar = preference.getString("codeBar", "null");
        urlImagenes=preference.getString("urlImagenes", "null");
        extImg=preference.getString("ext", "null");

        switch (strServer) {
            case "sprautomotive.servehttp.com:9090":
                serv="RODATECH";
                break;
            case "sprautomotive.servehttp.com:9095":
                serv="PARTECH";
                break;
            case "sprautomotive.servehttp.com:9080":
                serv="TG";
                break;
        }

        mDialog = new SpotsDialog.Builder().setContext(ActivityRecepTraspMultSuc.this).
                setMessage("Espere un momento...").build();
        mDialog.setCancelable(false);

        progressDialog = new ProgressDialog(ActivityRecepTraspMultSuc.this);//parala barra de
        progressDialog.setMessage("Procesando datos....");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        txtProd    = findViewById(R.id.txtProducto);
        txtCantidad = findViewById(R.id.txtCantidad);
        txtCantSurt = findViewById(R.id.txtCantSurt);
        tvProd      = findViewById(R.id.tvProd);
        btnBuscar  = findViewById(R.id.btnBuscar);
        btnAtras    = findViewById(R.id.btnAtras);
        btnAdelante =findViewById(R.id.btnAdelante);
        ivProd      = findViewById(R.id.ivProd);
        btnCorr     = findViewById(R.id.btnCorr);

        bepp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        sonido_correcto = bepp.load(ActivityRecepTraspMultSuc.this, R.raw.sonido_correct, 1);
        sonido_error = bepp.load(ActivityRecepTraspMultSuc.this, R.raw.error, 1);

        rvTraspasos    = findViewById(R.id.rvTraspasos);
        rvTraspasos.setLayoutManager(new LinearLayoutManager(ActivityRecepTraspMultSuc.this));
        adapter = new AdaptadorTraspasos(listaTrasp);
        keyboard = (InputMethodManager) getSystemService(ActivityRecepTraspMultSuc.INPUT_METHOD_SERVICE);

        txtProd.setInputType(InputType.TYPE_NULL);
        //txtProd.requestFocus();

        txtProd.addTextChangedListener(new TextWatcher() {
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
                        cambio(Producto,true);
                        txtProd.setText("");
                    }else{
                        for (int i = 0; i < editable.length(); i++) {
                            char ban;
                            ban = editable.charAt(i);
                            if (ban == '\n') {
                                posicion2=posG;
                                cambio(Producto,true);
                                txtProd.setText("");
                                break;
                            }//if
                        }//for
                    }//else
                }//if es diferente a vacio
            }//after
        });//txtProd textchange

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listaTrasp.size()>0){//si tiene
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepTraspMultSuc.this);
                    builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Producto="";
                            new AsyncRecepConsul().execute();
                        }//onclick
                    });//positive button
                    builder.setNegativeButton("CANCELAR",null);
                    builder.setCancelable(false);
                    builder.setTitle("Aviso").setMessage("Se eliminaran los datos que esten almacenados en el teléfono \n¿Desea continuar?").create().show();
                }else{
                    Producto="";
                    new AsyncRecepConsul().execute();
                }
            }//onclick
        });//btnGuardar setonclick

        btnAdelante.setOnClickListener(new View.OnClickListener() {//boton adelante
            @Override
            public void onClick(View view) {
                posicion2=posicion;
                cambio("next",false);
            }//onclick
        });//btnadelante setonclicklistener

        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posicion2=posicion;
                cambio("back",false);
            }//onclick
        });//btnatras setonclicklistener

        btnCorr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepTraspMultSuc.this);
                builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int cantAct=Integer.parseInt(listaTrasp.get(posicion).getCantSurt());
                        if(cantAct==0){
                            Toast.makeText(ActivityRecepTraspMultSuc.this, "Escaneados en 0, no se puede restar", Toast.LENGTH_SHORT).show();
                        }else{
                            listaTrasp.get(posicion).setCantSurt((cantAct-1)+"");
                            listaTrasp.get(posicion).setSincronizado(false);
                        }//else
                    }//onclick
                });
                builder.setNegativeButton("",null);
                builder.setCancelable(false);
                builder.setTitle("AVISO").setMessage("Se corregirá "+Producto+" con una pieza de más").create().show();
            }//onclick
        });//btnCorr

    }//onCreate

    public boolean firtMet() {//firtMet
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {//si hay conexion a internet
            return true;
        }else {
            return false;
        }//else
    }//FirtMet saber si hay conexion a internet

    public void cambiaProd(){
        if(posicion==0 && listaTrasp.size()>1){
            btnAdelante.setEnabled(true);
            btnAdelante.setBackgroundTintList(null);
            btnAdelante.setBackgroundResource(R.drawable.btn_background3);
            btnAtras.setEnabled(false);
            btnAtras.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));

        }else if(posicion+1==listaTrasp.size() && listaTrasp.size()>1){
            btnAtras.setEnabled(true);
            btnAtras.setBackgroundTintList(null);
            btnAtras.setBackgroundResource(R.drawable.btn_background3);
            btnAdelante.setEnabled(false);
            btnAdelante.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
        }else if(listaTrasp.size()==1){
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

    public void onClickLista(View v){//cada vez que se seleccione un producto en la lista
        if(posicion>=0 && listaTrasp.get(posicion).isSincronizado()==false){
            posicion2=posG;
            Producto=listaTrasp.get(rvTraspasos.getChildPosition(rvTraspasos.findContainingItemView(v))).getProducto();
        }else{
            posicion2 = rvTraspasos.getChildPosition(rvTraspasos.findContainingItemView(v));
        }
        cambio("change",false);
    }//onClickLista


    public void cambio(String var,boolean sumar){
        if(!listaTrasp.get(posicion2).getProducto().equals(Producto) && posG!=-1 && listaTrasp.get(posicion2).isSincronizado()==false){//identificando que prod anterior no se sincronizó
            new AsyncAct(listaTrasp.get(posicion2).getProducto(),listaTrasp.get(posicion2).getCantSurt(),var,sumar,Producto).execute();
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
    }

    public void mostrarDetalleProd(){//detalle por producto seleccionado
        adapter.index(posicion);
        adapter.notifyDataSetChanged();
        rvTraspasos.scrollToPosition(posicion);
        //Producto=listaTrasp.get(posicion).getProducto();
        tvProd.setText(listaTrasp.get(posicion).getProducto());
        txtCantidad.setText(listaTrasp.get(posicion).getCantidad());
        txtCantSurt.setText(listaTrasp.get(posicion).getCantSurt());

        if(Integer.parseInt(listaTrasp.get(posicion).getCantidad())==Integer.parseInt(listaTrasp.get(posicion).getCantSurt())){
            txtCantSurt.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }else{
            txtCantSurt.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.ColorGris)));
        }
        cambiaProd();

        if(!txtCantSurt.getText().toString().equals("") && Integer.parseInt(txtCantSurt.getText().toString())>0){
            btnCorr.setEnabled(true);
            btnCorr.setBackgroundTintList(null);
            btnCorr.setBackgroundResource(R.drawable.btn_background2);
        }else{
            btnCorr.setEnabled(false);
            btnCorr.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));
        }

        Picasso.with(getApplicationContext()).
                load(urlImagenes +
                        tvProd.getText().toString() + extImg)
                .error(R.drawable.aboutlogo)
                .fit()
                .centerInside()
                .into(ivProd);
        posG=posicion;
    }//mostrarDetalleProd

    public void limpiar(){
        txtCantidad.setText("");
        txtCantSurt.setText("");
        ivProd.setImageResource(R.drawable.logokepler);
        btnAtras.setEnabled(false);
        btnAtras.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.ColorGris)));
        btnAdelante.setEnabled(false);
        btnAdelante.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.ColorGris)));
        btnCorr.setEnabled(false);
        btnCorr.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.ColorGris)));
        posG=-1;
    }//limpiar

    public int encontrarPosEnLista(String prod){
        int p=posG;
        for(int i=0;i<listaTrasp.size();i++){
            if(listaTrasp.get(i).getProducto().equals(prod)){
                p=i;
                break;
            }//if
        }
        return p;
    }

    public void evaluarEscaneo(String prod){
        limpiar();
        boolean existe=false;
        for(int i=0;i<listaTrasp.size();i++){
            if(listaTrasp.get(i).getProducto().equals(prod)){
                posicion=i;
                existe=true;
                int cant=Integer.parseInt(listaTrasp.get(i).getCantidad());
                int cantS=Integer.parseInt(listaTrasp.get(i).getCantSurt());
                if((cantS+1)<=cant){
                    cantS++;
                    listaTrasp.get(i).setCantSurt(cantS+"");
                    listaTrasp.get(i).setSincronizado(false);
                    modificados=true;
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



    private class AsyncRecepConsul extends AsyncTask<Void, Void, Void> {//WEBSERVICE PARA CONSULTAR LOS PRODUCTOS
        private boolean conn=true;
        @Override
        protected void onPreExecute() {mDialog.show();}

        @Override
        protected Void doInBackground(Void... params) {
            datos=false;
            if(firtMet()==true){
                listaTrasp.clear();
                posicion=-1;
                modificados=false;
                conectaRecepCon();
                limpiar();
            }else{conn=false;}//else
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected void onPostExecute(Void result) {
            if(conn=false){
                mDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepTraspMultSuc.this);
                builder.setPositiveButton("ACEPTAR",null);
                builder.setCancelable(false);
                builder.setTitle("AVISO").setMessage("Sin conexión a internet").create().show();
            }else if(mensaje.equals("PENDIENTES")){
                mDialog.dismiss();
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepTraspMultSuc.this);
                builder.setPositiveButton("ACEPTAR",null);
                builder.setCancelable(false);
                builder.setTitle("AVISO").setMessage("Existen productos pendientes por procesar en kepler, para continuar valide en kepler").create().show();
            }else if(datos==true) {
                verLista();
                mDialog.dismiss();
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepTraspMultSuc.this);
                builder.setPositiveButton("ACEPTAR",null);
                builder.setCancelable(false);
                builder.setTitle("AVISO").setMessage("Ningun Dato").create().show();
                mDialog.dismiss();
            }//else
        }//onPostExecute
    }//AsynInsertInv


    private void conectaRecepCon() {
        String SOAP_ACTION = "RecepcionConsulta";
        String METHOD_NAME = "RecepcionConsulta";
        String NAMESPACE = "http://" + strServer + "/WSk75AlmacenesApp/";
        String URL = "http://" + strServer + "/WSk75AlmacenesApp";
        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
            XMLRecepConsul soapEnvelope = new XMLRecepConsul(SoapEnvelope.VER11);
            soapEnvelope.XMLRecepCon(strusr, strpass,strbran);
            soapEnvelope.dotNet = true;
            soapEnvelope.implicitTypes = true;
            soapEnvelope.setOutputSoapObject(Request);
            HttpTransportSE trasport = new HttpTransportSE(URL);
            trasport.debug = true;
            trasport.call(SOAP_ACTION, soapEnvelope);
            SoapObject response = (SoapObject) soapEnvelope.bodyIn;
            int c=1;
            for (int i = 0; i < response.getPropertyCount(); i++) {
                SoapObject response0 = (SoapObject) soapEnvelope.bodyIn;
                response0 = (SoapObject) response0.getProperty(i);
                mensaje=(response0.getPropertyAsString("MENSAJE").equals("anyType{}") ? "": response0.getPropertyAsString("MENSAJE"));
                if(mensaje.equals("PENDIENTES")){break;}

                String clave=(response0.getPropertyAsString("PRODUCTO").equals("anyType{}") ? " " : response0.getPropertyAsString("PRODUCTO"));
                String can=(response0.getPropertyAsString("CANTIDAD").equals("anyType{}") ? "" : response0.getPropertyAsString("CANTIDAD"));
                String ub=(response0.getPropertyAsString("UBICACION").equals("anyType{}") ? "" : response0.getPropertyAsString("UBICACION"));
                String surt=(response0.getPropertyAsString("SURTIDO").equals("anyType{}") ? "" : response0.getPropertyAsString("SURTIDO"));
                listaTrasp.add(new Traspasos(c+"",clave,can,surt,ub,true));
                datos=true;
                c++;
            }//for
        } catch (Exception ex) {mensaje="";}//catch
    }//conectaListInv

    private class AsyncAct extends AsyncTask<Void, Integer, Void> {//WEBSERVICE PARA ACTUALIZAR DATOS
        private String producto,cantidad,var,ProductoActual;
        private boolean conn=true,sumar;

        public AsyncAct(String producto, String cantidad,String var,boolean sumar,String ProductoActual) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.var=var;
            this.sumar=sumar;
            this.ProductoActual=ProductoActual;
        }

        @Override
        protected void onPreExecute() {mDialog.show();}

        @Override
        protected Void doInBackground(Void... params) {
            mensaje="";
            if(firtMet()==true){//si hay conexión a internet
                conectaRecepMultSuc(producto,cantidad);
            }else{conn=false;}
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            if(conn==false){
                Toast.makeText(ActivityRecepTraspMultSuc.this, "Sin conexión a internet\n"+
                        "No se podrá seguir escaneando a menos que se actualice este producto", Toast.LENGTH_SHORT).show();
            }else if (mensaje.equals("SINCRONIZADO")) {
                Toast.makeText(ActivityRecepTraspMultSuc.this, producto+" Sincronizado", Toast.LENGTH_SHORT).show();
                bepp.play(sonido_correcto, 1, 1, 1, 0, 0);
                listaTrasp.get(posicion2).setSincronizado(true);

                if(sumar==true){
                    evaluarEscaneo(ProductoActual);
                }else{
                    tipoCambio(var);
                    mostrarDetalleProd();
                }
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepTraspMultSuc.this);
                builder.setPositiveButton("ACEPTAR",null);
                builder.setCancelable(false);
                builder.setTitle("AVISO").setMessage("Producto "+producto+" no se actualizó, no se podrá seguir escaneando a menos que se actualice").create().show();
            }//else
        }//onPostExecute
    }//AsynInsertInv


    private void conectaRecepMultSuc (String producto, String cant) {
        String SOAP_ACTION = "RecepcionMultisucursal";
        String METHOD_NAME = "RecepcionMultisucursal";
        String NAMESPACE = "http://" + strServer + "/WSk75AlmacenesApp/";
        String URL = "http://" + strServer + "/WSk75AlmacenesApp";
        try {

            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
            XMLRecepMultSuc soapEnvelope = new XMLRecepMultSuc(SoapEnvelope.VER11);
            soapEnvelope.XMLTrasp(strusr, strpass, strbran, producto,cant);
            soapEnvelope.dotNet = true;
            soapEnvelope.implicitTypes = true;
            soapEnvelope.setOutputSoapObject(Request);
            HttpTransportSE trasport = new HttpTransportSE(URL);
            trasport.debug = true;
            trasport.call(SOAP_ACTION, soapEnvelope);
            SoapObject response = (SoapObject) soapEnvelope.bodyIn;
            response = (SoapObject) response.getProperty("PRODUCTO");

            mensaje=(response.getPropertyAsString("MENSAJE").equals("anyType{}") ? null : response.getPropertyAsString("MENSAJE"));
        }catch (SoapFault soapFault) {
            mensaje=soapFault.getMessage();
        }catch (XmlPullParserException e) {
            mensaje=e.getMessage();
        }catch (IOException e) {
            mensaje=e.getMessage();
        }catch (Exception ex) {
            mensaje=ex.getMessage();
        }//catch
    }//conectaActualiza

    public void verLista(){
        adapter = new AdaptadorTraspasos(listaTrasp);
        rvTraspasos.setAdapter(adapter);
        txtProd.setEnabled(true);
        txtProd.requestFocus();
        posicion=0;
        mostrarDetalleProd();
    }
    @Override
    public void onBackPressed() {
        if(modificados==true){
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepTraspMultSuc.this);
            builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            builder.setNegativeButton("CANCELAR",null);
            builder.setCancelable(false);
            builder.setTitle("AVISO").setMessage("Se hicieron movimientos ¿desea salir?").create().show();
        }else{
            finish();
        }
    }
}//ActivityInventario
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
    private boolean escaneo=false,datos=false;
    private int posicion=0,datPSinc=0;//para contar los registros que se escanearon y se van a sincronizar
    private String strusr,strpass,strbran,strServer,codeBar,mensaje,Producto="",serv;
    private ArrayList<Traspasos> listaTrasp = new ArrayList<>();
    private EditText txtProd,txtCantidad,txtCantSurt;
    private ImageView ivProd;
    private TextView tvProd;
    private Button btnBuscar,btnAtras,btnAdelante,btnSincro;
    private RecyclerView rvTraspasos;
    private AdaptadorTraspasos adapter;
    private AlertDialog mDialog;
    private ConexionSQLiteHelper conn;
    private SQLiteDatabase db;
    private InputMethodManager keyboard;
    private String urlImagenes,extImg;
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
        btnSincro   = findViewById(R.id.btnSincro);
        ivProd      = findViewById(R.id.ivProd);

        conn = new ConexionSQLiteHelper(ActivityRecepTraspMultSuc.this, "bd_INVENTARIO", null, 1);
        db = conn.getReadableDatabase();//apertura de la base de datos interna
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
                if(!editable.toString().equals("") && escaneo==true){
                    if (codeBar.equals("Zebra")) {
                        buscarEnSql(Producto);
                        txtProd.setText("");
                    }else{
                        for (int i = 0; i < editable.length(); i++) {
                            char ban;
                            ban = editable.charAt(i);
                            if (ban == '\n') {
                                buscarEnSql(Producto);
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

        btnSincro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datPSinc=datPSincro();
                if(listaTrasp.size()>0 && datPSinc>0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepTraspMultSuc.this);
                    builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new AsyncRecepMultiSuc().execute();
                        }//onclick
                    });//positive button
                    builder.setNegativeButton("CANCELAR",null);
                    builder.setCancelable(false);
                    builder.setTitle("Confirmación").setMessage(datPSinc+" productos escaneados ¿Desea sincronizar?").create().show();
                }else{
                    Toast.makeText(ActivityRecepTraspMultSuc.this, "Sin datos para sincronizar", Toast.LENGTH_SHORT).show();
                }
            }//onclick
        });//btnSincro setonclick

        btnAdelante.setOnClickListener(new View.OnClickListener() {//boton adelante
            @Override
            public void onClick(View view) {
                posicion++;
                mostrarDetalleProd();
            }//onclick
        });//btnadelante setonclicklistener

        btnAtras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posicion--;
                mostrarDetalleProd();
            }//onclick
        });//btnatras setonclicklistener
        consultaSql();
    }//onCreate

    @Override
    protected void onDestroy() {//para cerrar db de base de datos interna cuando se cierre la aplicacion
        super.onDestroy();
        db.close();
    }//onDestroy

    public int datPSincro(){//saber cuantos datos son para sincronizar(cantidad de surtido no sea 0) o sea que se escaneo
        int num=0,cont=0;
        for(int i=0;i<listaTrasp.size();i++){
            num=Integer.parseInt(listaTrasp.get(i).getCantSurt());
            if(num>0){
                cont++;
            }//if
        }//for
        return cont;
    }//datPSincro

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

    public void cambiaProd(){
        if(posicion==0){
            btnAdelante.setEnabled(true);
            btnAdelante.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.colorPrimary)));
            btnAtras.setEnabled(false);
            btnAtras.setBackgroundTintList(ColorStateList.
                    valueOf(getResources().getColor(R.color.ColorGris)));

        }else if(posicion+1==listaTrasp.size()){
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

    public void onClickLista(View v){//cada vez que se seleccione un producto en la lista
        posicion = rvTraspasos.getChildPosition(rvTraspasos.findContainingItemView(v));
        mostrarDetalleProd();
    }//onClickLista

    public void mostrarDetalleProd(){//detalle por producto seleccionado
        adapter.index(posicion);
        adapter.notifyDataSetChanged();
        rvTraspasos.scrollToPosition(posicion);
        Producto=listaTrasp.get(posicion).getProducto();
        tvProd.setText(Producto);
        txtCantidad.setText(listaTrasp.get(posicion).getCantidad());
        txtCantSurt.setText(listaTrasp.get(posicion).getCantSurt());

        if(Integer.parseInt(listaTrasp.get(posicion).getCantidad())==Integer.parseInt(listaTrasp.get(posicion).getCantSurt())){
            txtCantSurt.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }else{
            txtCantSurt.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.ColorGris)));
        }
        cambiaProd();
        Picasso.with(getApplicationContext()).
                load(urlImagenes +
                        tvProd.getText().toString() + extImg)
                .error(R.drawable.aboutlogo)
                .fit()
                .centerInside()
                .into(ivProd);
        escaneo=true;
    }//mostrarDetalleProd



    private class AsyncRecepConsul extends AsyncTask<Void, Void, Void> {//WEBSERVICE PARA CONSULTAR LOS PRODUCTOS
        private boolean conn=true;
        @Override
        protected void onPreExecute() {mDialog.show();}

        @Override
        protected Void doInBackground(Void... params) {
            datos=false;
            if(firtMet()==true){
                escaneo=false;
                eliminarSql("");
                listaTrasp.clear();
                conectaRecepCon();
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
                consultaSql();
                mDialog.dismiss();
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepTraspMultSuc.this);
                builder.setPositiveButton("ACEPTAR",null);
                builder.setCancelable(false);
                builder.setTitle("AVISO").setMessage("Ningun Dato").create().show();
                consultaSql();
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
            for (int i = 0; i < response.getPropertyCount(); i++) {
                SoapObject response0 = (SoapObject) soapEnvelope.bodyIn;
                response0 = (SoapObject) response0.getProperty(i);
                mensaje=(response0.getPropertyAsString("MENSAJE").equals("anyType{}") ? "": response0.getPropertyAsString("MENSAJE"));
                if(mensaje.equals("PENDIENTES")){break;}

                String clave=(response0.getPropertyAsString("PRODUCTO").equals("anyType{}") ? " " : response0.getPropertyAsString("PRODUCTO"));
                String can=(response0.getPropertyAsString("CANTIDAD").equals("anyType{}") ? "" : response0.getPropertyAsString("CANTIDAD"));
                String ub=(response0.getPropertyAsString("UBICACION").equals("anyType{}") ? "" : response0.getPropertyAsString("UBICACION"));
                String surt="0";
                insertarSql(clave, can,surt,ub);
                datos=true;
            }//for
        } catch (Exception ex) {mensaje="";}//catch
    }//conectaListInv

    private class AsyncRecepMultiSuc extends AsyncTask<Void, Integer, Void> {//WEBSERVICE PARA ACTUALIZAR DATOS
        private String pro,cc;
        private int contador=0;
        private int contador2=0;
        private boolean conn=true;
        @Override
        protected void onPreExecute() {progressDialog.show();}

        @Override
        protected Void doInBackground(Void... params) {
            escaneo=false;
            mensaje="";
            if(firtMet()==true){//si hay conexión a internet
                progressDialog.setMax(listaTrasp.size());
                String fecha =new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date());
                String hora=new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                for(int i=0;i<listaTrasp.size();i++){
                    try {
                        pro=listaTrasp.get(i).getProducto();
                        cc=listaTrasp.get(i).getCantSurt();
                        if(Integer.parseInt(cc)>0){
                            conectaRecepMultSuc(pro,cc,fecha,hora);
                            if(mensaje.equals("SINCRONIZADO")){
                                eliminarSql("AND PRODUCTO='"+pro+"'");//si se sincroniza se elimina de la base de datos sqlite del telefono
                                contador++;
                                mensaje="";
                            }else{
                                contador2++;
                            }//else
                        }else{
                            eliminarSql("AND PRODUCTO='"+pro+"'");//si se sincroniza se elimina de la base de datos sqlite del telefono
                            contador2++;
                        }//else

                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return null;
                    }//catch
                    progressDialog.setProgress(i);
                }//for
            }else{conn=false;}
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress[0]);
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            if (contador==datPSincro()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepTraspMultSuc.this);
                builder.setPositiveButton("OK", null);
                builder.setCancelable(false);
                builder.setTitle("Resultado Sincronización").setMessage("Total de productos escaneados: "+datPSinc+"\n"+" Datos sincronizados: "+contador+"\n"+" Datos no sincronizados: "+contador2).create().show();
                txtProd.setText("");
                consultaSql();
                tvProd.setText("");
                txtCantidad.setText("");
                txtCantSurt.setText("");
                Picasso.with(getApplicationContext()).
                        load(R.drawable.aboutlogo)
                        .error(R.drawable.aboutlogo)
                        .fit()
                        .centerInside()
                        .into(ivProd);
                btnAdelante.setEnabled(false);
                btnAtras.setEnabled(false);
                btnAdelante.setBackgroundTintList(ColorStateList.
                        valueOf(getResources().getColor(R.color.ColorGris)));
                btnAtras.setBackgroundTintList(ColorStateList.
                        valueOf(getResources().getColor(R.color.ColorGris)));
                datPSinc=0;
            }else if(conn==false){
                Toast.makeText(ActivityRecepTraspMultSuc.this, "Sin conexión a internet", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(ActivityRecepTraspMultSuc.this, "Error al actualizar datos", Toast.LENGTH_SHORT).show();
                txtProd.setText("");
                consultaSql();
            }//else
        }//onPostExecute
    }//AsynInsertInv


    private void conectaRecepMultSuc (String producto, String cant,String fecha,String hora) {
        String SOAP_ACTION = "RecepcionMultisucursal";
        String METHOD_NAME = "RecepcionMultisucursal";
        String NAMESPACE = "http://" + strServer + "/WSk75AlmacenesApp/";
        String URL = "http://" + strServer + "/WSk75AlmacenesApp";
        try {

            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
            XMLRecepMultSuc soapEnvelope = new XMLRecepMultSuc(SoapEnvelope.VER11);
            soapEnvelope.XMLTrasp(strusr, strpass, strbran, producto,cant,fecha,hora);
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

    public void consultaSql(){
        try{
            int i=1;
            escaneo=false;
            listaTrasp.clear();
            rvTraspasos.setAdapter(null);
            @SuppressLint("Recycle") Cursor fila = db.rawQuery("SELECT PRODUCTO,CANTIDAD,SURTIDO,UBICACION FROM INVENTARIO WHERE EMPRESA='"+serv+"' ORDER BY UBICACION ", null);
            if (fila != null && fila.moveToFirst()) {
                do {
                    listaTrasp.add(new Traspasos((i++)+"",fila.getString(0),fila.getString(1),fila.getString(2),fila.getString(3)));
                } while (fila.moveToNext());
                adapter = new AdaptadorTraspasos(listaTrasp);
                rvTraspasos.setAdapter(adapter);
                posicion=0;
                for (int j =0 ; j<listaTrasp.size();j++){//para tomar posicion de producto y que con eso se pueda posicionar en el producto en la lista
                    if (Producto.equals(listaTrasp.get(j).getProducto())){
                        posicion=j;
                        break;
                    }//if
                }//for
                txtProd.setEnabled(true);
                txtProd.requestFocus();
                mostrarDetalleProd();
            }//if
            fila.close();
        }catch(Exception e){
            Toast.makeText(ActivityRecepTraspMultSuc.this,
                    "Error al consultar datos de la base de datos interna", Toast.LENGTH_SHORT).show();
        }//catch
    }//consultaSql

    public void buscarEnSql(String prod){
        try{
            int cant=0,cantS=0;
            @SuppressLint("Recycle") Cursor fila = db.rawQuery(
                    "SELECT PRODUCTO,CANTIDAD,SURTIDO FROM INVENTARIO WHERE PRODUCTO='"+prod+"' AND EMPRESA='"+serv+"'", null);
            if (fila != null && fila.moveToFirst()) {
                cant=Integer.parseInt(fila.getString(1));
                cantS=Integer.parseInt(fila.getString(2));
                if((cantS+1)<=cant){
                    cantS++;
                    actualizarSql(prod,cantS+"");
                }else{
                    Toast.makeText(this, "Excede cantidad", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "No existe el producto en la lista", Toast.LENGTH_SHORT).show();
                escaneo=false;
            }
            fila.close();
        }catch(Exception e){
            Toast.makeText(ActivityRecepTraspMultSuc.this,
                    e+"", Toast.LENGTH_SHORT).show();
        }//catch
        consultaSql();
    }//consultaSql

    public void insertarSql(String prod,String cant,String cantS,String ubi){
        try{
            if(db != null){
                ContentValues valores = new ContentValues();
                valores.put("EMPRESA", serv);
                valores.put("PRODUCTO", prod);
                valores.put("CANTIDAD", cant);
                valores.put("SURTIDO", cantS);
                valores.put("UBICACION", ubi);
                db.insert("INVENTARIO", null, valores);
            }
        }catch(Exception e){
            Toast.makeText(this, "Problema al guardar producto", Toast.LENGTH_SHORT).show();
        }
    }//insertarSql

    public void actualizarSql(String prod,String cantSurt){
        try{
            ContentValues valores = new ContentValues();
            valores.put("SURTIDO", cantSurt);
            db.update("INVENTARIO", valores, " PRODUCTO='" + prod+"' AND EMPRESA='"+serv+"'", null);
        }catch(Exception e){
            Toast.makeText(this, "Problema al actualizar la cantidad del producto", Toast.LENGTH_SHORT).show();
        }
    }//actualizarSql

    public void eliminarSql(String sentProd) {//Se envia (AND PRODUCTO='"+prod+"') si se usa para eliminar un producto, vacio para todos los productos
        try{
            SQLiteDatabase db = conn.getWritableDatabase();
            db.delete("INVENTARIO","EMPRESA='"+serv+"' "+sentProd,null);
        }catch(Exception e){}
    }//eliminarSql

}//ActivityInventario

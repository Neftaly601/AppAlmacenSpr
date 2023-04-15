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
import android.widget.EditText;
import android.widget.ImageView;
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
import com.almacen.alamacen202.SetterandGetters.Traspasos;
import com.almacen.alamacen202.Sqlite.ConexionSQLiteHelper;
import com.almacen.alamacen202.XML.XMLRecepConsul;
import com.almacen.alamacen202.XML.XMLRecepMultSuc;
import com.almacen.alamacen202.includes.MyToolbar;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
    private boolean escaneo=false,datos=false;
    private int posicion=0,datPSinc=0;//para contar los registros que se escanearon y se van a sincronizar
    private String strusr,strpass,strbran,strServer,codeBar,mensaje,Producto="",folio="";
    private ArrayList<RecepConten> listaRecep = new ArrayList<>();
    private EditText txtBfolio;
    private ImageView ivProd;
    private TextView tvProd;
    private Button btnBuscaFolio,btnAtras,btnAdelante,btnSincro;
    private RecyclerView rvRecep;
    private AdaptadorRecepConten adapter;
    private AlertDialog mDialog;
    private ConexionSQLiteHelper conn;
    private SQLiteDatabase db;
    private InputMethodManager keyboard;
    private String urlImagenes,extImg;
    private RequestQueue mQueue;
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

        mQueue = Volley.newRequestQueue(this);

        mDialog = new SpotsDialog.Builder().setContext(ActivityRecepConten.this).
                setMessage("Espere un momento...").build();
        mDialog.setCancelable(false);

        progressDialog = new ProgressDialog(ActivityRecepConten.this);//parala barra de
        progressDialog.setMessage("Procesando datos....");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        txtBfolio    = findViewById(R.id.txtBfolio);
        btnBuscaFolio  = findViewById(R.id.btnBuscaFolio);
        /*btnAtras    = findViewById(R.id.btnAtras);
        btnAdelante =findViewById(R.id.btnAdelante);
        btnSincro   = findViewById(R.id.btnSincro);
        ivProd      = findViewById(R.id.ivProd);*/

        conn = new ConexionSQLiteHelper(ActivityRecepConten.this, "bd_INVENTARIO", null, 1);
        db = conn.getReadableDatabase();//apertura de la base de datos interna
        rvRecep    = findViewById(R.id.rvRecep);
        rvRecep.setLayoutManager(new LinearLayoutManager(ActivityRecepConten.this));
        adapter = new AdaptadorRecepConten(listaRecep);
        keyboard = (InputMethodManager) getSystemService(ActivityRecepConten.INPUT_METHOD_SERVICE);


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

        /*g.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                folio=editable.toString();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepConten.this);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRecepConten.this);
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
                    Toast.makeText(ActivityRecepConten.this, "Sin datos para sincronizar", Toast.LENGTH_SHORT).show();
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
        consultaSql();*/
    }//onCreate


    private void jSonRecepContent() throws InterruptedException{
        final CountDownLatch latch = new CountDownLatch(1);
        mQueue.start();
        String url="http://"+strServer+"/"+getString(R.string.resRecepConten);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(),
                new Response.Listener<JSONObject>() {//correcto
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("Response");
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject dato = jsonArray.getJSONObject(i);//Conjunto de datos
                                listaRecep.add(new RecepConten((i++)+"",dato.getString("k_prod"),dato.getString("k_cant"),dato.getString("k_paletCaja")));
                                mensaje="";
                            }//for
                            latch.countDown();//
                        } catch (JSONException e) {
                            mensaje="Problemas al recibir datos json";
                            latch.countDown();
                        }catch (Error error){
                            mensaje="Hubó un problema";
                            latch.countDown();

                        }//catch
                    }//onResponse
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {//Para error
                latch.countDown();
                mensaje="Hubó un problema al recibir datos";
            }//onError
        })//JsonObjectRequest
        {//headers
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("user", strusr);
                headers.put("pass", strpass);
                return headers;
            }//Map
            //parametros
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("k_folio",folio);
                return params;
            }
        };//headers
        mQueue.add(request);
        //Bloqueamos el hilo hasta que el callback llame a latch.countDown
        latch.await();
    }//jSonRecepContent



    private class AsyncRecepCon extends AsyncTask<Void, Boolean, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            mensaje="";
            listaRecep.clear();
        }//onPreExecute

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                jSonRecepContent();
            } catch (InterruptedException e) {mensaje="Hubó problema con el web service";}
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
                adapter= new AdaptadorRecepConten(listaRecep);
                rvRecep.setAdapter(adapter);
                adapter.index(posicion);
                //adapter.notifyDataSetChanged();
                //rvRecep.scrollToPosition(posicion)
            }//else
        }//onPost
    }//AsyncRecepCon

}//ActivityRecepConten

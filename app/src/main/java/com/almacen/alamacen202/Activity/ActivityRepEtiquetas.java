package com.almacen.alamacen202.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.almacen.alamacen202.Adapter.AdaptadorTraspasos;
import com.almacen.alamacen202.Adapter.AdapterListProd;
import com.almacen.alamacen202.R;
import com.almacen.alamacen202.SetterandGetters.ProdEtiq;
import com.almacen.alamacen202.SetterandGetters.Traspasos;
import com.almacen.alamacen202.XML.XMLActualizaOrdenCompra;
import com.almacen.alamacen202.XML.XMLListProd;
import com.almacen.alamacen202.XML.XMLRecepConsul;
import com.almacen.alamacen202.XML.XMLRepEtiq;
import com.almacen.alamacen202.includes.MyToolbar;
import com.squareup.picasso.Picasso;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import dmax.dialog.SpotsDialog;

public class ActivityRepEtiquetas extends AppCompatActivity {
    private AlertDialog mDialog;
    private SharedPreferences preference;
    private String strusr,strpass,strbran,strServer,codeBar,mensaje="";
    private ArrayList<ProdEtiq> lista = new ArrayList<>();
    private ArrayList<ProdEtiq> listaGuardada;
    private AdapterListProd adapter;
    private RecyclerView rvProd;
    private EditText txtProdE,txtNomProd,txtDescProd,txtCantEtiq;
    private ImageView ivProd,ivCloseSearch;
    private Button btnEnviar;
    private int posicion=0;
    private String urlImagenes,extImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rep_etiq);
        mDialog = new SpotsDialog.Builder().setContext(ActivityRepEtiquetas.this).
                setMessage("Espere un momento...").build();
        mDialog.setCancelable(false);

        MyToolbar.show(this, "Reporte Incidencias Etiquetas", true);
        preference = getSharedPreferences("Login", Context.MODE_PRIVATE);
        strusr = preference.getString("user", "null");
        strpass = preference.getString("pass", "null");
        strbran = preference.getString("codBra", "null");
        strServer = preference.getString("Server", "null");
        codeBar = preference.getString("codeBar", "null");
        urlImagenes=preference.getString("urlImagenes", "null");
        extImg=preference.getString("ext", "null");

        txtProdE= findViewById(R.id.txtProdE);
        txtNomProd= findViewById(R.id.txtNomProd);
        txtDescProd= findViewById(R.id.txtDescProd);
        txtCantEtiq= findViewById(R.id.txtCantEtiq);
        ivProd= findViewById(R.id.ivProd);
        ivCloseSearch = findViewById(R.id.ivCloseSearch);
        btnEnviar= findViewById(R.id.btnEnviar);

        rvProd = findViewById(R.id.rvProd);
        rvProd.setLayoutManager(new LinearLayoutManager(ActivityRepEtiquetas.this));
        adapter = new AdapterListProd(lista);

        txtProdE.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                posicion=0;
                if(editable.toString().equals("")){
                    ivCloseSearch.setVisibility(View.GONE);
                    lista=new ArrayList<>(listaGuardada);
                    llenarRecycler();
                }else{
                    ivCloseSearch.setVisibility(View.VISIBLE);
                    buscar(editable.toString());
                }//else
            }
        });//txtProdE

        ivCloseSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtProdE.setText("");
                vaciarDet();
                posicion=0;
            }//
        });//ivClose

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(txtNomProd.getText().toString().equals("") || isNumeric(txtCantEtiq.getText().toString())==false || Integer.parseInt(txtCantEtiq.getText().toString())<=0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRepEtiquetas.this);
                    builder.setPositiveButton("ACEPTAR", null);
                    builder.setCancelable(false);
                    builder.setTitle("AVISO").setMessage("Campos vacios o en 0").create().show();
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRepEtiquetas.this);
                    builder.setNegativeButton("CANCELAR",null);
                    builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new AsynCallRepEtiq().execute();
                        }
                    });
                    builder.setCancelable(false);
                    builder.setTitle("AVISO").setMessage("Desea reportar el producto: "+txtNomProd+" con "+txtCantEtiq+" etiquetas").create().show();
                }//else
            }
        });//btnEnviaronclick

        new AsyncListProd().execute();
    }

    public void vaciarDet(){
        txtNomProd.setText("");
        txtDescProd.setText("");
        txtCantEtiq.setText("");
        ivProd.setImageResource(R.drawable.aboutlogo);
    }
    private static boolean isNumeric(String cadena){
        try {
            Integer.parseInt(cadena);
            return true;
        } catch (NumberFormatException nfe){
            return false;
        }
    }//isNumeric

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

    public void buscar(String texto){
        //llenarRecycler();
        ArrayList<ProdEtiq> copia=new ArrayList<ProdEtiq>(listaGuardada);
        lista.clear();
        int c=0;
        for(int i=0;i<copia.size();i++){
            if(copia.get(i).getProd().toLowerCase().contains(texto.toLowerCase()) ){
                c++;
                lista.add(new ProdEtiq(c+"",copia.get(i).getProd(),copia.get(i).getDescrip()));
            }//if
        }//for
        adapter.filterList(lista);
        if(lista.size()>0){
            mostrarDetalle();
        }else{
            vaciarDet();
        }
    }//buscarClientes

    public void onClickP(View v){
        txtCantEtiq.setText("0");
        posicion = rvProd.getChildPosition(rvProd.findContainingItemView(v));
        mostrarDetalle();
    }

    public void mostrarDetalle(){
        adapter.index(posicion);
        adapter.notifyDataSetChanged();
        rvProd.scrollToPosition(posicion);
        txtNomProd.setText(lista.get(posicion).getProd());
        txtDescProd.setText(lista.get(posicion).getDescrip());
        txtCantEtiq.setText("0");

        Picasso.with(getApplicationContext()).
                load(urlImagenes+
                        txtNomProd.getText().toString() + extImg)
                .error(R.drawable.aboutlogo)
                .fit()
                .centerInside()
                .into(ivProd);
    }//mostrarDetalle
    public void llenarRecycler(){
        adapter = new AdapterListProd(lista);
        rvProd.setAdapter(adapter);
        mostrarDetalle();
    }

    private class AsyncListProd extends AsyncTask<Void, Void, Void> {//WEBSERVICE PARA CONSULTAR LOS PRODUCTOS
        private boolean conn=true;
        @Override
        protected void onPreExecute() {mDialog.show();}

        @Override
        protected Void doInBackground(Void... params) {
            mensaje="";
            if(firtMet()==true){
                conectaListProd();
            }else{conn=false;}//else
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected void onPostExecute(Void result) {
            mDialog.dismiss();
            if(conn=false){
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRepEtiquetas.this);
                builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                builder.setCancelable(false);
                builder.setTitle("AVISO").setMessage("Sin conexión a internet").create().show();
            }else if(lista.size()>0) {
                listaGuardada=new ArrayList<>(lista);
                llenarRecycler();
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRepEtiquetas.this);
                builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                builder.setCancelable(false);
                builder.setTitle("AVISO").setMessage("Ningun Producto").create().show();
            }//else
        }//onPostExecute
    }//AsynInsertInv


    private void conectaListProd() {
        String SOAP_ACTION = "ListProd";
        String METHOD_NAME = "ListProd";
        String NAMESPACE = "http://" + strServer + "/WSk75AlmacenesApp/";
        String URL = "http://" + strServer + "/WSk75AlmacenesApp";
        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
            XMLListProd soapEnvelope = new XMLListProd(SoapEnvelope.VER11);
            soapEnvelope.XMLlPr(strusr, strpass,strbran);
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

                String prod=(response0.getPropertyAsString("k_prod").equals("anyType{}") ? "" : response0.getPropertyAsString("k_prod"));
                String descrip=(response0.getPropertyAsString("k_descrip").equals("anyType{}") ? "" : response0.getPropertyAsString("k_descrip"));
                String surt="0";
                lista.add(new ProdEtiq((i+1)+"",prod,descrip));
            }//for
        } catch (Exception ex) {mensaje="";}//catch
    }//conectaListInv

    //WebService Actualizar Cantidad
    private class AsynCallRepEtiq extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mDialog.show();
        }//onPreejecute

        @Override
        protected Void doInBackground(Void... params) {
            mensaje="";
            String producto=txtNomProd.getText().toString();
            String cont=txtCantEtiq.getText().toString();
            conectaRepEtiq(producto,Integer.parseInt(cont)+"");
            return null;
        }//doInBackground


        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected void onPostExecute(Void result) {
            mDialog.dismiss();
            if(mensaje.equals("Correcto")){
                mensaje="Se envió la incidencia";
            }else{mensaje="Hubó un problema "+mensaje;}
            AlertDialog.Builder alerta = new AlertDialog.Builder(ActivityRepEtiquetas.this);
            alerta.setMessage(mensaje).setCancelable(false).
                    setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }//onclick
                    });//alertDialogBuilder
            AlertDialog titulo = alerta.create();
            titulo.setTitle("Aviso");
            titulo.show();
        }//OnpostEjecute
    }//class AsynCallActualiza


    private void conectaRepEtiq(String producto,String cont) {
        String SOAP_ACTION = "ReporteEtiq";
        String METHOD_NAME = "ReporteEtiq";
        String NAMESPACE = "http://" + strServer + "/WSk75AlmacenesApp/";
        String URL = "http://" + strServer + "/WSk75AlmacenesApp";

        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
            XMLRepEtiq soapEnvelope = new XMLRepEtiq(SoapEnvelope.VER11);
            soapEnvelope.XMLRep(strusr,strpass,producto,strbran,cont);
            soapEnvelope.dotNet = true;
            soapEnvelope.implicitTypes = true;
            soapEnvelope.setOutputSoapObject(Request);
            HttpTransportSE trasport = new HttpTransportSE(URL);
            trasport.debug = true;
            trasport.call(SOAP_ACTION, soapEnvelope);
            Vector response = (Vector) soapEnvelope.getResponse();
            mensaje = response.get(0).toString();

        } catch (SoapFault soapFault) {
            mensaje = "Error: " + soapFault.getMessage();
            soapFault.printStackTrace();
        } catch (XmlPullParserException e) {
            mensaje = "Error: " + e.getMessage();
        } catch (IOException e) {
            mensaje = "No se encontró servidor";
        } catch (Exception ex) {
            mensaje = "Error: " + ex.getMessage();
        }//catch
    }//conectaActualizar
}

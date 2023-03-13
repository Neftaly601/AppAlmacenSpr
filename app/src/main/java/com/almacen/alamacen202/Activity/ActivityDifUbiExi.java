package com.almacen.alamacen202.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.almacen.alamacen202.Adapter.AdaptadorListAlmacenes;
import com.almacen.alamacen202.Adapter.AdaptadorListaFolios;
import com.almacen.alamacen202.Adapter.AdaptadorListaFolios2;
import com.almacen.alamacen202.Adapter.AdapterDifUbiExi;
import com.almacen.alamacen202.Adapter.AdapterInventario;
import com.almacen.alamacen202.R;
import com.almacen.alamacen202.SetterandGetters.Almacenes;
import com.almacen.alamacen202.SetterandGetters.DifUbiExist;
import com.almacen.alamacen202.SetterandGetters.Folios;
import com.almacen.alamacen202.SetterandGetters.Inventario;
import com.almacen.alamacen202.SetterandGetters.ProdEtiq;
import com.almacen.alamacen202.SetterandGetters.UbicacionSandG;
import com.almacen.alamacen202.Sqlite.ConexionSQLiteHelper;
import com.almacen.alamacen202.XML.XMDifUbiExist;
import com.almacen.alamacen202.XML.XMLActualizaDif;
import com.almacen.alamacen202.XML.XMLActualizaInv;
import com.almacen.alamacen202.XML.XMLFolios;
import com.almacen.alamacen202.XML.XMLUbicacionAlma;
import com.almacen.alamacen202.XML.XMLValidEsc;
import com.almacen.alamacen202.XML.XMLlistInv;
import com.almacen.alamacen202.includes.MyToolbar;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import dmax.dialog.SpotsDialog;

public class ActivityDifUbiExi extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private SharedPreferences preference,preferenceD;
    private SharedPreferences.Editor editor;
    private int posicion=0;
    private String strusr,strpass,strServer,strbran,codeBar,ProductoAct="",folio="",fecha="",hora="",mensaje,serv="",where=" AND CONTEO>0 ";
    private ArrayList<DifUbiExist> lista2 = new ArrayList<>();
    private ArrayList<Almacenes> listaAlm = new ArrayList<>();
    private EditText txtFolioInv,txtProductoVi,txtFechaI,txtHoraI,txtProducto,txtCant,txtContF,txtExistS,txtDif,txtUbb;
    private ArrayList<Folios>listaFol;
    private Button btnGuardar,btnSincronizar,btnCont,btnNoCont,btnAlma;
    private CheckBox chbMan;
    private RecyclerView rvDifUbiExi;
    private AdapterDifUbiExi adapter;
    private AlertDialog mDialog;
    private InputMethodManager keyboard;
    private ConexionSQLiteHelper conn;
    private SQLiteDatabase db;
    private RecyclerView rvFolios;//para alertdialog
    private AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dif_ubi_exi);

        MyToolbar.show(this, "Diferencia Ubic. Exist.", true);
        preferenceD = getSharedPreferences("FolioDif", Context.MODE_PRIVATE);//para guardar folio
        editor = preferenceD.edit();

        preference = getSharedPreferences("Login", Context.MODE_PRIVATE);

        folio=preferenceD.getString("folio", "");
        fecha=preferenceD.getString("fechaI", "");
        hora=preferenceD.getString("horaI", "");

        strusr = preference.getString("user", "null");
        strpass = preference.getString("pass", "null");
        strServer = preference.getString("Server", "null");
        strbran = preference.getString("codBra", "null");
        codeBar = preference.getString("codeBar", "null");
        mDialog = new SpotsDialog.Builder().setContext(ActivityDifUbiExi.this).
                setMessage("Espere un momento...").build();
        progressDialog = new ProgressDialog(ActivityDifUbiExi.this);//parala barra de
        progressDialog.setMessage("Procesando....");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

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

        txtFolioInv     = findViewById(R.id.txtFolioInv);
        txtFechaI       = findViewById(R.id.txtFechaI);
        txtHoraI        = findViewById(R.id.txtHoraI);
        txtProducto     = findViewById(R.id.txtProducto);
        txtProductoVi   = findViewById(R.id.txtProductoVi);
        txtCant         = findViewById(R.id.txtCant);
        btnGuardar      = findViewById(R.id.btnGuardar);
        btnSincronizar  = findViewById(R.id.btnSincronizar);
        chbMan          = findViewById(R.id.chbMan);
        rvDifUbiExi     = findViewById(R.id.rvDifUbiExi);
        txtContF        = findViewById(R.id.txtContF);
        txtExistS       = findViewById(R.id.txtExistS);
        txtDif          = findViewById(R.id.txtDif);
        txtUbb          = findViewById(R.id.txtUbb);
        btnCont         = findViewById(R.id.btnCont);
        btnNoCont       = findViewById(R.id.btnNoCont);
        btnAlma         = findViewById(R.id.btnAlma);

        conn = new ConexionSQLiteHelper(ActivityDifUbiExi.this, "bd_INVENTARIO", null, 1);
        db = conn.getReadableDatabase();
        rvDifUbiExi.setLayoutManager(new LinearLayoutManager(ActivityDifUbiExi.this));
        keyboard = (InputMethodManager) getSystemService(ActivityInventario.INPUT_METHOD_SERVICE);

        txtProducto.requestFocus();
        txtProducto.setInputType(InputType.TYPE_NULL);
        txtCant.setEnabled(false);
        //BOTONES CONTADOS/NOCONTADOS
        btnCont.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.AzulBack)));
        btnNoCont.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.ColorGris)));

        chbMan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                txtProducto.setText("");
                txtProductoVi.setText("");
                txtProducto.requestFocus();
                //txtProductoVi.setText("");
                contados();
                if (b){
                    //keyboard.showSoftInput(txtProducto, InputMethodManager.SHOW_IMPLICIT);
                    txtCant.setEnabled(true);
                    txtCant.setText("");
                    //keyboard.showSoftInput(Cantidad, InputMethodManager.SHOW_IMPLICIT);
                    btnGuardar.setEnabled(true);
                    btnGuardar.setBackgroundTintList(ColorStateList.
                            valueOf(getResources().getColor(R.color.AzulBack)));
                }else{
                    txtCant.setText("0");
                    txtCant.setEnabled(false);

                    keyboard.hideSoftInputFromWindow(txtCant.getWindowToken(), 0);
                    btnGuardar.setEnabled(false);
                    btnGuardar.setBackgroundTintList(ColorStateList.
                            valueOf(getResources().getColor(R.color.ColorGris)));
                }//else
            }//oncheckedchange
        });//chbMan.setoncheckedchange

        //EVENTOS txtProducto
        txtProducto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                ProductoAct=editable.toString();
                if (!editable.toString().equals("")) {
                    txtProductoVi.setText(ProductoAct);
                    if (codeBar.equals("Zebra")) {//codebar
                        if (!chbMan.isChecked()) {//manual no
                            buscar(ProductoAct,"1",true);
                            txtProducto.setText("");
                        }else{//manual si
                            buscar(ProductoAct,"-1",false);
                            txtCant.requestFocus();
                            keyboard.showSoftInput(txtCant, InputMethodManager.SHOW_IMPLICIT);
                        }//else
                    } else{
                        for (int i = 0; i < editable.length(); i++) {
                            char ban;
                            ban = editable.charAt(i);
                            if (ban == '\n') {
                                if (!chbMan.isChecked()) {//manual no
                                    buscar(ProductoAct,"1",true);
                                    txtProducto.setText("");
                                }else{//manual si
                                    buscar(ProductoAct,"-1",false);
                                    txtCant.requestFocus();
                                    keyboard.showSoftInput(txtCant, InputMethodManager.SHOW_IMPLICIT);
                                }//else
                                break;
                            }//if
                        }//for
                    }//else
                }//if !editable
            }//after
        });//txtProducto.addTextChanged


        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String v1=ProductoAct;
                String v2=txtCant.getText().toString();
                if(!v1.equals("") && !v2.equals("")){
                    buscar(v1,v2,false);
                    keyboard.hideSoftInputFromWindow(txtCant.getWindowToken(), 0);
                    txtProducto.setText("");
                    txtProductoVi.setText("");
                    txtProducto.requestFocus();
                    //keyboard.hideSoftInputFromWindow(txtProducto.getWindowToken(), 0);
                    contados();
                }else{
                    Toast.makeText(ActivityDifUbiExi.this, "Campos vacios", Toast.LENGTH_SHORT).show();
                }
            }//onclick
        });//btnGuardar setonclick

        btnSincronizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDifUbiExi.this);
                builder.setPositiveButton("ACEPTAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AsyncActualiza().execute();
                    }//onclick
                });//positive button
                builder.setNegativeButton("CANCELAR",null);
                builder.setCancelable(false);
                builder.setTitle("AVISO").setMessage("¿Desea sincronizar?").create().show();
            }//onclcik
        });//btnSincronizar onclick

        btnCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contados();
                txtProducto.setText("");
            }//onclick
        });//btnCont
        btnNoCont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noContados();
                txtProducto.setText("");
                txtProductoVi.setText("");
            }//onclick
        });//btnNoCont

        btnAlma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!txtProductoVi.getText().toString().equals("")){
                    new AsyncalListAlm().execute();
                }else{
                    Toast.makeText(ActivityDifUbiExi.this, "Ningún producto seleccionado", Toast.LENGTH_SHORT).show();
                }//else
            }//onclick
        });//btnAlma onclick


        //FOLIO
        if(folio.equals("")){//si no hay folio guardado
            new AsyncFolios().execute();
        }else{
            seleccionaFol();
            contados();
        }//else
    }//onCreate

    private static boolean isNumeric(String cadena){
        try {
            Integer.parseInt(cadena);
            return true;
        } catch (NumberFormatException nfe){
            return false;
        }
    }//isNumeric


    public void contados(){//cuando se muestre la parte de contados
        btnCont.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.AzulBack)));
        btnNoCont.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.ColorGris)));
        txtCant.setText("");
        txtContF.setText("");
        txtExistS.setText("");
        txtDif.setText("");
        txtUbb.setText("");
        where=" AND ESTATUS=1 ";
        consultaSql();
    }//contados

    public void noContados(){
        btnNoCont.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.AzulBack)));
        btnCont.setBackgroundTintList(ColorStateList.
                valueOf(getResources().getColor(R.color.ColorGris)));
        txtProducto.setText("");
        txtCant.setText("");
        txtContF.setText("");
        txtExistS.setText("");
        txtDif.setText("");
        txtUbb.setText("");
        where=" AND ESTATUS=0 ";
        consultaSql();
    }//noContados



    public void buscaFolios(View v){
        if(!folio.equals("")){//si ya hay folio guardado
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDifUbiExi.this);
            builder.setPositiveButton("FOLIO ACTUAL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    consultaSql();
                }//onclick
            });//positive button
            builder.setNegativeButton("SELECCIONAR OTRO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    editor.clear().commit();
                    eliminarSql("");
                    new AsyncFolios().execute();
                }
            });//negative
            builder.setCancelable(false);
            builder.setTitle("AVISO").setMessage("Estas trabajando con un folio"+
                    "¿Desea seleccionar uno nuevo?(Se perderan los cambios no guardados)").create().show();
        }else{//si no hay folio guardado
            new AsyncFolios().execute();
        }//else
    }//buscarFolios

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    public void seleccionEnAlertFolios2(View v){
        int pos = rvFolios.getChildAdapterPosition(rvFolios.findContainingItemView(v));
        folio=listaFol.get(pos).getFolio();
        fecha=listaFol.get(pos).getFecha();
        hora=listaFol.get(pos).getHora();
        editor.putString("folio", folio);
        editor.putString("fechaI", fecha);
        editor.putString("horaI", hora);
        editor.commit();
        rvDifUbiExi.setAdapter(null);
        dialog.dismiss();
        seleccionaFol();
        new AsyncDifUbiExist().execute();
    }//seleccionEnAlertFolios

    public void seleccionaFol(){
        txtFolioInv.setText(folio);
        txtFechaI.setText(fecha);
        txtHoraI.setText(hora);
    }

    @SuppressLint("MissingInflatedId")
    public void listaFolio(){
        txtProductoVi.setText("");
        txtContF.setText("");
        txtExistS.setText("");
        txtDif.setText("");
        txtUbb.setText("");
        txtCant.setText("");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_info_folios, null);
        builder.setView(dialogView);

        rvFolios =dialogView.findViewById(R.id.rvFolios);
        GridLayoutManager gl = new GridLayoutManager(this, 1);
        rvFolios.setLayoutManager(gl);

        AdaptadorListaFolios2 adapter = new AdaptadorListaFolios2(listaFol);
        rvFolios.setAdapter(null);
        rvFolios.setAdapter(adapter);

        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setButton(Dialog.BUTTON_NEGATIVE, "CANCELAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        dialog.show();
    }//listaFolio


    private class AsyncFolios extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {mDialog.show();}

        @Override
        protected Void doInBackground(Void... params) {
            listaFol = new ArrayList<>();
            conectaFolios();
            return null;
        }//doInBackground

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected void onPostExecute(Void result) {
            mDialog.dismiss();
            if (listaFol.size()>0) {
                listaFolio();
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDifUbiExi.this);
                builder.setMessage("No se encontró folios");
                builder.setCancelable(false);
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });//negative botton
                AlertDialog dialog = builder.create();
                dialog.show();
            }//else
        }//onPostExecute
    }//AsyncFolios


    private void conectaFolios() {
        String SOAP_ACTION = "Folios";
        String METHOD_NAME = "Folios";
        String NAMESPACE = "http://" + strServer + "/WSk75AlmacenesApp/";
        String URL = "http://" + strServer + "/WSk75AlmacenesApp";
        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
            XMLFolios soapEnvelope = new XMLFolios(SoapEnvelope.VER11);
            soapEnvelope.XMLFol(strusr, strpass,strbran,"1");//solo folios abiertos
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
                listaFol.add(new Folios((response0.getPropertyAsString("k_folio").equals("anyType{}")?"" : response0.getPropertyAsString("k_folio")),
                        (response0.getPropertyAsString("k_fecha").equals("anyType{}")?"" : response0.getPropertyAsString("k_fecha")),
                        (response0.getPropertyAsString("k_hora").equals("anyType{}")? "" : response0.getPropertyAsString("k_hora"))));
            }//for
        } catch (Exception ex) {}//catch
    }//conectaFolios


    private class AsyncDifUbiExist extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {mDialog.show();}

        @Override
        protected Void doInBackground(Void... params) {
            lista2.clear();
            conectaDifUbiExist();
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected void onPostExecute(Void result) {
            chbMan.setChecked(false);
            if (lista2.size()>0) {
                for(int i=0;i<lista2.size();i++){
                    insertarSql(lista2.get(i).getProducto(),lista2.get(i).getCantidad(),
                            lista2.get(i).getExistencia(),lista2.get(i).getDiferencia(),
                            lista2.get(i).getUbicacion(),lista2.get(i).getEstatus());
                }//for
                contados();
                mDialog.dismiss();
            }else{
                mDialog.dismiss();
                Toast.makeText(ActivityDifUbiExi.this, "Ningún dato", Toast.LENGTH_SHORT).show();
            }
            txtProducto.setText("");
        }//onPostExecute
    }//AsynInsertInv


    private void conectaDifUbiExist() {
        String SOAP_ACTION = "DifUbiExist";
        String METHOD_NAME = "DifUbiExist";
        String NAMESPACE = "http://" + strServer + "/WSk75AlmacenesApp/";
        String URL = "http://" + strServer + "/WSk75AlmacenesApp";
        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
            XMDifUbiExist soapEnvelope = new XMDifUbiExist(SoapEnvelope.VER11);
            soapEnvelope.XMLdif(strusr, strpass, folio);
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
                lista2.add(new DifUbiExist(
                        (i+1)+"",
                        (response0.getPropertyAsString("CLAVE").equals("anyType{}") ? " " : response0.getPropertyAsString("CLAVE")),
                        (response0.getPropertyAsString("CANTIDAD").equals("anyType{}") ? " " : response0.getPropertyAsString("CANTIDAD")),
                        (response0.getPropertyAsString("EXISTENCIA").equals("anyType{}") ? " " : response0.getPropertyAsString("EXISTENCIA")),
                        (response0.getPropertyAsString("DIFERENCIA").equals("anyType{}") ? " " : response0.getPropertyAsString("DIFERENCIA")),
                        (response0.getPropertyAsString("UBICACION").equals("anyType{}") ? " " : response0.getPropertyAsString("UBICACION")),
                        "0",(response0.getPropertyAsString("ESTATUS").equals("anyType{}") ? "" : response0.getPropertyAsString("ESTATUS"))));
            }//for
        } catch (Exception ex) {}//catch
    }//conectaListInv

    private class AsyncActualiza extends AsyncTask<Void, Integer, Void> {
        private String pro,cc,ubic;
        private int contador=0;
        @Override
        protected void onPreExecute() {progressDialog.show();}

        @Override
        protected Void doInBackground(Void... params) {
            progressDialog.setMax(lista2.size());
            for(int j=0;j<lista2.size();j++){//for para los registros de cada servidor
                try {
                    mensaje="";
                    pro=lista2.get(j).getProducto();
                    cc=lista2.get(j).getConteo();
                    ubic=lista2.get(j).getUbicacion();
                    conectaActualiza(pro,cc,ubic);
                    if(mensaje.equals("Actualizado")){
                        eliminarSql("AND PRODUCTO='"+pro+"' ");
                        contador++;
                    }//if
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    return null;
                }//catch
                progressDialog.setProgress(j);
            }//for
            return null;
        }//doinbackground

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress[0]);
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            if (contador==lista2.size()) {
                lista2.clear();
                rvDifUbiExi.setAdapter(null);
                editor.clear().commit();
                eliminarSql("");
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDifUbiExi.this);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AsyncFolios().execute();
                    }//onclick
                });//positivebutton
                builder.setCancelable(false);
                builder.setTitle("Resultado Sincronización").setMessage(contador+" Datos sincronizados").create().show();

            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityDifUbiExi.this);
                builder.setMessage("Error al sincronizar");
                builder.setCancelable(false);
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });//negative botton
                AlertDialog dialog = builder.create();
                dialog.show();
                contados();
            }//else
        }//onPostExecute
    }//AsynInsertInv


    private void conectaActualiza (String producto, String cont, String ubic) {
        String SOAP_ACTION = "ActualizaDif";
        String METHOD_NAME = "ActualizaDif";
        String NAMESPACE = "http://" + strServer + "/WSk75AlmacenesApp/";
        String URL = "http://" + strServer + "/WSk75AlmacenesApp";
        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
            XMLActualizaDif soapEnvelope = new XMLActualizaDif(SoapEnvelope.VER11);
            soapEnvelope.XMLAct(strusr, strpass, folio, strbran, producto,cont,ubic);
            soapEnvelope.dotNet = true;
            soapEnvelope.implicitTypes = true;
            soapEnvelope.setOutputSoapObject(Request);
            HttpTransportSE trasport = new HttpTransportSE(URL);
            trasport.debug = true;
            trasport.call(SOAP_ACTION, soapEnvelope);
            SoapObject response = (SoapObject) soapEnvelope.bodyIn;
            mensaje=response.getPropertyAsString("k_estado");
            //mensaje=(response.getPropertyAsString("k_estatus").equals("anyType{}") ? null : response.getPropertyAsString("k_estatus"));
        } catch (SoapFault soapFault) {
            mensaje=soapFault.getMessage();
        } catch (XmlPullParserException e) {
            mensaje=e.getMessage();
        } catch (IOException e) {
            mensaje=e.getMessage();
        } catch (Exception ex) {
            mensaje=ex.getMessage();
        }//catch
    }//conectaActualiza

    public void buscar(String prod,String canti,boolean sum){
        boolean band=false;
        int contA=0,cont=0,exist=0,dif=0;
        String ubi="";
        rvDifUbiExi.setAdapter(null);
        for(int i=0;i<lista2.size();i++){
            if(prod.equals(lista2.get(i).getProducto())){
                if(canti.equals("-1")){
                    canti=lista2.get(i).getConteo();
                }
                exist=Integer.parseInt(lista2.get(i).getExistencia());
                cont=Integer.parseInt(lista2.get(i).getConteo());
                ubi=lista2.get(i).getUbicacion();
                int op;
                if(sum==true){
                    op=cont+1;
                }else{
                    contA=Integer.parseInt(canti);
                    op=contA;
                }
                dif=exist-op;
                cont=op;
                actualizarSql(prod,cont+"",dif+"",ubi,exist+"");
                lista2.get(i).setConteo(cont+"");
                lista2.get(i).setDiferencia(dif+"");
                band=true;
                break;
            }//if
        }//for
        if (band==false){//si no existe el producto
            Toast.makeText(this, "Producto no existe en lista", Toast.LENGTH_SHORT).show();
            txtProducto.setText("");
            txtProductoVi.setText("");
            if(chbMan.isChecked()){
                txtProducto.requestFocus();
            }
        }
        consultaSql();
    }//buscar

    private class AsyncalListAlm extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mDialog.show();
        }//onPreejecute
        @Override
        protected Void doInBackground(Void... params) {
            mensaje="";
            listaAlm.clear();
            conectaAlma();
            return null;
        }//doInBackground
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        protected void onPostExecute(Void result) {
            mDialog.dismiss();
            if(listaAlm.size()>0){
                AlertDialog.Builder alert = new AlertDialog.Builder(ActivityDifUbiExi.this);
                LayoutInflater inflater = ActivityDifUbiExi.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_lista_almacenes, null);
                alert.setView(dialogView);
                alert.setCancelable(true);
                alert.setCancelable(false);
                alert.setNegativeButton("ACEPTAR",null);

                RecyclerView rvAlmacenes =  dialogView.findViewById(R.id.rvAlmacenes);
                GridLayoutManager gl = new GridLayoutManager(ActivityDifUbiExi.this, 1);
                rvAlmacenes.setLayoutManager(gl);

                adapter= new AdapterDifUbiExi(lista2);
                rvDifUbiExi.setAdapter(adapter);

                AdaptadorListAlmacenes adapAlm = new AdaptadorListAlmacenes(listaAlm);
                rvAlmacenes.setAdapter(adapAlm);
                AlertDialog mm = alert.create();
                mm.show();
            }else{
                AlertDialog.Builder alerta = new AlertDialog.Builder(ActivityDifUbiExi.this);
                alerta.setMessage("Sin Almacénes").setCancelable(false)
                        .setPositiveButton("Ok", null).setCancelable(false);//alertdialog
                AlertDialog titulo = alerta.create();
                titulo.setTitle("AVISO");
                titulo.show();
            }//else
            mensaje="";
        }//onPostExecute
    }//AsyncallUbicaciones

    private void conectaAlma() {
        String SOAP_ACTION = "ValidarEscanInv";
        String METHOD_NAME = "ValidarEscanInv";
        String NAMESPACE = "http://"+strServer+"/WSk75AlmacenesApp/";
        String URL = "http://"+strServer+"/WSk75AlmacenesApp";
        try {
            SoapObject Request = new SoapObject(NAMESPACE, METHOD_NAME);
            XMLValidEsc soapEnvelope = new XMLValidEsc(SoapEnvelope.VER11);
            soapEnvelope.XMLValid(strusr, strpass, txtProductoVi.getText().toString(), strbran);
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
                listaAlm.add(new Almacenes((response0.getPropertyAsString("k_Almacen").equals("anyType{}") ? " " : response0.getPropertyAsString("k_Almacen")),
                        (response0.getPropertyAsString("k_Descrip").equals("anyType{}") ? " " : response0.getPropertyAsString("k_Descrip")),
                        (response0.getPropertyAsString("k_Existencia").equals("anyType{}") ? " " : response0.getPropertyAsString("k_Existencia"))));
            }//for
        }catch (SoapFault soapFault) {
            mensaje = "Error: " + soapFault.getMessage();
        }catch (XmlPullParserException e) {
            mensaje = "Error: " + e.getMessage();
        }catch (IOException e) {
            mensaje = "No se encontró servidor";
        }catch (Exception ex) {
            mensaje ="Puede que la clave del producto no exista";
        }
    }//AsynCall


    public void onClickListDif(View v){//cada vez que se seleccione un producto en la lista
        posicion = rvDifUbiExi.getChildPosition(rvDifUbiExi.findContainingItemView(v));
        adapter.notifyItemChanged(posicion);

        //selectedPos = getLayoutPosition();
        //notifyItemChanged(selectedPos);
        /*adapter.index(posicion);
        adapter.notifyDataSetChanged();*/
        rvDifUbiExi.scrollToPosition(posicion);
        detalle(posicion);
    }//onClickLista

    public void detalle(int posi){//detalle del producto seleccionado
        ProductoAct=lista2.get(posi).getProducto();
        txtProductoVi.setText(ProductoAct);
        txtContF.setText(lista2.get(posi).getCantidad());
        txtExistS.setText(lista2.get(posi).getExistencia());
        txtDif.setText(lista2.get(posi).getDiferencia());
        txtUbb.setText(lista2.get(posi).getUbicacion());
        txtCant.setText(lista2.get(posi).getConteo());
    }//detalle
    public void detalleProd(String prod){//se busca el detalle en todos lo productos ya sea contados o no contados
        for(int i=0;i<lista2.size();i++){
            if(lista2.get(i).getProducto().equals(prod)){
                txtContF.setText(lista2.get(i).getCantidad());
                txtExistS.setText(lista2.get(i).getExistencia());
                txtDif.setText(lista2.get(i).getDiferencia());
                txtUbb.setText(lista2.get(i).getUbicacion());
                txtCant.setText(lista2.get(i).getConteo());
                break;
            }
        }
    }//detalleLista2

    public void consultaSql(){
        try{
            boolean var=true;
            lista2.clear();
            int j=0;
            rvDifUbiExi.setAdapter(null);
            posicion=0;
            @SuppressLint("Recycle") Cursor fila = db.rawQuery("SELECT PRODUCTO,CANTIDAD,EXISTENCIA,DIFERENCIA,"+
                    "UBICACION,CONTEO,ESTATUS FROM DIFUBIEXIST WHERE EMPRESA='"+serv+"' "+where+" ORDER BY UBICACION,PRODUCTO ", null);
            if (fila != null && fila.moveToFirst()) {
                do {
                    j++;
                    if(ProductoAct.equals(fila.getString(0))){
                        posicion=j-1;
                    }
                    lista2.add(new DifUbiExist(j+"",fila.getString(0),fila.getString(1),fila.getString(2),
                            fila.getString(3),fila.getString(4),fila.getString(5),fila.getString(6)));
                } while (fila.moveToNext());
                adapter= new AdapterDifUbiExi(lista2);
                rvDifUbiExi.setAdapter(adapter);
            }//if
            if(!ProductoAct.equals("")){
                detalleProd(ProductoAct);
            }
            fila.close();
        }catch(Exception e){
            Toast.makeText(ActivityDifUbiExi.this,
                    "Error al consultar datos de la base de datos interna", Toast.LENGTH_SHORT).show();
        }//catch
    }//consultaSql

    public void insertarSql(String prod,String cant,String exist,String dif,String ubi,String estatus){
        try{
            if(db != null){
                ContentValues valores = new ContentValues();
                valores.put("PRODUCTO", prod);
                valores.put("CANTIDAD", cant);
                valores.put("EXISTENCIA", exist);
                valores.put("DIFERENCIA", dif);
                valores.put("UBICACION", ubi);
                valores.put("CONTEO", "0");
                valores.put("EMPRESA", serv);
                valores.put("ESTATUS", estatus);
                db.insert("DIFUBIEXIST", null, valores);
            }
        }catch(Exception e){
            Toast.makeText(this, "Problema al guardar producto", Toast.LENGTH_SHORT).show();
        }
    }//insertarSql

    public void actualizarSql(String prod,String cant,String dif,String ubi,String exist){
        try{
            ContentValues valores = new ContentValues();
            valores.put("CONTEO", Integer.parseInt(cant));
            valores.put("UBICACION", ubi);
            valores.put("EXISTENCIA", exist);
            valores.put("DIFERENCIA", Integer.parseInt(dif));
            db.update("DIFUBIEXIST", valores, "PRODUCTO='"+prod+"' AND EMPRESA='"+serv+"'", null);
        }catch(Exception e){
            Toast.makeText(this, "Problema al actualizar la cantidad del producto", Toast.LENGTH_SHORT).show();
        }
    }//actualizarSql

    public void eliminarSql(String sentProd) {//parte de sentencia que es para eliminar prod o todos los productos
        try{
            SQLiteDatabase db = conn.getWritableDatabase();
            db.delete("DIFUBIEXIST"," EMPRESA='"+serv+"' "+sentProd,null);
        }catch(Exception e){}
    }//eliminarSql

}//ActivityInventario

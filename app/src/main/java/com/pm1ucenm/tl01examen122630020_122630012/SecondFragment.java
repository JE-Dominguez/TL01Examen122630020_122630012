package com.pm1ucenm.tl01examen122630020_122630012;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.pm1ucenm.tl01examen122630020_122630012.Configuraciones.SQLiteConexion;
import com.pm1ucenm.tl01examen122630020_122630012.Configuraciones.Transacciones;
import com.pm1ucenm.tl01examen122630020_122630012.databinding.FragmentSecondBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private static final int PERMISO_CAMARA = 101;
    private File archivoFoto;
    private String fotoBase64;
    private String fotoBase644;
    private boolean modoEdicion = false;
    private int idContacto = -1;
    private ActivityResultLauncher<Intent> lanzarCamara;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        inicializarLanzadorCamara();
        inicializarSpinnerPais();
        cargarDatosSiEditando();
        binding.btnfoto.setOnClickListener(v -> verificarPermisosCamara());
        binding.guardar.setOnClickListener(v -> guardarContacto());
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        liberarBitmapAnterior();
        binding = null;
    }

    private void inicializarSpinnerPais() {
        Spinner spinnerPais = binding.spinnerPais;
        String[] paises = {"Honduras", "El Salvador", "Guatemala", "Nicaragua", "Costa Rica"};
        String[] codigos = {"504", "503", "502", "505", "506"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, paises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPais.setAdapter(adapter);
        spinnerPais.setTag(codigos);
    }

    private void cargarDatosSiEditando() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("id")) {
            modoEdicion = true;
            idContacto = args.getInt("id", -1);
            binding.nombre.setText(args.getString("nombre"));
            binding.telefono.setText(args.getString("telefono"));
            binding.nota.setText(args.getString("nota"));
            String codigoPais = args.getString("pais");
            seleccionarPaisEnSpinner(codigoPais);
            String imagen = args.getString("imagen");
            if (imagen != null && !imagen.isEmpty()) {
                fotoBase644 = imagen;
                mostrarFotoBase64(imagen);
            } else {
                binding.foto.setImageResource(R.drawable.ic_contact);
            }
        } else {
            binding.foto.setImageResource(R.drawable.ic_contact);
        }
    }

    private void seleccionarPaisEnSpinner(String codigoPais) {
        if (codigoPais == null) return;
        String[] codigos = (String[]) binding.spinnerPais.getTag();
        for (int i = 0; i < codigos.length; i++) {
            if (codigos[i].equals(codigoPais)) {
                binding.spinnerPais.setSelection(i);
                return;
            }
        }
    }

    private void inicializarLanzadorCamara() {
        lanzarCamara = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultado -> {
                    try {
                        if (resultado.getResultCode() == getActivity().RESULT_OK &&
                                archivoFoto != null && archivoFoto.exists() &&
                                archivoFoto.length() > 0) {

                            procesarFoto();

                        } else {
                            if (modoEdicion && idContacto != -1) {
                                cargarDatosSiEditando();
                                alerta("Aviso", "No se tomó foto nueva");
                            } else {
                                alerta("Aviso", "No se tomó foto");
                            }
                        }
                    } catch (Exception e) {
                        archivoFoto = null;
                        alerta("Error", "Error al regresar de la cámara");
                    }
                }
        );
    }

    private void verificarPermisosCamara() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA);
        } else {
            abrirCamara();
        }
    }

    private void abrirCamara() {
        try {
            liberarBitmapAnterior();
            File directorio = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            archivoFoto = new File(directorio, "foto_" + System.currentTimeMillis() + ".jpg");
            Uri fotoUri = FileProvider.getUriForFile(getContext(),
                    "com.pm1ucenm.tl01examen122630020_122630012.provider", archivoFoto);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                lanzarCamara.launch(intent);
            }
        } catch (Exception ex) {
            archivoFoto = null;
        }
    }

    private void procesarFoto() {
        try {
            Bitmap bitmapFoto = BitmapFactory.decodeFile(archivoFoto.getAbsolutePath());
            ExifInterface exif = new ExifInterface(archivoFoto.getAbsolutePath());
            int orientacion = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotacion = exifAGrados(orientacion);

            if (rotacion != 0) {
                Matrix matrix = new Matrix();
                matrix.preRotate(rotacion);
                bitmapFoto = Bitmap.createBitmap(bitmapFoto, 0, 0,
                        bitmapFoto.getWidth(), bitmapFoto.getHeight(), matrix, true);
            }

            binding.foto.setImageBitmap(bitmapFoto);
            fotoBase64 = convertirBitmapABase64(bitmapFoto);

        } catch (Exception e) {
            e.printStackTrace();
            alerta("Error", "Error al procesar la foto");
        }
    }

    private int exifAGrados(int exifOrientacion) {
        switch (exifOrientacion) {
            case ExifInterface.ORIENTATION_ROTATE_90: return 90;
            case ExifInterface.ORIENTATION_ROTATE_180: return 180;
            case ExifInterface.ORIENTATION_ROTATE_270: return 270;
            default: return 0;
        }
    }

    private void mostrarFotoBase64(String base64) {
        if (base64 == null || base64.isEmpty()) return;
        liberarBitmapAnterior();
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opciones);
        if (bitmap != null) binding.foto.setImageBitmap(bitmap);
        else binding.foto.setImageResource(R.drawable.ic_contact);
    }

    private String convertirBitmapABase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private void liberarBitmapAnterior() {
        if (binding == null) return;
        if (binding.foto.getDrawable() instanceof android.graphics.drawable.BitmapDrawable) {
            android.graphics.drawable.BitmapDrawable drawable = (android.graphics.drawable.BitmapDrawable) binding.foto.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) bitmap.recycle();
        }
    }

    private void guardarContacto() {
        String nombre = binding.nombre.getText().toString().trim();
        String telefono = binding.telefono.getText().toString().trim();
        String nota = binding.nota.getText().toString().trim();
        String[] codigos = (String[]) binding.spinnerPais.getTag();
        String codigoPais = codigos[binding.spinnerPais.getSelectedItemPosition()];

        if (nombre.isEmpty()) {
            alerta("Campo requerido", "Debes ingresar un nombre para el contacto.");
            return;
        }

        if (telefono.isEmpty()) {
            alerta("Campo requerido", "Debes ingresar un número de teléfono.");
            return;
        }

        if (nota.isEmpty()) {
            alerta("Campo requerido", "Debes escribir una nota para el contacto.");
            return;
        }

        String mensajeConfirmacion = modoEdicion
                ? "¿Deseas actualizar este contacto?"
                : "¿Deseas guardar este nuevo contacto?";

        new AlertDialog.Builder(getContext())
                .setTitle("Confirmar acción")
                .setMessage(mensajeConfirmacion)
                .setPositiveButton("Sí", (dialog, which) -> {
                    ejecutarGuardado(nombre, telefono, nota, codigoPais);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void ejecutarGuardado(String nombre, String telefono, String nota, String codigoPais) {
        SQLiteConexion conexion = null;
        SQLiteDatabase db = null;

        try {
            conexion = new SQLiteConexion(getContext(), Transacciones.DBNAME, null, 1);
            db = conexion.getWritableDatabase();

            String query;
            String[] args;

            if (modoEdicion && idContacto != -1) {
                query = "SELECT COUNT(*) FROM " + Transacciones.TableContactos +
                        " WHERE " + Transacciones.telefono + "=? AND " + Transacciones.id + "!=?";
                args = new String[]{telefono, String.valueOf(idContacto)};
            } else {
                query = "SELECT COUNT(*) FROM " + Transacciones.TableContactos +
                        " WHERE " + Transacciones.telefono + "=?";
                args = new String[]{telefono};
            }

            Cursor cursor = db.rawQuery(query, args);
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                cursor.close();
                alerta("Número duplicado", "El número ingresado ya existe en tus contactos.");
                return;
            }
            cursor.close();

            ContentValues valores = new ContentValues();
            valores.put(Transacciones.pais, codigoPais);
            valores.put(Transacciones.nombre, nombre);
            valores.put(Transacciones.telefono, telefono);
            valores.put(Transacciones.nota, nota);

            String imagenAGuardar = fotoBase64 != null ? fotoBase64 :
                    fotoBase644 != null ? fotoBase644 : "";
            valores.put(Transacciones.imagen, imagenAGuardar);

            if (modoEdicion && idContacto != -1) {
                int filas = db.update(
                        Transacciones.TableContactos,
                        valores,
                        Transacciones.id + "=?",
                        new String[]{String.valueOf(idContacto)}
                );

                if (filas > 0) {
                    alerta("Éxito", "Contacto actualizado correctamente");
                    limpiarCampos();
                } else {
                    alerta("Error", "No se pudo actualizar el contacto. Intenta nuevamente.");
                }
            } else {
                long idInsertado = db.insert(Transacciones.TableContactos, null, valores);
                if (idInsertado != -1) {
                    alerta("Éxito", "Contacto guardado correctamente");
                    limpiarCampos();
                } else {
                    alerta("Error", "No se pudo guardar el contacto. Intenta nuevamente.");
                }
            }

        } catch (Exception e) {
            alerta("Error inesperado", "Ocurrió un error al guardar: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) db.close();
            if (conexion != null) conexion.close();
        }
    }

    private void alerta(String titulo, String mensaje) {
        new AlertDialog.Builder(getContext())
                .setTitle(titulo)
                .setMessage(mensaje)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void limpiarCampos() {
        binding.nombre.setText("");
        binding.telefono.setText("");
        binding.nota.setText("");
        binding.foto.setImageResource(R.drawable.ic_contact);
        liberarBitmapAnterior();
        fotoBase64 = null;
        fotoBase644 = null;
        binding.spinnerPais.setSelection(0);
        modoEdicion = false;
        idContacto = -1;
        if (archivoFoto != null && archivoFoto.exists()) archivoFoto.delete();
        archivoFoto = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permisos,
                                           @NonNull int[] resultados) {
        super.onRequestPermissionsResult(requestCode, permisos, resultados);
        if (requestCode == PERMISO_CAMARA && resultados.length > 0 && resultados[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        }
    }
}

package com.pm1ucenm.tl01examen122630020_122630012;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.pm1ucenm.tl01examen122630020_122630012.Configuraciones.SQLiteConexion;
import com.pm1ucenm.tl01examen122630020_122630012.Configuraciones.Transacciones;
import com.pm1ucenm.tl01examen122630020_122630012.databinding.FragmentSecondBinding;

import java.io.File;
import java.io.ByteArrayOutputStream;

public class SecondFragment  extends Fragment {

    private FragmentSecondBinding binding;

    private static final int PERMISO_CAMARA = 101;
    private File archivoFoto;
    private String fotoBase64 = null;

    private ActivityResultLauncher<android.content.Intent> lanzarCamara;

    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                          android.view.ViewGroup container,
                                          Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);

        // Inicializar ActivityResultLauncher para la cámara
        lanzarCamara = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultado -> {
                    if (resultado.getResultCode() == getActivity().RESULT_OK
                            && archivoFoto != null
                            && archivoFoto.exists()) {
                        procesarFoto();
                    }
                }
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inicializarSpinnerPais();

        binding.btnfoto.setOnClickListener(v -> verificarPermisosCamara());

        binding.guardar.setOnClickListener(v -> guardarContacto());
    }

    // --------------------------- SPINNER ---------------------------
    private void inicializarSpinnerPais() {
        Spinner spinnerPais = binding.spinnerPais;
        String[] paises = {"Honduras", "El Salvador", "Guatemala", "Nicaragua", "Costa Rica"};
        int[] codigos = {504, 503, 502, 505, 506};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                paises
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPais.setAdapter(adapter);

        // Guardamos los códigos en el tag para acceder luego
        binding.spinnerPais.setTag(codigos);
    }

    // --------------------------- CÁMARA ---------------------------
    private void verificarPermisosCamara() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA);
        } else {
            abrirCamara();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permisos,
                                           @NonNull int[] resultados) {
        super.onRequestPermissionsResult(requestCode, permisos, resultados);
        if (requestCode == PERMISO_CAMARA) {
            if (resultados.length > 0 && resultados[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(getContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void abrirCamara() {
        try {
            archivoFoto = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "foto_" + System.currentTimeMillis() + ".jpg");
            Uri fotoUri = FileProvider.getUriForFile(getContext(),
                    "com.pm1ucenm.tl01examen122630020_122630012.provider", archivoFoto);

            android.content.Intent intent = new android.content.Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
            lanzarCamara.launch(intent);

        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(getContext(), "Error al abrir cámara: " + ex.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(getContext(), "Error al procesar la foto", Toast.LENGTH_LONG).show();
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

    // --------------------------- GUARDADO ---------------------------
    private void guardarContacto() {
        String nombre = binding.nombre.getText().toString().trim();
        String telefono = binding.telefono.getText().toString().trim();
        String nota = binding.nota.getText().toString().trim();

        int[] codigos = (int[]) binding.spinnerPais.getTag();
        String codigoPais = String.valueOf(codigos[binding.spinnerPais.getSelectedItemPosition()]);

        SQLiteConexion conexion = new SQLiteConexion(getContext(), Transacciones.DBNAME, null, 1);
        SQLiteDatabase db = conexion.getWritableDatabase();

        try {
            ContentValues valores = new ContentValues();
            valores.put(Transacciones.pais, codigoPais);
            valores.put(Transacciones.nombre, nombre);
            valores.put(Transacciones.telefono, telefono);
            valores.put(Transacciones.nota, nota);
            valores.put(Transacciones.imagen, fotoBase64 != null ? fotoBase64 : "");

            long resultado = db.insert(Transacciones.TableContactos, null, valores);

            if (resultado > 0) {
                Toast.makeText(getContext(), "Contacto guardado correctamente", Toast.LENGTH_SHORT).show();
                limpiarCampos();
            } else {
                Toast.makeText(getContext(), "Error al guardar el contacto", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
            conexion.close();
        }
    }

    private void limpiarCampos() {
        binding.nombre.setText("");
        binding.telefono.setText("");
        binding.nota.setText("");
        binding.foto.setImageBitmap(null);
        fotoBase64 = null;
        binding.spinnerPais.setSelection(0);
    }

    private String convertirBitmapABase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] arrayBytes = baos.toByteArray();
        return android.util.Base64.encodeToString(arrayBytes, android.util.Base64.DEFAULT);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

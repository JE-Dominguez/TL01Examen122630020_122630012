package com.pm1ucenm.tl01examen122630020_122630012;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pm1ucenm.tl01examen122630020_122630012.Adaptadores.ContactosAdapter;
import com.pm1ucenm.tl01examen122630020_122630012.Configuraciones.SQLiteConexion;
import com.pm1ucenm.tl01examen122630020_122630012.Configuraciones.Transacciones;
import com.pm1ucenm.tl01examen122630020_122630012.Modelos.Contacto;
import com.pm1ucenm.tl01examen122630020_122630012.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private RecyclerView recyclerView;
    private List<Contacto> listaContactos;
    private ContactosAdapter contactosAdapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        inicializarRecyclerView();
        obtenerContactosDB();

        return root;
    }
    public void actualizarMensajeVacio() {
        if (listaContactos.isEmpty()) {
            binding.contactListRecyclerView.setVisibility(View.GONE);
            binding.textoVacio.setVisibility(View.VISIBLE);
        } else {
            binding.contactListRecyclerView.setVisibility(View.VISIBLE);
            binding.textoVacio.setVisibility(View.GONE);
        }
    }


    private void inicializarRecyclerView() {
        recyclerView = binding.contactListRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        listaContactos = new ArrayList<>();
        contactosAdapter = new ContactosAdapter(getContext(), listaContactos);
        recyclerView.setAdapter(contactosAdapter);
    }

    private void obtenerContactosDB() {
        SQLiteConexion conexion = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            conexion = new SQLiteConexion(getContext(), Transacciones.DBNAME, null, 1);
            db = conexion.getReadableDatabase();
            listaContactos.clear();

            cursor = db.rawQuery(Transacciones.SELECTALLCONTACTOS, null);

            if (cursor.moveToFirst()) {
                do {
                    Contacto contacto = new Contacto();
                    contacto.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Transacciones.id)));
                    contacto.setPais(cursor.getString(cursor.getColumnIndexOrThrow(Transacciones.pais)));
                    contacto.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(Transacciones.nombre)));
                    contacto.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(Transacciones.telefono)));
                    contacto.setNota(cursor.getString(cursor.getColumnIndexOrThrow(Transacciones.nota)));
                    contacto.setImagen(cursor.getString(cursor.getColumnIndexOrThrow(Transacciones.imagen)));
                    listaContactos.add(contacto);
                } while (cursor.moveToNext());
            }

            if (contactosAdapter != null) {
                contactosAdapter.notifyDataSetChanged();
            }
            actualizarMensajeVacio();

        } catch (Exception e) {
            alerta("Error al cargar contactos: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
    }

    private void alerta(String mensaje) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Aviso")
                .setMessage(mensaje)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

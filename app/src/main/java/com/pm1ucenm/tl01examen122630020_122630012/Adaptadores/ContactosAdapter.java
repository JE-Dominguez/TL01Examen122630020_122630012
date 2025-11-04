package com.pm1ucenm.tl01examen122630020_122630012.Adaptadores;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pm1ucenm.tl01examen122630020_122630012.Configuraciones.SQLiteConexion;
import com.pm1ucenm.tl01examen122630020_122630012.Configuraciones.Transacciones;
import com.pm1ucenm.tl01examen122630020_122630012.Modelos.Contacto;
import com.pm1ucenm.tl01examen122630020_122630012.FirstFragment;
import com.pm1ucenm.tl01examen122630020_122630012.R;

import java.util.ArrayList;
import java.util.List;

public class ContactosAdapter extends RecyclerView.Adapter<ContactosAdapter.ContactoViewHolder> {

    private final Context context;
    private final List<Contacto> contactosList;

    public ContactosAdapter(Context context, List<Contacto> contactosList) {
        this.context = context;
        this.contactosList = contactosList != null ? contactosList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ContactoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_card, parent, false);
        return new ContactoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactoViewHolder holder, int position) {
        Contacto contacto = contactosList.get(position);

        holder.contactName.setText(contacto.getNombre());
        holder.contactPhone.setText(String.format("(%s) %s", "+" + contacto.getPais(), contacto.getTelefono()));
        holder.contactImage.setImageResource(R.drawable.ic_contact);

        String base64 = contacto.getImagen();
        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                if (bitmap != null) holder.contactImage.setImageBitmap(bitmap);
            } catch (Exception ignored) {}
        }

        setupItemClickListeners(holder, contacto);
    }

    @Override
    public int getItemCount() {
        return contactosList.size();
    }

    public static class ContactoViewHolder extends RecyclerView.ViewHolder {
        public final CardView cardView;
        public final ImageView contactImage;
        public final TextView contactName;
        public final TextView contactPhone;
        public final LinearLayout contactActions;
        public final ImageButton buttonCall, buttonEdit, buttonVer, buttonCompartir, buttonEliminar;

        public ContactoViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.contact_card);
            contactImage = itemView.findViewById(R.id.contact_image);
            contactName = itemView.findViewById(R.id.contact_name);
            contactPhone = itemView.findViewById(R.id.contact_phone);
            contactActions = itemView.findViewById(R.id.contact_actions);
            buttonCall = itemView.findViewById(R.id.button_call);
            buttonEdit = itemView.findViewById(R.id.button_edit);
            buttonVer = itemView.findViewById(R.id.button_ver);
            buttonCompartir = itemView.findViewById(R.id.button_compartir);
            buttonEliminar = itemView.findViewById(R.id.button_eliminar);
        }
    }

    private void setupItemClickListeners(ContactoViewHolder holder, Contacto contacto) {
        holder.cardView.setOnClickListener(v ->
                holder.contactActions.setVisibility(
                        holder.contactActions.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));

        holder.buttonCall.setOnClickListener(v -> realizarLlamada(contacto));
        holder.buttonEdit.setOnClickListener(v -> confirmarEdicion(v, contacto));
        holder.buttonVer.setOnClickListener(v -> mostrarImagenGrande(holder.contactImage));
        holder.buttonCompartir.setOnClickListener(v -> confirmarCompartir(contacto));
        holder.buttonEliminar.setOnClickListener(v -> eliminarContacto(contacto));
    }

    private void realizarLlamada(Contacto contacto) {
        String phoneNumber = "+" + contacto.getPais() + contacto.getTelefono();

        if (phoneNumber.trim().isEmpty()) {
            alerta("Número inválido");
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(context)
                    .setTitle("Confirmar llamada")
                    .setMessage("¿Deseas llamar a " + contacto.getNombre() + " al número " + phoneNumber + "?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else if (context instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CALL_PHONE}, 101);
        } else {
            alerta("No se pudo solicitar el permiso");
        }
    }

    private void confirmarEdicion(View v, Contacto contacto) {
        new AlertDialog.Builder(context)
                .setTitle("Editar contacto")
                .setMessage("¿Deseas editar a " + contacto.getNombre() + "?")
                .setPositiveButton("Sí", (dialog, which) -> editarContacto(v, contacto))
                .setNegativeButton("No", null)
                .show();
    }

    private void editarContacto(View v, Contacto contacto) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", contacto.getId());
        bundle.putString("nombre", contacto.getNombre());
        bundle.putString("telefono", contacto.getTelefono());
        bundle.putString("nota", contacto.getNota());
        bundle.putString("pais", contacto.getPais());
        bundle.putString("imagen", contacto.getImagen());

        androidx.navigation.NavController navController =
                androidx.navigation.Navigation.findNavController(v);
        navController.navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
    }

    private void confirmarCompartir(Contacto contacto) {
        new AlertDialog.Builder(context)
                .setTitle("Compartir contacto")
                .setMessage("¿Deseas compartir la información de " + contacto.getNombre() + "?")
                .setPositiveButton("Sí", (dialog, which) -> compartirContacto(contacto))
                .setNegativeButton("No", null)
                .show();
    }

    private void compartirContacto(Contacto contacto) {
        String texto = "Nombre: " + contacto.getNombre() +
                "\nTeléfono: +" + contacto.getPais() + contacto.getTelefono() +
                (contacto.getNota() != null && !contacto.getNota().isEmpty() ? "\nNota: " + contacto.getNota() : "");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, texto);
        context.startActivity(Intent.createChooser(shareIntent, "Compartir contacto"));
    }

    private void eliminarContacto(Contacto contacto) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar contacto")
                .setMessage("¿Deseas eliminar a " + contacto.getNombre() + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    SQLiteConexion conexion = new SQLiteConexion(context, Transacciones.DBNAME, null, 1);
                    SQLiteDatabase db = conexion.getWritableDatabase();
                    try {
                        int filas = db.delete(Transacciones.TableContactos, Transacciones.id + "=?",
                                new String[]{String.valueOf(contacto.getId())});
                        if (filas > 0) {
                            contactosList.remove(contacto);
                            notifyDataSetChanged();
                            alerta("Contacto eliminado");
                        } else alerta("Error al eliminar contacto");
                    } catch (Exception e) {
                        alerta("Error: " + e.getMessage());
                    } finally {
                        db.close();
                        conexion.close();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void mostrarImagenGrande(ImageView imageView) {
        Bitmap bitmap = ((android.graphics.drawable.BitmapDrawable) imageView.getDrawable()).getBitmap();
        if (bitmap == null) {
            alerta("No hay imagen disponible");
            return;
        }

        ImageView imageViewGrande = new ImageView(context);
        imageViewGrande.setImageBitmap(bitmap);
        imageViewGrande.setAdjustViewBounds(true);
        imageViewGrande.setScaleType(ImageView.ScaleType.FIT_CENTER);

        new AlertDialog.Builder(context)
                .setView(imageViewGrande)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void alerta(String mensaje) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
    }

    public void updateList(List<Contacto> newList) {
        contactosList.clear();
        contactosList.addAll(newList);
        notifyDataSetChanged();
    }
}

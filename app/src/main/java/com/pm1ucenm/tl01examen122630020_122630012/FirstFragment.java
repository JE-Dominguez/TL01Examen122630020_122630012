package com.pm1ucenm.tl01examen122630020_122630012;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.pm1ucenm.tl01examen122630020_122630012.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState


    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);

        // ================= INSTANCIAS =================
        CardView contactCard1 = binding.contactCard1;
        LinearLayout contactActions1 = binding.contactActions1;
        ImageButton buttonCall1 = binding.buttonCall1;
        ImageButton buttonEdit1 = binding.buttonEdit1;

        // ================= CONFIGURACIONES =================
        setupCardClick(contactCard1, contactActions1);
        setupCallButton(buttonCall1, "+50412345678"); // reemplaza con el número real
        setupEditButton(buttonEdit1);

        return binding.getRoot();


    }

    private void setupCardClick(final CardView card, final LinearLayout actions) {
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actions.getVisibility() == View.GONE) {
                    actions.setVisibility(View.VISIBLE);
                } else {
                    actions.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupCallButton(ImageButton button, final String phoneNumber) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                // Verificar permiso
                if (getActivity().checkSelfPermission(android.Manifest.permission.CALL_PHONE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                } else {
                    // Solicitar permiso si no está concedido
                    requestPermissions(new String[]{android.Manifest.permission.CALL_PHONE}, 1);
                }
            }
        });
    }


    private void setupEditButton(ImageButton button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes abrir un diálogo o Activity para editar
                Toast.makeText(getContext(), "Editar contacto", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
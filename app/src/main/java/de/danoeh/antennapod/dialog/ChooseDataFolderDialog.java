package de.danoeh.antennapod.dialog;

import android.content.Context;

import android.view.LayoutInflater;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.LinearLayoutManager;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.adapter.DataFolderAdapter;
import de.danoeh.antennapod.databinding.ChooseDataFolderDialogBinding;

public class ChooseDataFolderDialog {

    public static void showDialog(final Context context, Consumer<String> handlerFunc) {
        final ChooseDataFolderDialogBinding binding =
                ChooseDataFolderDialogBinding.inflate(LayoutInflater.from(context));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot())
                .setTitle(R.string.choose_data_directory)
                .setMessage(R.string.choose_data_directory_message)
                .setNegativeButton(R.string.cancel_label, null)
                .create();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context));

        DataFolderAdapter adapter = new DataFolderAdapter(context, path -> {
            dialog.dismiss();
            handlerFunc.accept(path);
        });
        binding.recyclerView.setAdapter(adapter);

        if (adapter.getItemCount() > 0) {
            dialog.show();
        } else {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.error_label)
                    .setMessage(R.string.external_storage_error_msg)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

}
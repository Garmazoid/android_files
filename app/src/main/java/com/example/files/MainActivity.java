package com.example.files;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "===== MainActivity";

    private ListView lvFiles;

    private ArrayAdapter<String> adapter;

    private File esCurDir;

    private AlertDialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Разрешения нет, запросим это разрешение у пользователя");

            ActivityCompat.requestPermissions(this,
                                                new String[] {
                                                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                                        android.permission.WRITE_EXTERNAK_STORAGE
                                                }, 1);
        }

        /*
        if (this.isExternalStorageWritable()) {
            File esManager = Environment.getExternalStorageDirectory();
            Log.d(MainActivity.TAG, "Путь к каталогу внешнего носителя: " + esMainDir.getAbsolute);
        }
        */

        this.esCurDir = Environment.getExternalStorageDirectory();
        ArrayAdapter<String> listFiles = this.fillDirectory(this.esCurDir);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.esCurDir.getAbsolutePath());
        this.lvFiles = new ListView(this);
        this.adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, listFiles);
        this.lvFiles.setAdapter(adapter);

        this.lvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String item = MainActivity.this.adapter.getItem(position);
                item = item.replaceAll("[\\[\\]]", "");

                File fileObj = (item.compareTo("..") == 0) ?
                        (MainActivity.this.esCurDir.getParentFile()) :
                        (new File(MainActivity.this.esCurDir, item));

                if (fileObj.isDirectory()) {
                    MainActivity.this.adapter.clear();

                    ArrayList<String> lastFiles = MainActivity.this.fillDirectory(fileObj);

                    if (fileObj.compareTo(Environment.getExternalStorageDirectory()) != 0){
                        MainActivity.this.adapter.add("[..]");
                    }

                    MainActivity.this.adapter.addAll(listFiles);
                    MainActivity.this.esCurDir = fileObj;
                    MainActivity.this.dialog.setTitle(MainActivity.this.esCurDir.getAbsolutePath());
                } else if (fileObj.isFile()) {
                    try {
                        LineNumberReader LR = new LineNumberReader(new FileReader(fileObj));
                        String S = "";
                        while(true) {
                            String z = LR.readLine();
                            if (z == null) break;
                            S += z + "\n";
                        }
                        LR.close();

                        EditText etEditFile = (EditText) MainActivity.this.findViewById(R.id.etEditFile);
                        etEditFile.setText(S);

                        MainActivity.this.esCurDir = fileObj;
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this,
                                "Ошибка открытия файла : \n" +
                                        e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.d(MainActivity.TAG,
                                "Ошибкаоткрытия файла : " + e.getMessage());
                    }

                    MainActivity.this.dialog.dismiss();

                    Toast.makeText(MainActivity.this, fileObj.getAbsolutePath(), Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setView(this.lvFiles);

        builder.setNegativeButton("Закрыть", null);

        this.dialog = builder.create();
    }






    public void btnListFilesClick(View v) {
        if (this.isExternalStorageReadable()) {

            File esMainDir = Environment.getExternalStorageDirectory();

            Log.d(MainActivity.TAG, "Путь к каталогу внешнего носителя : " +
                    esMainDir.getAbsolutePath());

            ArrayList<String> listFiles = new ArrayList<>();
            File[] arrFiles = esMainDir.listFiles();
            if (arrFiles != null) {
                for (File f : arrFiles) {
                    if (f.isDirectory()) {
                        listFiles.add("[" + f.getName() + "]");
                    } else {
                        listFiles.add(f.getName());
                    }
                }
            } else {
                Toast.makeText(this, "Каталог внешнего носителя пуст!", Toast.LENGTH_SHORT).show();
            }

            for (int i = 0; i < listFiles.size(); i++){
                Log.d(TAG,  listFiles.get(i));
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    this, android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle("Список файлов и каталогов внешнего носителя");

            ListView LV = new ListView(this);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_list_item_1, listFiles);
            LV.setAdapter(adapter);

            builder.setView(LV);

            builder.setNegativeButton("Закрыть", null);

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Toast.makeText(this, "Ошибка: Внешний носитель не готов!", Toast.LENGTH_SHORT).show();
        }
    }





    public void btnCreateFileClick(View v) {
        if (this.isExternalStorageWritable()) {
            File esMainDir = Environment.getExternalStorageDirectory();

            ArrayList<String> listFiles = this.fillDirectory(esMainDir);

            AlertDialog.Builder builder= new AlertDialog.Builder(
                    this, android.R.style.Theme_Holo_Light_Dialog);
            builder.setTitle("Создать файл");

            LayoutInflater inflater = this.getLayoutInflater();
            View view = inflater.inflate(R.layout.create_file_dialog_content,
                    null, false);

            ArrayAdapter<String> A = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_single_choice, listFiles);

            ListView lvDirList = (ListView) view.findViewById(R.id.lvDirList);
            lvDirList.setAdapter(A);

            lvDirList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            builder.setView(view);

            builder.setPositiveButton("Создать", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    File esMainDir = Environment.getExternalStorageDirectory();

                    ListView lvDirList = (ListView)
                            ((AlertDialog) dialog).findViewById(R.id.lvDirList);

                    int index = lvDirList.getCheckedItemPosition();
                    String strDir = "";
                    if (index != -1) {
                        strDir = lvDirList.getAdapter().getItem(index).toString();
                        strDir = strDir.replace("[\\[\\]]", "");
                    }
                    File dir = (strDir.isEmpty()) ?
                            esMainDir : (new File(esMainDir, strDir));

                    EditText etFileName = (EditText)
                            ((AlertDialog) dialog).findViewById(R.id.etFileName);
                    String strFileName = etFileName.getText().toString();
                    if (strFileName.isEmpty()) {
                        strFileName = "noname" + ((int)(Math.random() * 100)) + ".txt";
                    }
                }
            })
        }

    }





    private ArrayList<String> fillDirectory(File dir){
        ArrayList<String> listFiles = new ArrayList<>();

        if (this.isExternalStorageReadable()) {
            File[] arrFiles = dir.listFiles();
            if (arrFiles != null) {
                for (File f : arrFiles) {
                    if (f.isDirectory()) {
                        listFiles.add("[" + f.getName() + "]");
                    } else {
                        listFiles.add(f.getName());
                    }
                }
            }
        }
        return listFiles;
    }





    public boolean isExternalStorageWritable(){

    }

    public boolean isExternalStorageReadable(){
        String state = Environment.getExternalStorageState();
        return (state.equals(Environment.MEDIA_MOUNTED) ||
                state.equals(Environment.MEDIA_MOUNTED_READ_ONLY));
    }
}
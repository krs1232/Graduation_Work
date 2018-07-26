package com.example.jw.userjoin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    private static String TAG = "phptest_MainActivity";

    private EditText EditNumber;
    private EditText EditPassword;
    private CheckBox AutoLogin;
    private SharedPreferences setting;
    private SharedPreferences.Editor editor;

    private TextView mTextViewJoin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditNumber = (EditText)findViewById(R.id.editText_number);
        EditPassword = (EditText)findViewById(R.id.editText_password);


        mTextViewJoin = (TextView)findViewById(R.id.textView_main_join);
        mTextViewJoin.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SubActivity.class);
                startActivity(intent);
                //finish();

            }
        });

        Button buttonLogin = (Button)findViewById(R.id.button_main_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String number = EditNumber.getText().toString();
                String password = EditPassword.getText().toString();


                if (setting.getBoolean("Auto_Login_enabled",true)) {

                    String ID = EditNumber.getText().toString();
                    String PW = EditPassword.getText().toString();

                    editor.putBoolean("Auto_Login_enabled",false);
                    editor.putString("ID",ID);
                    editor.putString("PW",PW);
                    editor.commit();

                    loginDB task = new loginDB();
                    task.execute(setting.getString("ID",""), setting.getString("PW",""));

                }else{

                    loginDB task = new loginDB();
                    task.execute(number,password);
                }

                EditNumber.setText("");
                EditPassword.setText("");


            }
        });
        setting = getSharedPreferences("setting",0);
        editor = setting.edit();

            if (setting.getBoolean("Auto_Login_enabled", true)) {


                loginDB task = new loginDB();
                task.execute(setting.getString("ID", ""), setting.getString("PW", ""));
                editor.putBoolean("Auto_Login_enabled",false);
                editor.commit();

            }



        CheckBox AutoLogin = (CheckBox)findViewById(R.id.mainCheck);
        AutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    editor.putBoolean("Auto_Login_enabled",true);
                    editor.commit();
                }else{
                    editor.clear();
                    editor.commit();
                }
            }
        });


    }
    class AutoLogin extends loginDB{


    }

    class loginDB extends AsyncTask<String, Void, Boolean>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);



            if(result){
                Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_LONG).show();

                Intent in = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(in);
                finish();

            }
            else{
                Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();

            }
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected Boolean doInBackground(String... params) {


            Boolean flag = false;
            String number = (String)params[0];
            String password = (String)params[1];

            String serverURL = "http://192.168.0.3:3100/android/android_login.php";
            String postParameters = "number=" + number + "&password=" + password ;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();



                if(sb.toString().equals("SQL") ) {
                    flag = true;
                    return flag;
                }
                else{
                    return flag;
                }

            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return flag;
            }

        }
    }

}







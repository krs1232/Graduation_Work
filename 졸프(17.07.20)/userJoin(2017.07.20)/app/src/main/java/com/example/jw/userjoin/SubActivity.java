package com.example.jw.userjoin;

/**
 * Created by JW on 2017. 7. 4..
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SubActivity extends MainActivity {

    private static String TAG = "phptest_MainActivity";

    private EditText mEditTextDay;
    private EditText mEditTextName;
    private EditText mEditTextNumber;
    private EditText mEditTextPassword;
    private EditText mEditTextPhone;
    private EditText mEditTextBox;

    private TextView mTextViewResult;

    // 현재시간을 msec 으로 구한다.
    long now = System.currentTimeMillis();
    // 현재시간을 date 변수에 저장한다.
    Date date = new Date(now);
    // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd");
    // nowDate 변수에 값을 저장한다.
    String formatDate = sdfNow.format(date);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        Button b3 = (Button)findViewById(R.id.button3);
        b3.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), NFCActivity.class);
                startActivity(intent);
            }
        });


        mEditTextDay = (EditText)findViewById(R.id.editText_main_day);
        mEditTextDay.setText(formatDate);
        mEditTextName = (EditText)findViewById(R.id.editText_main_name);
        mEditTextNumber = (EditText)findViewById(R.id.editText_main_number);
        mEditTextPassword = (EditText)findViewById(R.id.editText_main_password);
        mEditTextPhone = (EditText)findViewById(R.id.editText_main_phone);
        mEditTextBox = (EditText)findViewById(R.id.editText_main_box);

        mTextViewResult = (TextView)findViewById(R.id.textView_main_result);


        Intent intent1 = getIntent();
        final String nfcid = intent1.getStringExtra("NFCid=");

        mEditTextBox.setText(nfcid);

        Button buttonInsert = (Button)findViewById(R.id.button_main_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String day = mEditTextDay.getText().toString();
                String name = mEditTextName.getText().toString();
                String number = mEditTextNumber.getText().toString();
                String password = mEditTextPassword.getText().toString();
                String phone = mEditTextPhone.getText().toString();
                String box = mEditTextBox.getText().toString();

                InsertData task = new InsertData();
                task.execute(day,name,number,password,phone,box);





                //mEditTextDay.setText("");
                mEditTextName.setText("");
                mEditTextNumber.setText("");
                mEditTextPassword.setText("");
                mEditTextPhone.setText("");
                mEditTextBox.setText("");


            }
        });

    }




    class InsertData extends AsyncTask<String, Void, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SubActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);

            if(result){
                Toast.makeText(SubActivity.this, "회원가입 완료", Toast.LENGTH_LONG).show();

                Intent in = new Intent(SubActivity.this, MainActivity.class);
                startActivity(in);
                finish();

            }
            else{
                Toast.makeText(SubActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();

            }
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected Boolean doInBackground(String... params) {

            Boolean flag = false;

            String day = (String)params[0];
            String name = (String)params[1];
            String number = (String)params[2];
            String password = (String)params[3];
            String phone = (String)params[4];
            String box = (String)params[5];

            String serverURL = "http://192.168.0.3:3100/android/signup_user_information.php";
            String postParameters = "day=" + day + "&name=" + name + "&number=" + number + "& password=" + password + "&phone=" + password + "&box=" + box;


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



                if(sb.toString().equals("insert") ) {
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




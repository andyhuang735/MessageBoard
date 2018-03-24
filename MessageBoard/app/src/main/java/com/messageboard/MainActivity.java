package com.messageboard;

import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Typeface font;
    int ScreenWidth, ScreenHeight;

    String Url = "http://111.253.74.57:8080/messageboard/messageboard.php/";
    String ResonseCode = "", ReturnMessage = "";

    SwipeRefreshLayout SwipeRefresh;
    RecyclerView RecyclerView;
    FloatingActionButton FAB;
    AlertDialog Dialog, Add_Edit_Dialog, Delete_Dialog;

    //資料
    int Count;
    List<String> Id = new ArrayList<String>();
    List<String> Title = new ArrayList<String>();
    List<String> Message = new ArrayList<String>();
    List<String> Time = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        font = Typeface.createFromAsset(MainActivity.this.getAssets(),"fonts/fontawesome-webfont.ttf");
        Display();

        RecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this); //單列
        RecyclerView.setLayoutManager(layoutManager);

        SwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.SwipeRefresh);
        SwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ListTask ListTask = new ListTask();
                ListTask.execute();
                SwipeRefresh.setRefreshing(false); //停止更新圖示效果
            }
        });

        FAB = (FloatingActionButton) findViewById(R.id.FAB);
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Add_Edit_Dialog("Add", -1, "");
            }
        });

        ListTask ListTask = new ListTask();
        ListTask.execute();
    }

    //RecyclerView Adapter
    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        //Layout
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        //Item項目
        public class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout Linear_Item;
            TextView Item_Time,Item_Title,Item_Message;
            public ViewHolder(View v) {
                super(v);
                Linear_Item = v.findViewById(R.id.Linear_Item);
                Item_Time = v.findViewById(R.id.Item_Time);
                Item_Title = v.findViewById(R.id.Item_Title);
                Item_Message = v.findViewById(R.id.Item_Message);
            }
        }

        //參數,事件
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.Linear_Item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder BuilderAlertDialog = new AlertDialog.Builder(MainActivity.this);
                    View view1 = MainActivity.this.getLayoutInflater().inflate(R.layout.dialog_choose, null);

                    //設定參數
                    TextView IconEdit = view1.findViewById(R.id.IconEdit);
                    IconEdit.setTypeface(font);
                    IconEdit.setText("\uf040");

                    TextView IconDelete = view1.findViewById(R.id.IconDelete);
                    IconDelete.setTypeface(font);
                    IconDelete.setText("\uf014");

                    LinearLayout LinearEdit = view1.findViewById(R.id.LinearEdit);
                    LinearEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Add_Edit_Dialog("Edit", position, Id.get(position));
                            Dialog.cancel();
                        }
                    });
                    LinearLayout LinearDelete = view1.findViewById(R.id.LinearDelete);
                    LinearDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Delete_Dialog(position);
                            Dialog.cancel();
                        }
                    });

                    BuilderAlertDialog.setView(view1);
                    Dialog = BuilderAlertDialog.create();
                    Dialog.show();
                    int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
                    int height = WindowManager.LayoutParams.WRAP_CONTENT;
                    Dialog.getWindow().setLayout(width, height); //自訂寬高
                    Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }
            });
            holder.Item_Time.setText(Time.get(position));
            holder.Item_Title.setText(Title.get(position));
            holder.Item_Message.setText(Message.get(position));
        }

        //總筆數
        @Override
        public int getItemCount() {
            return Count;
        }
    }

    //新增,修改 Dialog
    void Add_Edit_Dialog(final String Type, final int position, final String Id){
        AlertDialog.Builder BuilderAlertDialog = new AlertDialog.Builder(MainActivity.this);
        View view1 = MainActivity.this.getLayoutInflater().inflate(R.layout.dialog_add_edit, null);

        //設定高度
        LinearLayout LinearTitle = view1.findViewById(R.id.LinearTitle);
        ScrollView ScrollMid = view1.findViewById(R.id.ScrollMid);
        LinearLayout LinearButton = view1.findViewById(R.id.LinearButton);
        double Height = ScreenHeight*0.8; //螢幕80%
        Height = Height/10;
        LinearTitle.getLayoutParams().height = (int)Height; //佔10%
        ScrollMid.getLayoutParams().height = (int)Height*8; //佔80%
        LinearButton.getLayoutParams().height = (int)Height; //佔10%

        //設定參數
        TextView TextTitle = view1.findViewById(R.id.TextTitle);
        final EditText Edit_Title = view1.findViewById(R.id.Title);
        final EditText Edit_Message = view1.findViewById(R.id.Message);

        if(Type.equals("Add")){
            TextTitle.setText(getResources().getString(R.string.Add));
        }else{
            TextTitle.setText(getResources().getString(R.string.Edit));
            Edit_Title.setText(Title.get(position));
            Edit_Message.setText(Message.get(position));
        }

        Button ButtonNo = view1.findViewById(R.id.ButtonNo);
        ButtonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Add_Edit_Dialog.cancel();
            }
        });
        Button ButtonYes = view1.findViewById(R.id.ButtonYes);
        ButtonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Empty(Edit_Title.getText().toString())){
                    Toast.makeText(MainActivity.this, R.string.Tip1, Toast.LENGTH_SHORT).show();
                }else{
                    if(Type.equals("Add")){
                        AddTask AddTask = new AddTask(Edit_Title.getText().toString(), Edit_Message.getText().toString());
                        AddTask.execute();
                    }else{
                        EditTask EditTask = new EditTask(Id, Edit_Title.getText().toString(), Edit_Message.getText().toString());
                        EditTask.execute();
                    }
                }
            }
        });

        BuilderAlertDialog.setView(view1);
        Add_Edit_Dialog = BuilderAlertDialog.create();
        Add_Edit_Dialog.show();
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.8);
        Add_Edit_Dialog.getWindow().setLayout(width, height); //自訂寬高
        Add_Edit_Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    //刪除 Dialog
    void Delete_Dialog(final int position){
        AlertDialog.Builder BuilderAlertDialog = new AlertDialog.Builder(MainActivity.this);
        View view1 = MainActivity.this.getLayoutInflater().inflate(R.layout.dialog_delete, null);

        //設定參數
        Button DeleteNo = view1.findViewById(R.id.DeleteNo);
        DeleteNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Delete_Dialog.cancel();
            }
        });
        Button DeleteYes = view1.findViewById(R.id.DeleteYes);
        DeleteYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteTask DeleteTask = new DeleteTask(Id.get(position));
                DeleteTask.execute();
            }
        });

        BuilderAlertDialog.setView(view1);
        Delete_Dialog = BuilderAlertDialog.create();
        Delete_Dialog.show();
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.9);
        int height = WindowManager.LayoutParams.WRAP_CONTENT;
        Delete_Dialog.getWindow().setLayout(width, height); //自訂寬高
        Delete_Dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    //執行緒-列表
    class ListTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                URL url = new URL(Url);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                ResonseCode = String.valueOf(urlConnection.getResponseCode()); //回應代碼
                if(ResonseCode.equals("200")){
                    Id.clear();
                    Title.clear();
                    Message.clear();
                    Time.clear();
                    Count = 0;

                    InputStream inputstream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
                    String ReturnData = reader.readLine();
                    JSONArray jsonArray1 = new JSONArray(ReturnData);
                    for (int i = 0; i < jsonArray1.length(); i++){
                        String Data = jsonArray1.getString(i);
                        JSONObject jsonObject = new JSONObject(Data);
                        Id.add(jsonObject.getString("id"));
                        Title.add(jsonObject.getString("title"));
                        Message.add(jsonObject.getString("message"));
                        Time.add(jsonObject.getString("time"));
                        Count ++;
                    }
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(ResonseCode.equals("200")){
                RecyclerView.setAdapter(new RecyclerViewAdapter());
            }
        }
    }

    //執行緒-新增
    class AddTask extends AsyncTask {
        String Title, Message;

        public AddTask(String Title, String Message){
            this.Title = Title;
            this.Message = Message;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                URL url = new URL(Url);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                //Post參數
                String urlParameters = "Title="+ URLEncoder.encode(Title,"utf-8")+"&Message="+URLEncoder.encode(Message,"utf-8");

                //送值
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush ();
                wr.close ();

                ResonseCode = String.valueOf(urlConnection.getResponseCode()); //回應代碼

                if(ResonseCode.equals("200")){
                    InputStream inputstream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
                    ReturnMessage = reader.readLine();
                }else{
                    InputStream in = new BufferedInputStream(urlConnection.getErrorStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    ReturnMessage = reader.readLine();
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(ResonseCode.equals("200")){
                ListTask ListTask = new ListTask();
                ListTask.execute();
                Add_Edit_Dialog.cancel();
            }
            Toast.makeText(MainActivity.this, ReturnMessage, Toast.LENGTH_SHORT).show();
        }
    }

    //執行緒-修改
    class EditTask extends AsyncTask {
        String Id, Title, Message;

        public EditTask(String Id, String Title, String Message){
            this.Id = Id;
            this.Title = Title;
            this.Message = Message;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                URL url = new URL(Url + Id);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                //PUT參數
                String urlParameters = "Title="+ URLEncoder.encode(Title,"utf-8")+"&Message="+URLEncoder.encode(Message,"utf-8");

                //送值
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush ();
                wr.close ();

                ResonseCode = String.valueOf(urlConnection.getResponseCode()); //回應代碼

                if(ResonseCode.equals("200")){
                    InputStream inputstream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
                    ReturnMessage = reader.readLine();
                }else{
                    InputStream in = new BufferedInputStream(urlConnection.getErrorStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    ReturnMessage = reader.readLine();
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(ResonseCode.equals("200")){
                ListTask ListTask = new ListTask();
                ListTask.execute();
                Add_Edit_Dialog.cancel();
            }
            Toast.makeText(MainActivity.this, ReturnMessage, Toast.LENGTH_SHORT).show();
        }
    }

    //執行緒-刪除
    class DeleteTask extends AsyncTask {
        String Id;

        public DeleteTask(String Id){
            this.Id = Id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                URL url = new URL(Url + Id);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("DELETE");
                urlConnection.connect();
                ResonseCode = String.valueOf(urlConnection.getResponseCode()); //回應代碼
                if(ResonseCode.equals("200")){
                    InputStream inputstream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
                    ReturnMessage = reader.readLine();
                }else{
                    InputStream in = new BufferedInputStream(urlConnection.getErrorStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    ReturnMessage = reader.readLine();
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(ResonseCode.equals("200")){
                ListTask ListTask = new ListTask();
                ListTask.execute();
            }
            Toast.makeText(MainActivity.this, ReturnMessage, Toast.LENGTH_SHORT).show();
            Delete_Dialog.cancel();
        }
    }

    //取得螢幕寬高
    void Display(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ScreenWidth = displayMetrics.widthPixels;
        ScreenHeight = displayMetrics.heightPixels;
    }

    //判斷空值(含空白)
    Boolean Empty(final String str ){
        return str == null || str.trim().isEmpty(); //空值回傳True
    }
}
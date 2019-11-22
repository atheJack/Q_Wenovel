package com.example.qs_noval;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button get_noval_resource;
    private TextView noval_content;
    private Button last_chapter;
    private Button next_chapter;
    private String websites;
    private int count=0;
    private boolean flag=false;
    private TextView noval_chapter;
    private String chapter_next_address;  //记录下一章的网址
    private String chapter_prev_address; //记录上一章的网址
    private ArrayList<String> catalog;
    private Spinner select_chapter;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        catalog=new ArrayList<String>();
        select_chapter=(Spinner)findViewById(R.id.header_btn2);
        get_noval_resource=(Button)findViewById(R.id.header_btn1);
        noval_content=(TextView)findViewById(R.id.noval_content);
        last_chapter=(Button)findViewById(R.id.tail_btn1);
        next_chapter=(Button)findViewById(R.id.tail_btn2);
        noval_chapter=(TextView)findViewById(R.id.noval_chapter);
        websites="https://read.qidian.com/chapter/GRXKztRHlME1/pSrkQ4m7qec1";
        getHtml2();
        get_noval_resource.setOnClickListener(this);
        last_chapter.setOnClickListener(this);
        next_chapter.setOnClickListener(this);
    }

    private String sendRequest(String murl){
        String responseData=null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(murl)
                    .build();
            Response response=client.newCall(request).execute();
            responseData=response.body().string();
            return responseData;
        }catch (IOException e) {
            e.printStackTrace();
        }
        return responseData;
    }

    /*private void getHtml() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("https://book.qidian.com/info/2019#Catalog")
                            .build();
                    Response response=client.newCall(request).execute();
                    String responseData=response.body().string();
                    getCatalog(responseData);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<String>strings=new ArrayList<>();
                            for(int i=1;i<catalog.size();i++){
                                String string="第"+i+"章";
                                strings.add(string);
                            }
                            //等到获取完目录之后，再设置spinner的监听事件，不然会异常,而且adpter的操作为UI操作，须runOnUiThread
                            adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.my_spinner_item,strings);
                            adapter.setDropDownViewResource(R.layout.dropdown_item);
                            select_chapter.setAdapter(adapter);
                            select_chapter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    websites=catalog.get(position+1);
                                    count=position;
                                    flag=true;
                                    beginGetResource();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }*/

    private void getHtml2(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                String responseData=sendRequest("https://book.qidian.com/info/2019#Catalog");
                e.onNext(responseData);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        getCatalog(s);
                    }
                });
    }

    private void getCatalog(String data) {
        Document doc=Jsoup.parse(data);
        Element parent_log=doc.getElementById("j-catalogWrap");
        Elements elements=parent_log.getElementsByTag("ul");
        Elements urLs= elements.get(0).getElementsByTag("li");
        for(Element urL:urLs){
            String link="https:"+urL.child(0).attr("href");
            catalog.add(link);
        }
        for(String string:catalog){
            Log.d("String is",string);
        }
        ArrayList<String>strings=new ArrayList<>();
        for(int i=1;i<catalog.size();i++){
            String string="第"+i+"章";
            strings.add(string);
        }
        //等到获取完目录之后，再设置spinner的监听事件，不然会异常,而且adpter的操作为UI操作，须runOnUiThread
        adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.my_spinner_item,strings);
        adapter.setDropDownViewResource(R.layout.dropdown_item);
        select_chapter.setAdapter(adapter);
        select_chapter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                websites=catalog.get(position+1);
                count=position;
                flag=true;
                beginGetResource2();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void beginGetResource2(){
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                String responseData=sendRequest(websites);
                e.onNext(responseData);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        showResponse(s);
                        noval_chapter.setText("第"+(count+1)+"章");
                    }
                });
    }

   /* private void beginGetResource() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(websites)
                            .build();
                    Response response=client.newCall(request).execute();
                    String responseData=response.body().string();
                    showResponse(responseData);
                    noval_chapter.setText("第"+(count+1)+"章");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }*/

    private void showResponse(final String responseData) {
        final String resp;
        StringBuilder stringBuilder=new StringBuilder();
        Document doc=Jsoup.parse(responseData); //开始解析html
        //以下操作与js神似，具体操作看网页结构
        Element chapterElement=doc.getElementById("j_chapterBox");
        Elements element1=chapterElement.getElementsByClass("read-content");
        Elements contents=element1.first().getElementsByTag("p");
        if(count>0){
            Element chapterPrev=doc.getElementById("j_chapterPrev");
            chapter_prev_address="https:"+chapterPrev.attr("href"); //获取href中的网址
        }
        Element chapterNext=doc.getElementById("j_chapterNext");
        chapter_next_address="https:"+chapterNext.attr("href");

        for(Element content:contents){
            stringBuilder.append(content.text()+'\n'); //text()方法直接提取文本
        }
        resp=stringBuilder.toString();
        noval_content.setText(resp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.header_btn1:
                flag=true;
                Toast.makeText(MainActivity.this,"正在获取小说，请稍等",Toast.LENGTH_SHORT).show();
                beginGetResource2();
                break;
            case R.id.tail_btn1:
                if(count>0){
                    count--;
                    websites=chapter_prev_address;
                    beginGetResource2();
                }else{
                    Toast.makeText(MainActivity.this,"已经是第一章了哦！",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tail_btn2:
                if(flag){
                    count++;
                    websites=chapter_next_address;
                    beginGetResource2();
                }else{
                    Toast.makeText(MainActivity.this,"请先点击左上方按钮以获取小说！",Toast.LENGTH_SHORT).show();
                }
                break;
            default:break;
        }
    }
}

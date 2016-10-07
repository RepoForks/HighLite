package com.example.sqliteprocessor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jeppeman.sqliteprocessor.SQLiteOperator;

import java.util.Arrays;

import rx.Completable;
import rx.Subscription;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        MyLittleClass m = new MyLittleClass();
        m.name = "asdf";
        m.nameList = Arrays.asList("a, ", "c", "b");
        SQLiteOperator.from(this, MyLittleClass.class)
                .insert(m)
                .prepareStatement()
                .execute()
                .subscribe(new Completable.CompletableSubscriber() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onSubscribe(Subscription d) {

                    }
                });
    }
}
package ru.wt23.planner23;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    Button enter;
    LinearLayout completed, nonCompleted;

    DBHelper dbHelper;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add("Очистить");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getTitle().equals("Очистить")) {
            clear();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        enter = (Button) findViewById(R.id.enterButton);
        nonCompleted = (LinearLayout) findViewById(R.id.noncompleted);
        completed = (LinearLayout) findViewById(R.id.completed);

        dbHelper = new DBHelper(this);

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String task = editText.getText().toString();
                add(task, getResources().getColor(R.color.blackw), DBHelper.TABLE_NON_COMPLETED);
                editText.setText("");
                //hideKeyBoard();
                Toast.makeText(getApplicationContext(), "Задание ДОБАВЛЕНО!", Toast.LENGTH_SHORT).show();
                nlist();
                clist();
            }
        });

        nlist();
        clist();

    }

    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void clear() {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DBHelper.TABLE_COMPLETED, null, null);
        database.close();
        Toast.makeText(getApplicationContext(), "Список очищен!", Toast.LENGTH_SHORT).show();
        nlist();
        clist();
    }

    private void add(String txt, int color, String table) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.NAME, txt);
        contentValues.put(DBHelper.COLOR, color);
        database.insert(table, null, contentValues);
        database.close();
        nlist();
        clist();
    }

    private void del(int id, String table) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(table, DBHelper.ID + "=" + id, null);
        database.close();
        nlist();
        clist();
    }

    private void update(int id, String table, String txt, int color) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.NAME, txt);
        contentValues.put(DBHelper.COLOR, color);
        database.update(table, contentValues, DBHelper.ID + "=" + id, null);
        database.close();
    }

    List<Obj> objs = new ArrayList<>();

    private void swipe(Obj obj) {
        objs.add(obj);
        if (objs.size() >= 2) {
            update(objs.get(0).id, DBHelper.TABLE_NON_COMPLETED, objs.get(1).task, objs.get(1).color);
            update(objs.get(1).id, DBHelper.TABLE_NON_COMPLETED, objs.get(0).task, objs.get(0).color);

            objs.clear();
            nlist();
            clist();
        }
    }

    private class Obj {
        int id;
        String task;
        int color;

        Obj(int id, String task, int color) {
            this.id = id;
            this.task = task;
            this.color = color;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void nlist() {
        List<CardView> viewsList = new ArrayList<>();
        objs.clear();

        nonCompleted.removeAllViews();
        LinearLayout.LayoutParams lpSwipe = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpSwipe.weight = 125;
        LinearLayout.LayoutParams lpTask = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpTask.weight = 50;
        LinearLayout.LayoutParams lpOk = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpOk.weight = 125;


        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(DBHelper.TABLE_NON_COMPLETED, null, null, null, null, null, null);
        int index_ID = cursor.getColumnIndex(DBHelper.ID);
        int index_NAME = cursor.getColumnIndex(DBHelper.NAME);
        int index_COLOR = cursor.getColumnIndex(DBHelper.COLOR);

        while (cursor.moveToNext()) {
            final int id = cursor.getInt(index_ID);
            final String task = cursor.getString(index_NAME);
            final int color = cursor.getInt(index_COLOR);

            final CardView cardView = new CardView(this);
            cardView.setCardBackgroundColor(color);
            cardView.setUseCompatPadding(true);

            final LinearLayout tLay = new LinearLayout(this);
            tLay.setWeightSum(300);
            tLay.setOrientation(LinearLayout.HORIZONTAL);

            ImageView ivSwipe = new ImageView(this);
            ivSwipe.setImageDrawable(getResources().getDrawable(R.drawable.ic_unfold_more_black_24dp));
            ivSwipe.setColorFilter(Color.parseColor("#23a032"));
            ivSwipe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cardView.setCardBackgroundColor(Color.parseColor("#23a032"));
                    swipe(new Obj(id, task, color));
                }
            });

            TextView tvTask = new TextView(this);
            tvTask.setGravity(Gravity.CENTER | Gravity.START);
            tvTask.setText(task);

            ImageView ivOk = new ImageView(this);
            ivOk.setImageResource(R.drawable.ic_check_circle_black_24dp);
            if (color == getResources().getColor(R.color.colorWhite)) {
                tvTask.setTextColor(getResources().getColor(R.color.colorBlack));
                ivOk.setColorFilter(getResources().getColor(R.color.colorSiteGreen));
            } else if (color == getResources().getColor(R.color.colorGray)) {
                tvTask.setTextColor(getResources().getColor(R.color.colorBlack));
                ivOk.setColorFilter(getResources().getColor(R.color.colorBlack));
            } else if (color == getResources().getColor(R.color.colorSiteGreen)) {
                tvTask.setTextColor(getResources().getColor(R.color.colorWhite));
                ivOk.setColorFilter(getResources().getColor(R.color.colorBlack));
            } else {
                tvTask.setTextColor(getResources().getColor(R.color.colorWhite));
                ivOk.setColorFilter(getResources().getColor(R.color.colorSiteGreen));
            }

            ivOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    add(task, color, DBHelper.TABLE_COMPLETED);
                    del(id, DBHelper.TABLE_NON_COMPLETED);
                }
            });

            tvTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeDialog(id, DBHelper.TABLE_NON_COMPLETED, task, color);
                }
            });

            tvTask.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    colorDialog(id, task, DBHelper.TABLE_NON_COMPLETED);
                    return false;
                }
            });

            tLay.addView(ivSwipe, lpSwipe);
            tLay.addView(tvTask, lpTask);
            tLay.addView(ivOk, lpOk);

            cardView.addView(tLay);


            viewsList.add(cardView);

        }
        cursor.close();
        database.close();

        for (int i = viewsList.size() - 1; i >= 0; i--) {
            nonCompleted.addView(viewsList.get(i));
        }

    }

    private void changeDialog(final int id, final String table, String task, final int color) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change, null);
        final EditText etTask = (EditText) view.findViewById(R.id.et_task);
        etTask.setHint(task);
        etTask.setText(task);
        etTask.setSelection(task.length());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Изменение задания");
        builder.setView(view);
        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                update(id, table, etTask.getText().toString(), color);
                nlist();
                clist();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void colorDialog(final int id, final String txt, final String table) {
        final int[] color = new int[1];
        color[0] = getResources().getColor(R.color.blackw);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_color, null);
        final CardView exampleCard = (CardView) view.findViewById(R.id.exampleCard);
        final TextView tvExample = (TextView) view.findViewById(R.id.tvExample);
        final ImageView okExample = (ImageView) view.findViewById(R.id.okExample);

        RoundedImageView colorSiteYolo = (RoundedImageView) view.findViewById(R.id.colorSiteYolo);
        colorSiteYolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color[0] = getResources().getColor(R.color.colorSiteYolo);
                exampleCard.setCardBackgroundColor(getResources().getColor(R.color.colorSiteYolo));
                tvExample.setTextColor(getResources().getColor(R.color.colorWhite));
                okExample.setColorFilter(getResources().getColor(R.color.colorSiteGreen));
            }
        });
        RoundedImageView colorAccent = (RoundedImageView) view.findViewById(R.id.colorAccent);
        colorAccent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color[0] = getResources().getColor(R.color.colorAccent);
                exampleCard.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
                tvExample.setTextColor(getResources().getColor(R.color.colorWhite));
                okExample.setColorFilter(getResources().getColor(R.color.colorSiteGreen));
            }
        });
        RoundedImageView colorWhite = (RoundedImageView) view.findViewById(R.id.colorWhite);
        colorWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color[0] = getResources().getColor(R.color.colorWhite);
                exampleCard.setCardBackgroundColor(getResources().getColor(R.color.colorWhite));
                tvExample.setTextColor(getResources().getColor(R.color.colorBlack));
                okExample.setColorFilter(getResources().getColor(R.color.colorSiteGreen));
            }
        });
        RoundedImageView colorRed = (RoundedImageView) view.findViewById(R.id.colorRed);
        colorRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color[0] = getResources().getColor(R.color.colorRed);
                exampleCard.setCardBackgroundColor(getResources().getColor(R.color.colorRed));
                tvExample.setTextColor(getResources().getColor(R.color.colorWhite));
                okExample.setColorFilter(getResources().getColor(R.color.colorSiteGreen));
            }
        });
        RoundedImageView colorBlack = (RoundedImageView) view.findViewById(R.id.colorBlack);
        colorBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color[0] = getResources().getColor(R.color.colorBlack);
                exampleCard.setCardBackgroundColor(getResources().getColor(R.color.colorBlack));
                tvExample.setTextColor(getResources().getColor(R.color.colorWhite));
                okExample.setColorFilter(getResources().getColor(R.color.colorSiteGreen));
            }
        });
        RoundedImageView colorGray = (RoundedImageView) view.findViewById(R.id.colorGray);
        colorGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color[0] = getResources().getColor(R.color.colorGray);
                exampleCard.setCardBackgroundColor(getResources().getColor(R.color.colorGray));
                tvExample.setTextColor(getResources().getColor(R.color.colorBlack));
                okExample.setColorFilter(getResources().getColor(R.color.colorBlack));
            }
        });
        RoundedImageView colorSiteBlue = (RoundedImageView) view.findViewById(R.id.colorSiteBlue);
        colorSiteBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color[0] = getResources().getColor(R.color.colorSiteBlue);
                exampleCard.setCardBackgroundColor(getResources().getColor(R.color.colorSiteBlue));
                tvExample.setTextColor(getResources().getColor(R.color.colorWhite));
                okExample.setColorFilter(getResources().getColor(R.color.colorSiteGreen));
            }
        });
        RoundedImageView colorSiteWhiteBlue = (RoundedImageView) view.findViewById(R.id.colorSiteWhiteBlue);
        colorSiteWhiteBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color[0] = getResources().getColor(R.color.colorSiteWhiteBlue);
                exampleCard.setCardBackgroundColor(getResources().getColor(R.color.colorSiteWhiteBlue));
                tvExample.setTextColor(getResources().getColor(R.color.colorWhite));
                okExample.setColorFilter(getResources().getColor(R.color.colorSiteGreen));
            }
        });
        final RoundedImageView colorSiteGreen = (RoundedImageView) view.findViewById(R.id.colorSiteGreen);
        colorSiteGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                color[0] = getResources().getColor(R.color.colorSiteGreen);
                exampleCard.setCardBackgroundColor(getResources().getColor(R.color.colorSiteGreen));
                tvExample.setTextColor(getResources().getColor(R.color.colorWhite));
                okExample.setColorFilter(getResources().getColor(R.color.colorBlack));
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выбор цвета");
        builder.setView(view);
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                update(id, table, txt, color[0]);
                nlist();
                clist();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void clist() {
        objs.clear();
        LinearLayout.LayoutParams lpTask = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpTask.weight = 30;
        LinearLayout.LayoutParams lpOk = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lpOk.weight = 170;

        completed.removeAllViews();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(DBHelper.TABLE_COMPLETED, null, null, null, null, null, null);
        int index_ID = cursor.getColumnIndex(DBHelper.ID);
        int index_NAME = cursor.getColumnIndex(DBHelper.NAME);
        int index_COLOR = cursor.getColumnIndex(DBHelper.COLOR);
        while (cursor.moveToNext()) {
            final int id = cursor.getInt(index_ID);
            final String task = cursor.getString(index_NAME);
            final String color = cursor.getString(index_COLOR);

            CardView cardView = new CardView(this);
            cardView.setCardBackgroundColor(getResources().getColor(R.color.colorGray));
            cardView.setUseCompatPadding(true);

            LinearLayout tLay = new LinearLayout(this);
            tLay.setWeightSum(200);
            tLay.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvTask = new TextView(this);
            tvTask.setGravity(Gravity.CENTER | Gravity.START);
            tvTask.setTextColor(getResources().getColor(R.color.colorBlack));
            tvTask.setText(task);

            ImageView ivdel = new ImageView(this);
            ivdel.setImageResource(R.drawable.ic_cancel_black_24dp);
            ivdel.setColorFilter(getResources().getColor(R.color.colorSiteRed));
            ivdel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    del(id, DBHelper.TABLE_COMPLETED);
                }
            });

            tLay.addView(tvTask, lpTask);
            tLay.addView(ivdel, lpOk);

            cardView.addView(tLay);


            completed.addView(cardView);


        }
        cursor.close();
        database.close();


    }

}
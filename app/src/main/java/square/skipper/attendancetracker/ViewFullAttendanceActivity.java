package square.skipper.attendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static square.skipper.attendancetracker.MainActivity.DB;

public class ViewFullAttendanceActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_full_attendance);

        findViewById(R.id.VFABackB).setOnClickListener((v)-> finish());
        LoadContent();
    }

    private void LoadContent()
    {
        final HorizontalScrollView HeaderSV = findViewById(R.id.HeaderSV);
        final ScrollView FixedColumnSV = findViewById(R.id.FixedColumnSV);
        final NestedScrollView DataSVV = findViewById(R.id.DataSVV);
        final HorizontalScrollView DataSVH = findViewById(R.id.DataSVH);
        final TableLayout FixedColumnTL = findViewById(R.id.FixedColumnTL);
        final TableLayout DataTL = findViewById(R.id.DataTL);
        final TableLayout HeaderTL = findViewById(R.id.HeaderTL);

        DataSVV.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener()
        {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY)
            {
                FixedColumnSV.setScrollY(scrollY);
            }
        });

        DataSVH.setOnScrollChangeListener(new View.OnScrollChangeListener()
        {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3)
            {
                HeaderSV.setScrollX(i);
            }
        });

        HeaderSV.setOnScrollChangeListener(new View.OnScrollChangeListener()
        {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3)
            {
                DataSVH.setScrollX(i);
            }
        });

        FixedColumnSV.setOnScrollChangeListener(new View.OnScrollChangeListener()
        {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3)
            {
                DataSVV.setScrollY(i1);
            }
        });

        TableRow TRH = new TableRow(this);
        for(int i=1; i<=10; i++)
            TRH.addView(GetTextView(String.valueOf(i)));
        HeaderTL.addView(TRH);

        new Thread(()->
        {
            final List<String> Dates = DB.userDao().GetAllAttendanceDates();
            runOnUiThread(()->
            {
                if (Dates.size() == 0)
                {
                    Toast.makeText(this, "No Data Available!", Toast.LENGTH_SHORT).show();
                    finish();
                } else
                    for (String date : Dates)
                    {
                        TableRow TR = new TableRow(this);
                        TR.addView(GetTextView(ChangeDate(date)));
                        FixedColumnTL.addView(TR);
                        final TableRow TR1 = new TableRow(this);
                        for (int i = 0; i < 10; i++)
                            TR1.addView(GetTextView(" "));
                        DataTL.addView(TR1);

                        new Thread(() ->
                        {
                            List<Attendance> attendanceList = DB.userDao().GetAttendanceByDate(date);
                            int Size = attendanceList.size();

                            for (int i = 0; i < Size; i++)
                            {
                                Attendance attendance = attendanceList.get(i);
                                Subject subject = DB.userDao().GetSubjectById(attendance.SubjectId);
                                runOnUiThread(() ->
                                {
                                    String Text = (attendance.Present ? "P" : "A") + "\n" + subject.SubjectName;
                                    TextView TV = (TextView) TR1.getChildAt(attendance.Hour - 1);
                                    TV.setText(Text);
                                });
                            }
                        }).start();
                    }
            });
        }).start();
    }

    private TextView GetTextView(String Content)
    {
        TextView TV = new TextView(this);
        TV.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        TV.setText(Content);
        TV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        TV.setBackgroundResource(R.drawable.table_cell_background);
        TV.setWidth(getDP(150));
        TV.setHeight(getDP(100));
        return TV;
    }

    private int getDP(int Pixel)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Pixel, getResources().getDisplayMetrics());
    }

    @NonNull
    private String ChangeDate(String OldDate)
    {
        try
        {
            SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date d = DateFormat.parse(OldDate);
            DateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
            return DateFormat.format(d);
        }
        catch(Exception ex)
        {
            return "";
        }
    }
}

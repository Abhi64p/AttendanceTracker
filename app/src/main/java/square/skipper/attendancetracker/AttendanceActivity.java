package square.skipper.attendancetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static square.skipper.attendancetracker.MainActivity.DB;

public class AttendanceActivity extends AppCompatActivity
{
    private String SelectedDate = "";
    private int SelectedHour = -1;
    private Button SelectedHourButton = null;
    private int SubjectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        TextView SubjectNameTV = findViewById(R.id.MarkAttendaceSubjectNameTV);
        TextView DateTV = findViewById(R.id.MarkAttendanceDateTV);

        SubjectId = getIntent().getIntExtra("SubjectId",-1);
        new Thread(()->
        {
            Subject subject = DB.userDao().GetSubjectById(SubjectId);
            runOnUiThread(()-> SubjectNameTV.setText(subject.SubjectName));
        }).start();

        SelectedDate = GetDate();
        DateTV.setText(ChangeDate(SelectedDate));
        findViewById(R.id.MarkAttendanceBackButton).setOnClickListener((v)-> finish());
    }

    public void DateClicked(final View view)
    {
        final View DialogView = LayoutInflater.from(this).inflate(R.layout.date_picket_dialog,(ViewGroup)view.getParent(),false);
        final AlertDialog Dialog = new AlertDialog.Builder(this)
                .setView(DialogView)
                .create();
        DialogView.findViewById(R.id.DatePickerCloseButton).setOnClickListener((v)-> Dialog.dismiss());

        final CalendarView CV = DialogView.findViewById(R.id.DatePickerCV);
        CV.setDate(Calendar.getInstance().getTimeInMillis());
        CV.setOnDateChangeListener((calenderView,i,i1,i2) ->
        {
           Dialog.dismiss();
           SelectedDate = ParseDate(i + "-" + (i1+1) + "-" + i2);
           ((TextView)view).setText(ChangeDate(SelectedDate));
        });
        Dialog.show();
    }

    public void SaveAttendance(View view)
    {
        boolean Continue = true;
        RadioButton YesRB = findViewById(R.id.YesRB);

        if(SelectedDate.isEmpty())
        {
            Toast.makeText(this, "Select a date!", Toast.LENGTH_SHORT).show();
            Continue = false;
        }

        if(SelectedHour == -1)
        {
            Toast.makeText(this, "Select an hour!", Toast.LENGTH_SHORT).show();
            Continue = false;
        }

        if(Continue)
        {
            new Thread(()->
            {
                int result = DB.userDao().IsAttendanceAlreadyMarked(SelectedDate,SelectedHour);
                if(result == 0)
                {
                    Attendance attendance = new Attendance();
                    attendance.SubjectId = SubjectId;
                    attendance.AttendanceDate = SelectedDate;
                    attendance.Hour = SelectedHour;
                    attendance.Present = YesRB.isChecked();
                    DB.userDao().Insert(attendance);
                }

                runOnUiThread(()->
                {
                    if(result==0)
                    {
                        setResult(RESULT_OK);
                        finish();
                        Toast.makeText(this, "Attendance Saved!", Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(this, "Attendance Already Marked!", Toast.LENGTH_SHORT).show();
                });
            }).start();
        }
    }

    public void HourButtonClicked(View view)
    {
        Button CurrentButton = (Button) view;
        int tmpHour = Integer.parseInt(CurrentButton.getText().toString());
        if(SelectedHourButton != null)
                SelectedHourButton.setBackgroundResource(R.drawable.button_background_unselected);
        SelectedHourButton = CurrentButton;
        SelectedHour = tmpHour;
        CurrentButton.setBackgroundResource(R.drawable.button_background_selected);
    }

    @NonNull
    static String GetDate()
    {
        Calendar c = Calendar.getInstance();
        Date d = c.getTime();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return df.format(d);
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

    @NonNull
    private String ParseDate(String OldDate)
    {
        try
        {
            SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            Date d = DateFormat.parse(OldDate);
            DateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            return DateFormat.format(d);
        }
        catch(Exception ex)
        {
            return "";
        }
    }
}

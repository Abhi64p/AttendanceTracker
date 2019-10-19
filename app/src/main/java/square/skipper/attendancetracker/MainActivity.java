package square.skipper.attendancetracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    public static AppDatabase DB;
    private LinearLayout LL1;
    private LinearLayout LL2;
    private int SubjectSize = 0;
    private final int AttendanceActivityRequestCode = 0;
    private final int EditAttendanceActivityRequestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LL1 = findViewById(R.id.LL1);
        LL2 = findViewById(R.id.LL2);

        DB = Room.databaseBuilder(this, AppDatabase.class, "AppDatabase").build();
        LoadSubjects();

        findViewById(R.id.ViewFullAttendanceB).setOnClickListener((v)->startActivity(new Intent(this,ViewFullAttendanceActivity.class)));
    }

    public void AddSubjectClicked(View view)
    {
        final View DialogView = LayoutInflater.from(this).inflate(R.layout.add_subject_layout, (ViewGroup) view.getParent(), false);
        final AlertDialog Dialog = new AlertDialog.Builder(this)
                .setView(DialogView).create();

        DialogView.findViewById(R.id.CloseIV).setOnClickListener((v) -> Dialog.dismiss());
        DialogView.findViewById(R.id.AddB).setOnClickListener((v) ->
        {

            EditText SubjectET = ((TextInputLayout) DialogView.findViewById(R.id.SubjectNameTIL)).getEditText();
            assert SubjectET != null;
            String SubjectName = SubjectET.getText().toString();
            if (SubjectName.isEmpty())
            {
                SubjectET.setError("Empty!");
                SubjectET.requestFocus();
            } else
            {
                Dialog.dismiss();
                new Thread(() ->
                {
                    Subject subject = new Subject();
                    subject.SubjectName = SubjectName;
                    DB.userDao().Insert(subject);

                    runOnUiThread(this::LoadSubjects);
                }).start();
            }
        });

        Dialog.show();
    }

    private void LoadSubjects()
    {
        LL1.removeAllViews();
        LL2.removeAllViews();
        new Thread(() ->
        {
            List SubjectList = DB.userDao().GetAllSubjects();
            SubjectSize = SubjectList.size();

            if (SubjectSize != 0)
            {
                runOnUiThread(() -> findViewById(R.id.NoSubjectTV).setVisibility(View.GONE));
                for (int i = 0; i < SubjectSize; i++)
                {
                    final Subject S = (Subject) SubjectList.get(i);
                    final List<Attendance> attendances = DB.userDao().GetAttendanceBySubjectId(S.SubjectId);
                    final boolean Left = i % 2 == 0;
                    runOnUiThread(() ->
                    {
                        View SubjectCard = LayoutInflater.from(this).inflate(R.layout.subject_card_layout, (ViewGroup) LL1.getParent(), false);
                        ((TextView) SubjectCard.findViewById(R.id.CardSubjectNameTV)).setText(S.SubjectName);

                        SubjectCard.findViewById(R.id.CardAddButton).setOnClickListener((v) ->
                        {
                            Intent intent = new Intent(this, AttendanceActivity.class);
                            intent.putExtra("SubjectId", S.SubjectId);
                            startActivityForResult(intent, AttendanceActivityRequestCode);
                        });

                        SubjectCard.findViewById(R.id.CardEditButton).setOnClickListener((v) ->
                        {
                            Intent intent = new Intent(this, EditAttendanceActivity.class);
                            intent.putExtra("SubjectId", S.SubjectId);
                            startActivityForResult(intent, EditAttendanceActivityRequestCode);
                        });

                        int Total = attendances.size();
                        int Present = 0;
                        for (Attendance a : attendances)
                            if (a.Present)
                                Present++;
                        if (Total != 0)
                        {
                            float value = ((float) Present * 100) / Total;
                            ((TextView) SubjectCard.findViewById(R.id.CardAttendanceTV)).setText(String.format(Locale.ENGLISH, "%.2f %s", value, "%"));

                        }
                        if (Left)
                            LL1.addView(SubjectCard);
                        else
                            LL2.addView(SubjectCard);
                    });
                }

            } else
                runOnUiThread(() -> findViewById(R.id.NoSubjectTV).setVisibility(View.VISIBLE));

        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == AttendanceActivityRequestCode)
        {
            if (resultCode == RESULT_OK)
                LoadSubjects();
        }
        else if(requestCode == EditAttendanceActivityRequestCode)
            LoadSubjects();
    }
}
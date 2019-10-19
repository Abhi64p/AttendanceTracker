package square.skipper.attendancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import static square.skipper.attendancetracker.MainActivity.DB;

public class EditAttendanceActivity extends AppCompatActivity
{
    private int SubjectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_attendance);

        findViewById(R.id.EditAttendanceBackB).setOnClickListener((v) -> finish());
        SubjectId = getIntent().getIntExtra("SubjectId", -1);
        LoadContent();
    }

    private void LoadContent()
    {
        TableLayout TL = findViewById(R.id.EditAttendanceTL);
        int Size = TL.getChildCount();
        for (int i = 1; i < Size; i++)
            TL.removeViewAt(1);

        new Thread(() ->
        {
            List<Attendance> attendanceList = DB.userDao().GetAttendanceBySubjectId(SubjectId);
            runOnUiThread(() ->
            {
                int AttendanceSize = attendanceList.size();
                for (int i = 0; i < AttendanceSize; i++)
                {
                    Attendance attendance = attendanceList.get(i);
                    TableRow TR = new TableRow(this);
                    TR.addView(GetTextView(attendance.AttendanceDate));
                    TR.addView(GetTextView(String.valueOf(attendance.Hour)));

                    TextView TV = GetTextView(attendance.Present ? "Yes" : "No");
                    TV.setTextColor(Color.parseColor(getString(R.string.blue_color)));
                    TV.setOnClickListener((v)->
                    {
                        final View DialogView = LayoutInflater.from(this).inflate(R.layout.change_attendance_layout, (ViewGroup) v.getParent(), false);
                        final AlertDialog Dialog = new AlertDialog.Builder(this)
                                .setView(DialogView).create();

                        final RadioButton YesRB = DialogView.findViewById(R.id.ChangeAttendanceYesRB);
                        YesRB.setChecked(attendance.Present);
                        ((RadioButton)DialogView.findViewById(R.id.ChangeAttendanceNoRB)).setChecked(!attendance.Present);

                        DialogView.findViewById(R.id.ChangeAttendanceCloseButton).setOnClickListener((v1) -> Dialog.dismiss());
                        DialogView.findViewById(R.id.ChangeAttendanceUpdateButton).setOnClickListener((v1) ->
                                new Thread(() ->
                                {
                                    DB.userDao().UpdatePresentStatus(attendance.AttendanceId, YesRB.isChecked());
                                    Dialog.dismiss();
                                    runOnUiThread(this::LoadContent);
                                }).start());
                        Dialog.show();
                    });
                    TR.addView(TV);

                    ImageButton IB = new ImageButton(this);
                    IB.setImageResource(R.drawable.delete_button_icon);
                    IB.setBackgroundResource(R.drawable.table_cell_background);
                    IB.setOnClickListener((v)->
                    {
                        final View DialogView = LayoutInflater.from(this).inflate(R.layout.attendance_delete_layout, (ViewGroup) v.getParent(), false);
                        final AlertDialog Dialog = new AlertDialog.Builder(this)
                                .setView(DialogView).create();
                        DialogView.findViewById(R.id.AttendanceDeleteCloseButton).setOnClickListener((v1) -> Dialog.dismiss());
                        DialogView.findViewById(R.id.DeleteAttendanceNoB).setOnClickListener((v1) -> Dialog.dismiss());
                        DialogView.findViewById(R.id.DeleteAttendanceYesB).setOnClickListener((v1) ->
                                new Thread(() ->
                                {
                                    DB.userDao().Delete(attendance);
                                    runOnUiThread(() ->
                                    {
                                        Dialog.dismiss();
                                        LoadContent();
                                    });
                                }).start()
                        );
                        Dialog.show();
                    });
                    TR.addView(IB);

                    TL.addView(TR);
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
        TV.setPadding(10,20,10,20);
        TableRow.LayoutParams p;
        /*if(IsItemName)
            p = new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT);
        else*/
            p = new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.MATCH_PARENT);
        /*if(IsItemName)
            TV.setWidth(getDP());*/
        TV.setLayoutParams(p);
        return TV;
    }

    public void DeleteSubjectClicked(View view)
    {
        final View DialogView = LayoutInflater.from(this).inflate(R.layout.attendance_delete_layout, (ViewGroup) view.getParent(), false);
        final AlertDialog Dialog = new AlertDialog.Builder(this)
                .setView(DialogView).create();
        ((TextView)DialogView.findViewById(R.id.DeleteAttendanceTV)).setText(getText(R.string.subject_delete_warning));
        DialogView.findViewById(R.id.AttendanceDeleteCloseButton).setOnClickListener((v1) -> Dialog.dismiss());
        DialogView.findViewById(R.id.DeleteAttendanceNoB).setOnClickListener((v1) -> Dialog.dismiss());
        DialogView.findViewById(R.id.DeleteAttendanceYesB).setOnClickListener((v1) ->
                new Thread(() ->
                {
                    DB.userDao().DeleteAttendanceBySubjectId(SubjectId);
                    DB.userDao().DeleteSubjectById(SubjectId);
                    runOnUiThread(() ->
                    {
                        Dialog.dismiss();
                        finish();
                    });
                }).start()
        );
        Dialog.show();
    }
}
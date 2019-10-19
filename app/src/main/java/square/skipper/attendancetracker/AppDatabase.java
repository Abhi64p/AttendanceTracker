package square.skipper.attendancetracker;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.RoomDatabase;

import java.util.List;


@Database(entities = {Subject.class, Attendance.class}, version = 1,exportSchema = false)
abstract class AppDatabase extends RoomDatabase
{
    public abstract UserDao userDao();
}

@Entity
class Subject
{
    @PrimaryKey (autoGenerate = true)
    int SubjectId;
    String SubjectName;
}

@Entity
class Attendance
{
    @PrimaryKey (autoGenerate = true)
    int AttendanceId;
    int SubjectId;
    String AttendanceDate;
    boolean Present;
    int Hour;
}


@Dao
interface UserDao
{
    @Insert
    void Insert(Subject s);

    @Insert
    void Insert(Attendance a);

    @Query("SELECT * FROM Subject")
    List<Subject> GetAllSubjects();

    @Query("SELECT * FROM Subject WHERE SubjectId=:ID")
    Subject GetSubjectById(int ID);

    @Query("SELECT COUNT(AttendanceId) FROM Attendance WHERE AttendanceDate=:date AND Hour=:Hour")
    int IsAttendanceAlreadyMarked(String date, int Hour);

    @Query("SELECT * FROM Attendance WHERE SubjectId=:SID")
    List<Attendance> GetAttendanceBySubjectId(int SID);

    @Delete
    void Delete(Attendance a);

    @Query("DELETE FROM Attendance WHERE SubjectId=:SID")
    void DeleteAttendanceBySubjectId(int SID);

    @Query("DELETE FROM Subject WHERE SubjectId=:SID")
    void DeleteSubjectById(int SID);

    @Query("UPDATE Attendance SET Present=:p WHERE AttendanceId=:ID")
    void UpdatePresentStatus(int ID, boolean p);

    @Query("SELECT * FROM Attendance WHERE AttendanceDate=:date")
    List<Attendance> GetAttendanceByDate(String date);

    @Query("SELECT AttendanceDate FROM Attendance GROUP BY AttendanceDate")
    List<String> GetAllAttendanceDates();
}

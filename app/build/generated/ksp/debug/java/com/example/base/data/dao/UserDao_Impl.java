package com.example.base.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.base.data.model.User;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserDao_Impl implements UserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<User> __insertionAdapterOfUser;

  private final SharedSQLiteStatement __preparedStmtOfUpdateGoal;

  public UserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUser = new EntityInsertionAdapter<User>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_table` (`id`,`name`,`weight`,`dailyGoal`,`birthDate`,`wakeUpTime`,`sleepTime`,`onboardingCompleted`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final User entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindDouble(3, entity.getWeight());
        statement.bindLong(4, entity.getDailyGoal());
        statement.bindLong(5, entity.getBirthDate());
        statement.bindString(6, entity.getWakeUpTime());
        statement.bindString(7, entity.getSleepTime());
        final int _tmp = entity.getOnboardingCompleted() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
    this.__preparedStmtOfUpdateGoal = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE user_table SET dailyGoal = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertUser(final User user, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUser.insert(user);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateGoal(final int id, final int goal,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateGoal.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, goal);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateGoal.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getUser(final Continuation<? super User> $completion) {
    final String _sql = "SELECT * FROM user_table LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<User>() {
      @Override
      @Nullable
      public User call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "weight");
          final int _cursorIndexOfDailyGoal = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyGoal");
          final int _cursorIndexOfBirthDate = CursorUtil.getColumnIndexOrThrow(_cursor, "birthDate");
          final int _cursorIndexOfWakeUpTime = CursorUtil.getColumnIndexOrThrow(_cursor, "wakeUpTime");
          final int _cursorIndexOfSleepTime = CursorUtil.getColumnIndexOrThrow(_cursor, "sleepTime");
          final int _cursorIndexOfOnboardingCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "onboardingCompleted");
          final User _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final float _tmpWeight;
            _tmpWeight = _cursor.getFloat(_cursorIndexOfWeight);
            final int _tmpDailyGoal;
            _tmpDailyGoal = _cursor.getInt(_cursorIndexOfDailyGoal);
            final long _tmpBirthDate;
            _tmpBirthDate = _cursor.getLong(_cursorIndexOfBirthDate);
            final String _tmpWakeUpTime;
            _tmpWakeUpTime = _cursor.getString(_cursorIndexOfWakeUpTime);
            final String _tmpSleepTime;
            _tmpSleepTime = _cursor.getString(_cursorIndexOfSleepTime);
            final boolean _tmpOnboardingCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfOnboardingCompleted);
            _tmpOnboardingCompleted = _tmp != 0;
            _result = new User(_tmpId,_tmpName,_tmpWeight,_tmpDailyGoal,_tmpBirthDate,_tmpWakeUpTime,_tmpSleepTime,_tmpOnboardingCompleted);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

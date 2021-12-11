package com.example.mytable.database;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

public class SettingsRepository {
    private SettingsDao settingsDao;
    private Application application;

    public SettingsRepository(Application application) {
        this.application = application;
        AppDatabase database = AppDatabase.getInstance(application);
        settingsDao = database.settingsDao();
    }
    public void insert(Setting setting) {
        new InsertSettingAsyncTask(settingsDao).execute(setting);
    }
    public void update(Setting setting) {
        new UpdateSettingAsyncTask(settingsDao).execute(setting);
    }
    public void delete(Integer id) {
        new DeleteCourseAsyncTask(settingsDao, id).execute();
    }

    public void deleteAllSettings() {
        new DeleteAllSettingsAsyncTask(settingsDao).execute();
    }

    public List<Setting> getAllSettings() {
        return settingsDao.getAllSettings();
    }

    private static class InsertSettingAsyncTask extends AsyncTask<Setting, Void, Void> {
        private SettingsDao settingDao;

        private InsertSettingAsyncTask(SettingsDao dao) {
            this.settingDao = dao;
        }

        @Override
        protected Void doInBackground(Setting... settings) {
            // below line is use to insert our modal in dao.
            settingDao.insert(settings[0]);
            return null;
        }
    }
    private static class UpdateSettingAsyncTask extends AsyncTask<Setting, Void, Void> {
        private SettingsDao settingsDao;

        private UpdateSettingAsyncTask(SettingsDao dao) {
            this.settingsDao = dao;
        }

        @Override
        protected Void doInBackground(Setting... settings) {
            settingsDao.update(settings[0]);
            return null;
        }
    }
    private static class DeleteCourseAsyncTask extends AsyncTask<Setting, Void, Void> {
        private SettingsDao settingsDao;
        private Integer id;

        private DeleteCourseAsyncTask(SettingsDao dao, Integer id) {
            this.settingsDao = dao;
            this.id = id;
        }

        @Override
        protected Void doInBackground(Setting... settings) {
            settingsDao.delete(id);
            return null;
        }
    }
    private static class DeleteAllSettingsAsyncTask extends AsyncTask<Void, Void, Void> {
        private SettingsDao settingsDao;
        private DeleteAllSettingsAsyncTask(SettingsDao dao) {
            this.settingsDao = dao;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            settingsDao.deleteAllSettings();
            return null;
        }
    }
}

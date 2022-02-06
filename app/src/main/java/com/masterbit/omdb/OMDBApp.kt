package com.masterbit.omdb

import android.app.Application
import com.masterbit.omdb.local.MovieDatabase

class OMDBApp : Application() {
    lateinit var movieDatabase: MovieDatabase
    override fun onCreate() {
        super.onCreate()
        movieDatabase = MovieDatabase.getDatabase(this)
    }
}
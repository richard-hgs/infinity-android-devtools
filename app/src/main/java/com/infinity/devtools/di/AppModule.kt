package com.infinity.devtools.di

import android.content.Context
import com.infinity.devtools.MyApplication
import com.infinity.devtools.domain.database.AppDatabase
import com.infinity.devtools.domain.database.MysqlConnDao
import com.infinity.devtools.domain.repository.MysqlConnRepo
import com.infinity.devtools.domain.repository.MysqlConnRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun providesAppInstance(@ApplicationContext context: Context) : MyApplication {
        return context as MyApplication
    }

    @Provides
    @Singleton
    fun providesAppDatabase(context: MyApplication) : AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideMysqlConnDao(
        appDb: AppDatabase
    ) = appDb.getMysqlConnDao()

    @Provides
    fun provideMysqlConnRepository(
        mysqlConnDao: MysqlConnDao
    ): MysqlConnRepo = MysqlConnRepoImpl(
        connDao = mysqlConnDao
    )
}
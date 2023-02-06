package com.infinity.devtools.di

import android.content.Context
import com.infinity.devtools.MyApplication
import com.infinity.devtools.domain.database.AppDatabase
import com.infinity.devtools.domain.database.MysqlConnDao
import com.infinity.mysql.MysqlOdbc
import com.infinity.mysql.MysqlOdbcImpl
import com.infinity.devtools.domain.repository.MysqlConnRepo
import com.infinity.devtools.domain.repository.MysqlConnRepoImpl
import com.infinity.devtools.domain.resources.ResourcesProvider
import com.infinity.devtools.domain.resources.ResourcesProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by richard on 04/02/2023 23:42
 *
 * Application module of Hilt Dependency Injection
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    /**
     * Provides application context instance for injection
     *
     * @param context   Application context
     * @return [MyApplication] instance
     */
    @Provides
    @Singleton
    fun providesAppInstance(@ApplicationContext context: Context) : MyApplication {
        return context as MyApplication
    }

    /**
     * Provides application database instance for injection
     *
     * @param context Application context where database use
     * @return [AppDatabase] instance
     */
    @Provides
    @Singleton
    fun providesAppDatabase(context: MyApplication) : AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    /**
     * Provides [AppDatabase.getMysqlConnDao]  instance for injection
     *
     * @param appDb Application database instance where dao get is found
     * @return [MysqlConnDao] Mysql dao instance
     */
    @Provides
    fun provideMysqlConnDao(
        appDb: AppDatabase
    ) = appDb.getMysqlConnDao()

    /**
     * Provides [MysqlConnRepo] instance for injection
     *
     * @param mysqlConnDao [MysqlConnDao] instance
     * @return [MysqlConnRepo] instance
     */
    @Provides
    fun provideMysqlConnRepository(
        mysqlConnDao: MysqlConnDao
    ): MysqlConnRepo = MysqlConnRepoImpl(
        connDao = mysqlConnDao
    )

    /**
     * Provides resource provider instance for injection
     *
     * @param context [MyApplication] Application context
     * @return [ResourcesProvider] Resources provider instance
     */
    @Provides
    @Singleton
    fun provideResourcesProvider(
        context: MyApplication
    ) : ResourcesProvider = ResourcesProviderImpl(context = context)

    /**
     * Provides [MysqlOdbc] instance for injection
     *
     * @return [MysqlOdbc] instance
     */
    @Provides
    @Singleton
    fun provideMysqlOdbc() : MysqlOdbc {
        return MysqlOdbcImpl()
    }
}
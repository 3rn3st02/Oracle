package com.oraculo.app.data.local.chat.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.oraculo.app.data.local.chat.db.dao.ConversationDao
import com.oraculo.app.data.local.chat.db.dao.MessageDao
import com.oraculo.app.data.local.chat.db.dao.SourceDao
import com.oraculo.app.data.local.chat.db.entities.ConversationEntity
import com.oraculo.app.data.local.chat.db.entities.MessageEntity
import com.oraculo.app.data.local.chat.db.entities.SourceEntity

/*
 * Base de datos local del modo conversación de ORACLE.
 *
 * Guarda:
 * - conversaciones locales
 * - mensajes del usuario
 * - respuestas del Oráculo
 * - request_id devuelto por /ask/stream
 * - feedback local
 * - fuentes asociadas a respuestas
 *
 * Esta persistencia local permite conservar historial en el dispositivo
 * aunque el backend borre historial automáticamente cada 90 días.
 */
@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        SourceEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class OracleChatDatabase : RoomDatabase() {

    /*
     * DAO de conversaciones.
     */
    abstract fun conversationDao(): ConversationDao

    /*
     * DAO de mensajes.
     */
    abstract fun messageDao(): MessageDao

    /*
     * DAO de fuentes.
     */
    abstract fun sourceDao(): SourceDao

    companion object {

        /*
         * Nombre físico del archivo SQLite local.
         */
        private const val DATABASE_NAME = "oracle_chat_database.db"

        /*
         * Instancia singleton.
         *
         * Volatile evita problemas de visibilidad entre hilos.
         */
        @Volatile
        private var INSTANCE: OracleChatDatabase? = null

        /*
         * Devuelve una única instancia de la base de datos.
         *
         * Usamos applicationContext para evitar fugas de memoria
         * ligadas a Activity.
         */
        fun getInstance(context: Context): OracleChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OracleChatDatabase::class.java,
                    DATABASE_NAME
                )
                    /*
                     * Durante desarrollo permitimos migración destructiva.
                     *
                     * IMPORTANTE:
                     * En versiones futuras, cuando la app ya tenga usuarios reales,
                     * convendrá reemplazar esto por migraciones formales.
                     */
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
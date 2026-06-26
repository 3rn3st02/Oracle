package com.oraculo.app.data.local.chat.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.oraculo.app.data.local.chat.db.entities.SourceEntity
import kotlinx.coroutines.flow.Flow

/*
 * DAO para fuentes asociadas a respuestas.
 *
 * El backend puede devolver sources con:
 * - label
 * - section
 *
 * Estas fuentes quedan asociadas al mensaje ASSISTANT.
 */
@Dao
interface SourceDao {

    /*
     * Inserta una fuente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceEntity)

    /*
     * Inserta varias fuentes.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<SourceEntity>)

    /*
     * Observa fuentes por mensaje.
     */
    @Query(
        """
        SELECT * FROM message_sources
        WHERE messageId = :messageId
        ORDER BY id ASC
        """
    )
    fun observeSourcesByMessage(
        messageId: String
    ): Flow<List<SourceEntity>>

    /*
     * Obtiene fuentes de un mensaje una sola vez.
     */
    @Query(
        """
        SELECT * FROM message_sources
        WHERE messageId = :messageId
        ORDER BY id ASC
        """
    )
    suspend fun getSourcesByMessage(
        messageId: String
    ): List<SourceEntity>

    /*
     * Elimina fuentes de un mensaje.
     */
    @Query(
        """
        DELETE FROM message_sources
        WHERE messageId = :messageId
        """
    )
    suspend fun deleteSourcesByMessage(messageId: String)
}
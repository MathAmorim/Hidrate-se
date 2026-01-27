package com.example.base.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import com.example.base.data.model.BackupData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class BackupManager(private val context: Context) {

    private val gson = Gson()
    // Hardcoded key for simplicity as per plan. In production, use Android Keystore.
    // 32 bytes for AES-256
    private val SECRET_KEY_BYTES = "HidrateSeAppBackupKey2024Secret!".toByteArray(StandardCharsets.UTF_8)
    private val ALGORITHM = "AES/GCM/NoPadding"
    private val TAG_LENGTH_BIT = 128
    private val IV_LENGTH_BYTE = 12

    suspend fun performBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val user = db.userDao().getUser()
            val records = db.waterRecordDao().getAllRecords()

            val backupData = BackupData(user, records)
            val jsonData = gson.toJson(backupData)
            
            val encryptedData = encrypt(jsonData)
            
            val fileName = "backup_hidratese_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.hds"
            val uri = saveFileToDownloads(fileName, encryptedData)
            
            if (uri != null) {
                Result.success("Backup salvo em Downloads: $fileName")
            } else {
                Result.failure(Exception("Falha ao salvar arquivo."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun performRestore(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Não foi possível abrir o arquivo."))
            
            val encryptedBytes = inputStream.readBytes()
            inputStream.close()
            
            val jsonData = decrypt(encryptedBytes)
            val backupData = gson.fromJson(jsonData, BackupData::class.java)
            
            val db = AppDatabase.getDatabase(context)
            db.runInTransaction {
                // Clear existing data? Or merge? Plan said overwrite.
                // We don't have a clearAll method in DAOs yet, let's assume we need to implement or just delete all.
                // For now, I'll use a raw query or loop delete if needed, but better to add clear methods to DAO later.
                // Actually, let's just insert with REPLACE strategy for User, but for records we might duplicate if IDs match or auto-generate.
                // To do a clean restore, we should delete all first.
                // Since I can't easily modify DAO inside this transaction block without casting or access, 
                // I will do it via the DAO methods which are suspend functions, so I can't use runInTransaction easily with them directly if they are suspend.
                // But Room runInTransaction takes a Runnable.
                // Let's do it sequentially.
            }
            
            // Manual "transaction" logic
            // 1. Delete all
            // We need to add deleteAll to DAOs or use raw query.
            // Since I didn't add deleteAll to DAOs in the plan, I'll use a workaround or add them now if I can.
            // I'll assume I can add them or use a raw query via `db.openHelper.writableDatabase`.
            
            // Safe approach: Delete all records one by one or via a new DAO method if I modify it.
            // Let's modify DAOs in next steps to support clearing. For now, I will try to use what I have.
            // WaterRecordDao has no delete all.
            // I will implement a `clearAllTables` logic using Room's built-in method if available or just raw SQL.
            
            db.clearAllTables() // This is available in RoomDatabase!
            
            // 2. Insert new
            if (backupData.user != null) {
                db.userDao().insertUser(backupData.user)
            }
            
            backupData.records.forEach { record ->
                // We want to keep the original IDs or let them regenerate?
                // If we keep IDs, we might have conflicts if we didn't clear properly.
                // Since we cleared all tables, we can insert them as is.
                db.waterRecordDao().insert(record)
            }
            
            // Achievements are recalculated automatically based on records.
            
            Result.success("Dados restaurados com sucesso!")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Erro ao restaurar: Arquivo inválido ou senha incorreta."))
        }
    }

    private fun encrypt(data: String): ByteArray {
        val cipher = Cipher.getInstance(ALGORITHM)
        val keySpec = SecretKeySpec(SECRET_KEY_BYTES, "AES")
        
        // Generate random IV
        val iv = ByteArray(IV_LENGTH_BYTE)
        java.security.SecureRandom().nextBytes(iv)
        val ivSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val cipherText = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))
        
        // Prepend IV to cipherText
        val output = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, output, 0, iv.size)
        System.arraycopy(cipherText, 0, output, iv.size, cipherText.size)
        
        return output
    }

    private fun decrypt(encryptedData: ByteArray): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        val keySpec = SecretKeySpec(SECRET_KEY_BYTES, "AES")
        
        // Extract IV
        val iv = ByteArray(IV_LENGTH_BYTE)
        System.arraycopy(encryptedData, 0, iv, 0, iv.size)
        val ivSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        
        // Extract CipherText
        val cipherTextSize = encryptedData.size - iv.size
        val cipherText = ByteArray(cipherTextSize)
        System.arraycopy(encryptedData, iv.size, cipherText, 0, cipherTextSize)
        
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        val plainTextBytes = cipher.doFinal(cipherText)
        
        return String(plainTextBytes, StandardCharsets.UTF_8)
    }

    private fun saveFileToDownloads(fileName: String, data: ByteArray): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        return if (uri != null) {
            try {
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                outputStream?.use { it.write(data) }
                uri
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                null
            }
        } else {
            null
        }
    }
}

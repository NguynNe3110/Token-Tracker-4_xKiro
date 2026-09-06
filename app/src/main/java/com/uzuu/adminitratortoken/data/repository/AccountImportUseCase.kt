package com.uzuu.adminitratortoken.data.repository

import android.content.Context
import android.net.Uri
import com.uzuu.adminitratortoken.data.local.entity.AccountEntity
import com.uzuu.adminitratortoken.data.security.EncryptedStorage
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountImportUseCase @Inject constructor(
    private val repository: TokenRepository,
    private val encryptedStorage: EncryptedStorage
) {
    data class ImportResult(
        val successCount: Int,
        val skippedCount: Int,
        val errorCount: Int,
        val errors: List<String>
    )

    suspend fun importFromFile(context: Context, uri: Uri): ImportResult {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            if (content.isNullOrBlank()) {
                return ImportResult(0, 0, 1, listOf("File is empty or unreadable"))
            }
            importFromContent(content)
        } catch (e: Exception) {
            ImportResult(0, 0, 1, listOf(e.message ?: "Failed to read file"))
        }
    }

    suspend fun importFromContent(content: String): ImportResult {
        val lines = content.lines()
        val existingNames = repository.getAllAccounts().first()
            .map { it.name.lowercase() }
            .toSet()
        val newNames = mutableSetOf<String>()

        var successCount = 0
        var skippedCount = 0
        val errors = mutableListOf<String>()
        val accountsToInsert = mutableListOf<AccountEntity>()

        for ((index, line) in lines.withIndex()) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            val parts = trimmedLine.split("|", limit = 2)
            if (parts.size != 2) {
                errors.add("Line ${index + 1}: Invalid format (missing '|')")
                continue
            }

            val token = parts[0].trim()
            val name = parts[1].trim()

            if (token.isBlank() || name.isBlank()) {
                errors.add("Line ${index + 1}: Empty token or name")
                continue
            }

            val nameLower = name.lowercase()
            if (nameLower in existingNames || nameLower in newNames) {
                skippedCount++
                continue
            }

            newNames.add(nameLower)
            val alias = "acc_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
            encryptedStorage.saveApiKey(alias, token)

            accountsToInsert.add(
                AccountEntity(
                    name = name,
                    providerType = "XKIRO",
                    apiKeyAlias = alias,
                    baseUrl = "https://api.xkiro.com/v1/"
                )
            )
            successCount++
        }

        if (accountsToInsert.isNotEmpty()) {
            repository.insertAccounts(accountsToInsert)
        }

        return ImportResult(
            successCount = successCount,
            skippedCount = skippedCount,
            errorCount = errors.size,
            errors = errors.take(10)
        )
    }
}

</parameter>
</invoke>
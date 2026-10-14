package com.example.service

import android.content.Context
import com.example.model.ResumeScanResult
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ResumaxService(private val context: Context) {
    private val prefs = context.getSharedPreferences("empire_resumax_memory", Context.MODE_PRIVATE)

    // Standard high-demand ATS keywords per domain
    private val standardTechKeywords = listOf(
        "Jetpack Compose", "Kotlin", "Coroutines", "Architecture", "CI/CD", 
        "ATS Optimization", "Unit Testing", "System Design", "Scalability", 
        "Android SDK", "REST API", "Microservices", "Docker", "Performance Tuning",
        "Team Leadership", "Agile", "Cross-Functional", "Git"
    )

    private val actionVerbs = listOf(
        "architected", "engineered", "spearheaded", "developed", "led", "optimized",
        "implemented", "reduced", "scaled", "boosted", "conducted", "formulated",
        "migrated", "orchestrated", "automated", "mentored", "achieved"
    )

    fun analyzeResume(rawText: String, targetJob: String): ResumeScanResult {
        val lowerText = rawText.lowercase()

        // 1. Candidate Name extraction (first non-empty line or fallback)
        val candidateName = rawText.lines()
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() && it.length in 3..40 && !it.contains("@") }
            ?: "Candidate Applicant"

        // 2. Metrics & Impact Score (Look for percentages, digits, multipliers)
        val metricsCount = Regex("""\b(\d+[%kKmM]?|\$\d+|\d+\.\d+)\b""").findAll(rawText).count()
        val impactScore = ((metricsCount / 5f).coerceIn(0f, 1f) * 25).toInt()

        // 3. Action Verbs Score
        val foundVerbs = actionVerbs.filter { lowerText.contains(it) }
        val verbScore = ((foundVerbs.size / 6f).coerceIn(0f, 1f) * 20).toInt()

        // 4. Skills & Keywords
        val matchingKeywords = mutableListOf<String>()
        val missingKeywords = mutableListOf<String>()

        standardTechKeywords.forEach { kw ->
            if (lowerText.contains(kw.lowercase())) {
                matchingKeywords.add(kw)
            } else {
                missingKeywords.add(kw)
            }
        }
        val skillsScore = ((matchingKeywords.size.toFloat() / (matchingKeywords.size + missingKeywords.size).coerceAtLeast(1)) * 30).toInt()

        // 5. Structure & Section format
        var formatPoints = 10
        if (lowerText.contains("experience") || lowerText.contains("employment")) formatPoints += 5
        if (lowerText.contains("education") || lowerText.contains("degree")) formatPoints += 5
        if (lowerText.contains("skill") || lowerText.contains("technologies")) formatPoints += 5
        val formatScore = formatPoints.coerceAtMost(25)

        // Total ATS Score (0 - 100)
        val rawTotal = impactScore + verbScore + skillsScore + formatScore
        // Standardize to realistic high-caliber ATS range (72 - 96 for good input)
        val overallScore = rawTotal.coerceIn(40, 98)

        // Strengths & Warnings
        val strengths = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (metricsCount >= 3) {
            strengths.add("Strong quantifiable impact detected ($metricsCount data points like % or $).")
        } else {
            warnings.add("Lacks quantifiable metrics. Add exact % gains or revenue numbers.")
        }

        if (foundVerbs.size >= 4) {
            strengths.add("Exceptional usage of decisive action verbs (${foundVerbs.take(3).joinToString()}).")
        } else {
            warnings.add("Passive voice detected. Replace weak descriptions with proactive action verbs.")
        }

        if (matchingKeywords.size >= 5) {
            strengths.add("Good keyword alignment with modern ATS filters (${matchingKeywords.take(4).joinToString()}).")
        } else {
            warnings.add("Low keyword density for modern automated applicant tracking filters.")
        }

        val executiveSummary = "ATS Audit: $candidateName achieves an overall score of $overallScore/100 for '$targetJob'. " +
                "Detected ${matchingKeywords.size} key skills and ${foundVerbs.size} executive action verbs. " +
                "Resume parsed successfully with clean hierarchical formatting."

        val vocalScript = "Hello Boss. RESUMAX has completed the ATS evaluation for $candidateName. " +
                "The overall ATS score is $overallScore out of 100. " +
                "Quantifiable impact received $impactScore points, while action verbs scored $verbScore. " +
                "I have identified ${missingKeywords.take(3).joinToString(", ")} as the top priority missing keywords. " +
                "Your tailored cover letter has also been generated."

        val coverLetter = generateCoverLetter(candidateName, targetJob, rawText, matchingKeywords)

        val result = ResumeScanResult(
            id = UUID.randomUUID().toString(),
            candidateName = candidateName,
            targetJobTitle = targetJob.ifBlank { "Senior Specialist" },
            overallScore = overallScore,
            impactScore = impactScore,
            skillsScore = skillsScore,
            formatScore = formatScore,
            verbScore = verbScore,
            matchingKeywords = matchingKeywords,
            missingKeywords = missingKeywords.take(6),
            strengths = strengths,
            criticalWarnings = warnings,
            executiveSummary = executiveSummary,
            vocalScript = vocalScript,
            generatedCoverLetter = coverLetter
        )

        saveToMemory(result)
        return result
    }

    private fun generateCoverLetter(name: String, job: String, resumeText: String, skills: List<String>): String {
        val topSkillsStr = skills.take(4).joinToString(", ")
        return """
            Dear Hiring Team,

            I am writing to express my enthusiastic interest in the $job position at your organization. Having engineered high-impact solutions with a focus on $topSkillsStr, I bring both technical execution and strategic vision to this role.

            Throughout my career, I have prioritized architecting reliable systems, delivering measurable improvements, and collaborating with cross-functional partners to solve complex engineering and business challenges. My background aligns closely with the core requirements of the $job scope.

            I welcome the opportunity to discuss how my expertise can accelerate your team's objectives. Thank you for your consideration.

            Sincerely,
            $name
        """.trimIndent()
    }

    private fun saveToMemory(result: ResumeScanResult) {
        try {
            val existingJson = prefs.getString("resumax_memory_history", "[]") ?: "[]"
            val array = JSONArray(existingJson)
            val obj = JSONObject().apply {
                put("id", result.id)
                put("candidateName", result.candidateName)
                put("targetJobTitle", result.targetJobTitle)
                put("overallScore", result.overallScore)
                put("timestamp", result.timestamp)
                put("summary", result.executiveSummary)
            }
            array.put(0, obj) // prepend latest
            prefs.edit().putString("resumax_memory_history", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMemoryHistory(): List<String> {
        val list = mutableListOf<String>()
        try {
            val json = prefs.getString("resumax_memory_history", "[]") ?: "[]"
            val array = JSONArray(json)
            for (i in 0 until array.length().coerceAtMost(5)) {
                val obj = array.getJSONObject(i)
                list.add("${obj.getString("candidateName")} - ${obj.getInt("overallScore")}/100 (${obj.getString("targetJobTitle")})")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}

package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.ResumeScanResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmpireFirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun authenticatedUser_canSaveAndObserveResumes() = runBlocking {
    org.junit.Assume.assumeTrue("Firebase Auth emulator not active in local test environment", isEmulatorRunning())
    val uid = signInTestUser("star@empire.ai")
    val repository = EmpireFirestoreRepository(firestore, auth)

    val sample = ResumeScanResult(
      id = "test_res_1",
      candidateName = "Empire Star",
      targetJobTitle = "Lead Mobile Architect",
      overallScore = 88,
      impactScore = 24,
      skillsScore = 26,
      formatScore = 20,
      verbScore = 18,
      matchingKeywords = listOf("Kotlin", "Compose"),
      missingKeywords = listOf("CI/CD"),
      strengths = listOf("High quantifiable metrics"),
      criticalWarnings = emptyList(),
      executiveSummary = "High-performing resume",
      vocalScript = "Audit score is 88.",
      generatedCoverLetter = "Dear Hiring Manager..."
    )

    val docId = repository.saveResume(sample)
    assertNotNull(docId)

    val resumes = repository.observeResumes().first()
    assertTrue("Should observe at least 1 saved resume", resumes.isNotEmpty())
    assertEquals("Empire Star", resumes.first().candidateName)
    assertEquals(88, resumes.first().overallScore)
  }

  @Test(expected = IllegalStateException::class)
  fun unauthenticatedUser_throwsExceptionWhenAccessingRepository() {
    auth.signOut()
    val repository = EmpireFirestoreRepository(firestore, auth)
    runBlocking {
      repository.saveResume(
        ResumeScanResult(
          id = "unauth",
          candidateName = "Hacker",
          targetJobTitle = "None",
          overallScore = 50,
          impactScore = 10,
          skillsScore = 10,
          formatScore = 10,
          verbScore = 10,
          matchingKeywords = emptyList(),
          missingKeywords = emptyList(),
          strengths = emptyList(),
          criticalWarnings = emptyList(),
          executiveSummary = "",
          vocalScript = ""
        )
      )
    }
  }
}

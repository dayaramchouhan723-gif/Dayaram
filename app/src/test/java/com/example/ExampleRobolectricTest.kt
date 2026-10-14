package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AgentProfiles
import com.example.model.SampleResumes
import com.example.service.ResumaxService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CHOUHAN EMPIRE", appName)
  }

  @Test
  fun `verify 11 agents are initialized`() {
    val agents = AgentProfiles.allAgents
    assertEquals(11, agents.size)
    assertNotNull(AgentProfiles.getAgentById("resumax"))
    assertNotNull(AgentProfiles.getAgentById("myra"))
    assertNotNull(AgentProfiles.getAgentById("kayra"))
    assertNotNull(AgentProfiles.getAgentById("maya"))
    assertNotNull(AgentProfiles.getAgentById("jarvis"))
    assertNotNull(AgentProfiles.getAgentById("zoya"))
    assertNotNull(AgentProfiles.getAgentById("vyron"))
    assertNotNull(AgentProfiles.getAgentById("archer"))
    assertNotNull(AgentProfiles.getAgentById("nova"))
    assertNotNull(AgentProfiles.getAgentById("stonic"))
    assertNotNull(AgentProfiles.getAgentById("luna"))
  }

  @Test
  fun `verify resumax ATS calculation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val service = ResumaxService(context)
    val sample = SampleResumes.samples.first()
    val result = service.analyzeResume(sample.rawText, sample.jobTitle)

    assertTrue("Score should be between 40 and 100", result.overallScore in 40..100)
    assertTrue("Should detect matching keywords", result.matchingKeywords.isNotEmpty())
    assertNotNull(result.generatedCoverLetter)
  }
}

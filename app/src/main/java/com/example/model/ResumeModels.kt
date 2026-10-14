package com.example.model

data class ResumeScanResult(
    val id: String,
    val candidateName: String,
    val targetJobTitle: String,
    val overallScore: Int,
    val impactScore: Int,
    val skillsScore: Int,
    val formatScore: Int,
    val verbScore: Int,
    val matchingKeywords: List<String>,
    val missingKeywords: List<String>,
    val strengths: List<String>,
    val criticalWarnings: List<String>,
    val executiveSummary: String,
    val vocalScript: String,
    val generatedCoverLetter: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class SampleResumeData(
    val label: String,
    val jobTitle: String,
    val rawText: String
)

object SampleResumes {
    val samples = listOf(
        SampleResumeData(
            label = "Senior Android / AI Engineer",
            jobTitle = "Senior Android & AI Lead",
            rawText = """
                ALEXANDER CHOUHAN
                alexander.chouhan@empire.ai | +971 50 123 4567 | Dubai, UAE | linkedin.com/in/chouhan-empire
                
                SUMMARY
                Results-driven Senior Android Engineer with 7+ years of experience architecting high-performance mobile systems, Jetpack Compose UI, and on-device generative AI integrations. Led multi-agent mobile development serving 2.5M+ active users.

                EXPERIENCE
                Lead Mobile Architect — EMPIRE TECH (2022 - Present)
                - Architected scalable Jetpack Compose architecture with M3 design system, reducing app cold start time by 42%.
                - Integrated Gemini API, Groq LLM streaming, and local vector search, boosting user session duration by 65%.
                - Mentored 12 mobile engineers and implemented CI/CD pipelines achieving 99.98% crash-free sessions.
                - Authored end-to-end Kotlin Coroutines and Flow data pipelines handling 100k+ concurrent telemetry signals.

                Senior Software Engineer — APEX GLOBAL (2019 - 2022)
                - Spearheaded migration from legacy XML to Jetpack Compose across 40+ production screens.
                - Reduced APK size by 35% through Proguard optimization, R8 rule tuning, and modular dynamic feature delivery.
                - Engineered offline-first Room database synchronization with optimistic conflict resolution.

                SKILLS
                Languages: Kotlin, Java, Coroutines, Flow, Rust
                Frameworks & Tools: Jetpack Compose, Material Design 3, Retrofit, Room, Hilt, Firebase, Android SDK, Git
                AI & Systems: Gemini API, Prompt Engineering, ATS Systems, Multi-Agent Architecture, LLM Streaming
                
                EDUCATION
                B.S. in Computer Science — Imperial Institute of Technology (2015 - 2019)
            """.trimIndent()
        ),
        SampleResumeData(
            label = "Product & Marketing Strategist",
            jobTitle = "Executive Product Growth Strategist",
            rawText = """
                SAMIRA AL-MANSOOR
                samira.m@growth.ae | +971 52 987 6543 | Dubai Silicon Oasis, UAE
                
                EXECUTIVE PROFILE
                Dynamic Product Strategist with proven success launching consumer mobile apps and AI agents across Middle East and Europe. Increased gross revenue by 140% in 18 months through data-backed product experiments.

                EXPERIENCE
                VP of Product Growth — FALCON ENTERPRISES (2021 - Present)
                - Formulated go-to-market strategy for enterprise AI assistant, acquiring 500k subscribers in under 9 months.
                - Conducted continuous A/B testing on onboarding funnels, elevating conversion rate from 14% to 38%.
                - Managed a cross-functional squad of 18 designers, data scientists, and engineers.

                SKILLS & COMPETENCIES
                Strategic Planning, A/B Testing, User Acquisition, Product Analytics, SQL, Tableau, Growth Hacking, Dubai Market Dynamics.
            """.trimIndent()
        )
    )
}

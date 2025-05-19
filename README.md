Project Overview:
StudyPal is a comprehensive exam preparation tool built using modern Android development technologies. The application helps students organize their study schedule, track multiple subjects, and generate personalized revision plans using AI.

Key Features:
1. Subject Management
   - Add multiple subjects with their respective exam dates
   - Real-time synchronization with Firebase Firestore
   - Subjects are automatically sorted by exam dates for better visibility

2. AI-Powered Revision Planning
   - Generates personalized study schedules using Google's Gemini AI
   - Creates day-by-day revision plans considering:
     * Multiple subjects
     * Time gaps between exams
     * Different study sessions (Morning, Afternoon, Evening)
     * Break times and study techniques

3. User Interface
   - Modern Material 3 design
   - Intuitive navigation with back buttons
   - Real-time loading states and error handling
   - Profile management and authentication

Technical Implementation:
- Built with Kotlin and Jetpack Compose
- Uses MVVM architecture pattern
- Implements Firebase Authentication and Firestore
- Integrates Google's Gemini AI for plan generation
- Features comprehensive error handling and logging
- Implements Hilt for dependency injection

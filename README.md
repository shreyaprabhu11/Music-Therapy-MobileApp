# raga-therapy

Medical Therapy Application

A comprehensive Android-based Medical Therapy Application designed to support patients through music therapy. The application enables patients to listen to therapeutic music, book appointments with consultants, receive personalized recommendations, and monitor their therapy progress. Administrators manage users and analyze listening data, while consultants oversee appointments and patient progress.

---

📋 Table of Contents

- Project Overview
- Features
- Technology Stack
- System Architecture
- User Roles
- Modules
- Application Workflow
- Database Overview
- Installation
- Future Enhancements
- Team Members

---

📖 Project Overview

The Medical Therapy Application is developed to assist patients undergoing music therapy by providing an interactive platform for therapeutic music sessions, appointment scheduling, and progress tracking.

The system consists of three user roles:

- Patient
- Consultant (Expert)
- Administrator

Patients can listen to therapeutic music, book consultation appointments, receive notifications, and manage their profiles. Consultants can manage appointments and monitor patient progress, while administrators oversee the complete system.

---

✨ Features

Patient Module

- Secure Login
- Change Password on First Login
- Forgot Password with OTP Verification
- Profile Management
- Music Library
- Music Player
- Daily Notifications
- Book Therapist Appointment
- View Consultation Status
- Submit Feedback
- Light/Dark Mode
- Language Selection
- Adjustable Font Size

---

Consultant Module

- Secure Login
- Manage Daily Availability
- Holiday Mode
- View Assigned Patients
- Generate Google Meet Links
- Manage Consultation Appointments
- Receive Appointment Notifications
- View Patient Music Analytics

---

Admin Module

- Register Patients
- Create Temporary Credentials
- Manage Patient Information
- Search Patients
- View Listening Statistics
- Assign Consultants
- View Dashboard
- Generate Excel Reports
- Analyze Music Usage
- View Feedback
- Patient Grouping by Location

---

🛠 Technology Stack

Frontend

- Android (Java)
- XML

Backend

- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Firebase Cloud Messaging

Media

- ExoPlayer

Additional Services

- Google Meet Integration
- Push Notifications

---

🏗 System Architecture

Patients / Consultants / Admin
             │
             ▼
      Android Application
             │
             ▼
 Firebase Authentication
             │
             ▼
 Firebase Firestore Database
             │
             ▼
 Firebase Storage
             │
             ▼
     ExoPlayer Media Engine

---

👥 User Roles

Patient

- Listen to therapeutic music
- Book appointments
- View therapist details
- Update profile
- Receive notifications
- Submit feedback

Consultant

- Manage consultation slots
- Accept appointments
- Create Google Meet links
- View patient analytics

Administrator

- Register patients
- Manage consultants
- Assign therapists
- Generate reports
- Monitor user activity
- Analyze listening statistics

---

📦 Modules

Authentication Module

- Role Selection
- Login
- Forgot Password
- OTP Verification
- Password Reset

Profile Module

- View Profile
- Edit Profile
- Settings
- Feedback

Music Module

- Music Library
- Music Categories
- Music Player
- Play/Pause
- Next/Previous

Appointment Module

- Therapist Listing
- Slot Booking
- Appointment Confirmation
- Google Meet Integration

Notification Module

- Morning Reminder
- Evening Reminder
- Appointment Reminder

Analytics Module

- Music Listening Statistics
- Pie Charts
- Bar Charts
- Spider Charts
- Daily Listening Report
- Excel Report Generation

---

🔄 Application Workflow

Patient Workflow

1. Login
2. Complete password setup
3. Browse music
4. Listen to therapy sessions
5. Book therapist appointment
6. Attend online consultation
7. Continue therapy
8. Submit feedback

Consultant Workflow

1. Login
2. Update available slots
3. Accept appointments
4. Generate Google Meet link
5. Conduct consultation
6. Review patient progress

Admin Workflow

1. Register patients
2. Assign consultants
3. Monitor patient activity
4. Generate reports
5. Analyze listening statistics
6. Manage feedback

---

🗄 Database Overview

Collections include:

- Users
- Patients
- Consultants
- Music
- Categories
- Appointments
- Notifications
- Feedback
- Listening History

---

🚀 Installation

1. Clone the repository.
2. Open the project in Android Studio.
3. Configure Firebase Authentication.
4. Connect Firestore Database.
5. Configure Firebase Storage.
6. Enable Firebase Cloud Messaging.
7. Build and run the application.

---

🔮 Future Enhancements

- AI-based music recommendations
- Wearable device integration
- Offline music playback
- Video therapy sessions
- Voice assistant support
- Health report integration
- Mood tracking using AI
- Cloud analytics dashboard

---

👨‍💻 Team Members

- Shreya M Prabhu
- Nisarga
- Akash
- Keerthan G Rao
- Ryan Vargheese

---

📄 License

This project is developed for academic and research purposes as part of the Master of Computer Applications (MCA) program.



# User Authentication and Profile System Implementation Plan

This plan outlines the implementation of a complete user authentication, session management, profile section, and user-scoped data isolation system in Java for the Smart Pantry Manager app.

## User Review Required

> [!IMPORTANT]
> - **Data Isolation**: Pantry items will be associated with the currently logged-in user ID so that each user only sees and modifies their own pantry inventory.
> - **Local Authentication**: Accounts and sessions are stored securely using Room DB (`users` table) and SharedPreferences (`SessionManager`), ensuring offline-first reliability consistent with the app's architecture.

## Proposed Changes

### Database & Models
#### [NEW] [User.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/model/User.java)
- Room Entity for user accounts (`id`, `fullName`, `email`, `password`, `avatarUri`).

#### [NEW] [UserDao.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/db/UserDao.java)
- Room DAO for inserting and querying users by email/id.

#### [MODIFY] [PantryItem.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/model/PantryItem.java)
- Add `userId` field to bind pantry items to specific users.

#### [MODIFY] [PantryDao.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/db/PantryDao.java)
- Update queries to filter by `userId` (`WHERE userId = :userId`).

#### [MODIFY] [AppDatabase.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/db/AppDatabase.java)
- Add `User.class` to database entities, add `userDao()`, and bump version to 3 with migration handling.

### Session Management
#### [NEW] [SessionManager.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/utils/SessionManager.java)
- SharedPreferences helper to store active user email/id and session state.

### Authentication Activities & Layouts
#### [NEW] [SignInActivity.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/SignInActivity.java) & `activity_sign_in.xml`
- Sign-in screen with email, password, show/hide password toggle, input validation, and links to Sign Up.

#### [NEW] [SignUpActivity.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/SignUpActivity.java) & `activity_sign_up.xml`
- Account creation screen with full name, email, password, confirm password, and validation.

#### [NEW] [ProfileActivity.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/ProfileActivity.java) & `activity_profile.xml`
- Profile section displaying avatar, name, email, edit profile option, change password option, and sign-out button.

#### [NEW] [EditProfileActivity.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/EditProfileActivity.java) & `activity_edit_profile.xml`
- Screen to edit name, email, and avatar.

### App Integration & Personalization
#### [MODIFY] [PantryListActivity.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/PantryListActivity.java)
- Check session in `onCreate`; redirect to `SignInActivity` if not logged in.
- Display personalized greeting: "Welcome back, [Name]!".
- Pass current `userId` when adding/editing pantry items and querying DAO.

#### [MODIFY] [AddEditActivity.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/AddEditActivity.java)
- Assign `userId` to created/edited pantry items.

#### [MODIFY] [SettingsActivity.java](file:///C:/Users/riyaa/StudioProjects/Smart-Pantry-Manager/app/src/main/java/com/example/smartpantrymanager/SettingsActivity.java)
- Integrate navigation to Profile section and Sign Out.

## Verification Plan

### Automated Tests
- Run Gradle build (`app:assembleDebug`) to ensure Java compilation succeeds without errors.

### Manual Verification
- Launch app when signed out -> Verify Sign-In screen appears.
- Test Sign-Up with validation (mismatched passwords, empty fields).
- Test Sign-In with correct credentials -> Verify redirection to Home screen with personalized greeting ("Welcome back, [Name]!").
- Test pantry data isolation (different users see only their own items).
- Test Profile section (view info, edit profile, sign out).

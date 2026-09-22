# Smart Pantry Manager (Android)

**Student Name:** Riyaad [LAST NAME]  
**Module:** Mobile App Development 700 - Practical Assignment

## Concept
The **Smart Pantry Manager** helps users reduce food waste by tracking ingredients they have at home and suggesting recipes they can cook immediately. 

## The Strict-Matching Rule (Core Logic)
The core value of this application is the **Strict-Matching Logic**. A recipe is only suggested if **every single ingredient** it requires is currently present in the user's pantry.
- **Robust Matching:** The algorithm handles real-world pluralization (e.g., "tomato" vs "tomatoes") and unit differences.
- **Quantity Aware:** Suggestions only appear if the user has a sufficient quantity of the item.

## Features
- **Pantry Management:** Full CRUD (Create, Read, Update, Delete) functionality for ingredients.
- **Strict Suggestions:** A dedicated "Discover" screen that filters recipes based on your actual inventory.
- **Live Recipe API:** Integration with **TheMealDB** to pull real-world recipes and high-quality food imagery asynchronously.
- **Kitchen Profile:** Track your kitchen statistics (Items Tracked, Recipes Ready) and manage preferences.
- **Modern UI:** Designed with a premium Dark Mode theme using Material 3 and custom design tokens.

## Technical Details
- **Language:** Strictly Java (0% Kotlin).
- **Database:** **SQLite (via Room Persistence Library)**. 
  - *Why:* SQLite was chosen for its offline-first reliability and efficient local data persistence, ensuring user data is never lost when the app closes.
- **Networking:** Asynchronous background execution using `HttpURLConnection` and `ExecutorService` (No heavy 3rd-party libraries to ensure a lightweight build).
- **Architecture:** MVVM-inspired structure with isolated DAO, Model, and Network layers.

## Setup & Run Instructions
1.  **Clone the Repository:** `git clone https://github.com/Riyaad24/Smart-Pantry-Manager.git`
2.  **Open in Android Studio:** Open the project in **Android Studio Hedgehog** (or newer).
3.  **Sync Gradle:** Allow the IDE to sync dependencies from the `build.gradle` and `libs.versions.toml` files.
4.  **Internet Access:** Ensure your computer has internet access for the **TheMealDB API** to fetch live images.
5.  **Run:** Select an emulator (API Level 26 or higher) and click the **Run** button.

## GitHub History
This project followed an incremental development process with **over 10 meaningful commits** documenting the transition from a local prototype to a live, data-connected consumer application.

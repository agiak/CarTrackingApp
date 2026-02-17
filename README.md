# Car Tracking App

A comprehensive Android application for tracking car expenses, fuel consumption, maintenance, and statistics. Built with Kotlin and Jetpack Compose, this offline-first app helps users manage multiple vehicles and gain insights into their driving habits and spending patterns.

## 📱 Overview

Car Tracking App is a feature-rich mobile application designed to help car owners:
- Track fuel refills and calculate real-time consumption
- Monitor service and maintenance expenses
- Record all car-related costs (insurance, parking, tolls, etc.)
- View detailed statistics and trends through interactive charts
- Set and manage service reminders
- Import/export data for backup and sharing
- Manage multiple vehicles from a single dashboard
- View all transactions in a unified interface
- Customize appearance with multiple color themes

## ✨ Key Features

### 🚗 Multi-Vehicle Management
- Add and manage multiple cars
- Track comprehensive vehicle information including:
  - Name, license plate, and odometer readings
  - Insurance expiration dates
  - KTEO (vehicle inspection) dates
  - Emissions card expiration
  - Road tax information
  - Tire details (brand, dimensions, installation date)
  - Service history
- Edit car details with incomplete information banner
- View per-vehicle statistics and history

### ⛽ Fuel Refill Tracking
- Log fuel refills with detailed information:
  - Amount paid, liters added, trip distance
  - Automatic consumption calculation (L/100km)
  - Price per liter tracking
  - GPS location capture (optional)
  - Custom notes
  - Date and odometer reading
- **Voice Entry Support**:
  - Natural language input: "35 ευρώ, 25 λίτρα, 384 χιλιόμετρα"
  - AI-powered parsing with OpenAI GPT models
  - Supports Greek and English
  - Manual recording control (start/stop)
  - Confirmation before saving
  - Automatic field pre-filling
  - Fallback to regex parsing if AI unavailable
- Real-time odometer calculation based on trip distance
- Comprehensive validation to prevent invalid data entry
- View refill history with filtering and sorting options
- Edit and manage past refills
- Detailed refill information screens

### 💰 Expense Management
- Track various expense categories:
  - **Service-related**: Tire change, oil change, small service, big service, repairs
  - **General**: Accessories, insurance, registration, parking, tolls, car wash
  - Custom user-defined categories
- Record expense details:
  - Category, amount, date, notes
  - Optional service reminder settings
- Manage custom expense categories
- View expense history with filtering
- Edit and delete expenses
- Detailed expense information screens

### 📋 Transactions Overview
- **Unified Transaction View**: View all fuel refills and expenses in one place
- **Smart Filtering**: 
  - Filter by transaction type (fuel refills, expenses, or both)
  - Filter by car
  - Filter by expense categories
  - Visual filter chips showing active filters
- **Flexible Sorting**: Sort by date, amount, or type
- **Split View Layout**: Optimized for tablets and landscape orientation
- **Quick Actions**: Direct access to transaction details and editing
- **Summary Statistics**: Overview of total transactions and spending
- **Quick Entry**: Add transactions via home screen widgets or voice input

### 🔔 Service Reminders
- Set reminders based on:
  - **Date**: Get notified 1 day before due date
  - **Mileage**: Get notified within 500 km of target
  - **Both**: Combined date and mileage reminders
- **Notifications Overview Screen** showing:
  - All upcoming service reminders
  - Remaining kilometers until service is due
  - Days until service date
  - Active/inactive toggle per reminder
- Smart reminder management:
  - Automatic deduplication (shows only the latest reminder per service type)
  - Edit reminder dates and mileage targets
  - Enable/disable individual reminders
  - Pre-expiry notifications
- Home screen banner for today's reminders with swipe-to-dismiss
- Full notification permission management

### 📊 Statistics & Analytics
- **Global Dashboard**:
  - Total cars, refills, and expenses
  - Overall cost, distance, and fuel consumption
  - Cost per kilometer across all vehicles
  - Monthly trends with interactive charts
  
- **Per-Vehicle Statistics**:
  - Average fuel consumption
  - Total cost breakdown (refills vs. expenses)
  - Total distance traveled
  - Number of refills and services
  - Average price per liter
  - Recent refills and expenses

- **Interactive Graphs** (with car and time filtering):
  - **Distance Graph**: Monthly distance traveled with line chart visualization
  - **Consumption Graph**: Fuel consumption trends over time
  - **Cost Graph**: Total cost analysis including refills and expenses
  - **Refills Graph**: Refilling patterns and frequency
  - Tap interaction to view exact values
  - Time period filters: 6 months, 1 year, all time
  - Multi-car selection for aggregated statistics
  - Export-ready visualizations

- **Advanced Comparisons**:
  - **Year-to-Year Comparison**:
    - Select and compare any two years
    - Side-by-side metrics display
    - Total cost, distance, avg consumption, cost per km
    - Percentage change with directional indicators
    - Optional overlay line chart showing monthly trends
    - Semantic coloring for improvements/increases
    - Automatic comparison year detection
  
  - **Car-to-Car Comparison**:
    - Compare two or more vehicles
    - All cars overview mode
    - Cost per km ranking
    - Fuel efficiency analysis
    - Maintenance cost per year
    - AI-generated summary insights
    - Best/worst performer highlighting
    - Dynamic percentage differences

- **Monthly Details View**:
  - Detailed breakdown of specific months
  - Refills and expenses grouped by date
  - Monthly totals and averages
  - **Interactive Tooltips**: Info tooltips explaining metrics like "Fuel as % of spending"
    - Inline, non-intrusive design
    - Anchored to field with arrow pointer
    - Tap to show/hide
    - Outside tap to dismiss

- **Monthly Trends Screen**:
  - Comprehensive monthly analysis
  - Sorting by time, cost, distance, or transactions
  - Time period filtering (3 months, 6 months, 1 year, all time)
  - Per-month summary cards

### 📥 Import/Export Features
- **JSON Export/Import**:
  - Complete data backup in JSON format
  - Export all cars, refills, and expenses
  - Import data with validation
  - Maintains data integrity and relationships
  
- **Spreadsheet Import (Excel/CSV)**:
  - Import data from Excel (.xlsx) or CSV files
  - Structured import with predefined column headers
  - Sample file generation for easy template creation
  - Separate sheets for Cars, Refills, and Expenses
  - Validation and error handling
  - Cars matched by license plate
  
- **Data Management**:
  - Storage usage information
  - Clear all data option with confirmation
  - Safe import/export with error handling

### 🎓 Onboarding Experience
- Welcome guide for new users
- Feature highlights across multiple slides:
  - Welcome and app overview
  - Fuel tracking capabilities
  - Service and maintenance tracking
  - Expense management
  - Statistics and insights
- Permission requests:
  - Location permission (for GPS-tagged refills)
  - Notification permission (for service reminders)
- Graceful handling of denied permissions
- Skip option available

### 🎨 User Interface & Theming
- **Modern Material 3 Design** with dynamic theming
- **Extensive Color Customization**: 
  - 30+ built-in color palettes including:
    - System colors (Android 12+ Dynamic Colors)
    - Classic themes: Blue, Orange, Green, Purple, Teal, Red, etc.
    - Modern themes: Neon, Cyber, Electric, Midnight, Ice, etc.
    - High-contrast themes: Sunset Fire, Tropical Paradise, Royal Gold, etc.
  - **Smart Background Tinting**: Backgrounds automatically tint based on selected primary color
    - Configurable tint intensity (adjustable in Settings)
    - Automatic contrast adjustment for card visibility
    - Works seamlessly in light and dark modes
    - Consistent across all screens and themes
- **Responsive Design**:
  - **Adaptive Layouts**: Different layouts for phones vs tablets
  - **Smart Split Views**: Automatic split-screen layouts on tablets and landscape phones
  - **Optimized TopBar**: Reduced padding on mobile devices in landscape mode
- **Smooth Animations**:
  - Professional slide + fade transitions between screens
  - Optimized performance (220-260ms duration)
  - Context-aware animations (horizontal for navigation, vertical for modals)
  - Smooth back navigation with reverse animations
- **Bottom Navigation**: 
  - Home (Cars List)
  - Transactions (All fuel refills and expenses)
  - Statistics
  - Settings
- **Advanced UI Features**:
  - Floating Action Buttons for quick actions
  - Swipe gestures for banner dismissal
  - Interactive tooltips and info popups
  - Smart grid layouts (1-3 columns based on device)
  - Permission-aware UI elements (voice button, etc.)
  - Context-aware displays (car selector, filters, etc.)
- **Localization**: Greek and English support with RTL preparation

### ⚙️ Settings & Customization
- **Appearance & Localization**:
  - **Theme Selection**: Light, Dark, or System theme
  - **Color Palette**: Choose from 30+ color themes
  - **Background Tint Intensity**: Adjustable color intensity for backgrounds
  - **Language Selection**: Greek and English support
  - **AI Model Selection**: Configure OpenAI model for voice parsing
    - Enter OpenAI API key
    - Choose from 5 GPT models (gpt-3.5-turbo, gpt-4o-mini, etc.)
    - View estimated costs per model
    - Switch models at runtime

- **Notifications Section**:
  - Enable/disable notifications globally
  - View upcoming service reminders
  - Manage notification permissions
  - Direct link to app settings if permission denied
  
- **Data & Storage**:
  - View storage usage
  - Export/import data (JSON)
  - Spreadsheet import with sample generation
  - Clear all data with confirmation
  
- **Customization**:
  - **Expense Categories Management**: Add, edit, and delete custom expense categories
  - **Default Car Setting**: Set one car as default for quick entry
    - Automatically selected in add screens
    - Visual badge in car list
  
- **Help & About**:
  - App version information
  - View onboarding guide again
  
- **Developer Tools** (Debug builds only):
  - Generate sample data for testing
  - Pre-populate database with realistic data
  - Test reminders with various scenarios

## 🏗️ Technical Architecture

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture with MVVM
- **Dependency Injection**: Hilt/Dagger
- **Database**: Room (SQLite)
- **Asynchronous**: Coroutines + Flow
- **Navigation**: Jetpack Navigation Compose
- **Data Storage**: DataStore Preferences
- **Background Tasks**: WorkManager

### Advanced Features
- **Voice Recognition**: OpenAI GPT integration for natural language refill entry
- **Speech-to-Text**: Android SpeechRecognizer API for voice input
- **Home Screen Widgets**: Glance-based widgets for quick entry
- **Device Detection**: Smart tablet vs phone detection using smallest width
- **Location Services**: GPS integration for refill locations
- **File Handling**: Apache POI for Excel/CSV processing
- **Serialization**: Kotlinx Serialization for JSON export/import
- **Theming System**: Advanced color palette management with dynamic backgrounds
- **Responsive Design**: Automatic layout adaptation for different screen sizes
- **Navigation Animations**: Professional slide + fade transitions

### Key Components
- **Domain Layer**: Use cases, models, repositories
  - Voice recognition use cases (ParseVoiceRefillUseCase)
  - Centralized business logic (AddFuelRefillUseCase, AddExpenseUseCase)
  - Validation (RefillValidator, ExpenseValidator)
- **Data Layer**: 
  - Local database with Room
  - DAOs for Cars, Refills, Expenses, Reminders
  - Foreign key relationships with cascade delete
  - Data mappers between entities and domain models
  - Preferences management with DataStore
- **Presentation Layer**: 
  - ViewModels with StateFlow
  - Composable UI components with previews
  - Navigation graph with type-safe arguments
  - Reusable component library (StyledCard, StyledTopAppBar, etc.)
- **Widget Layer**:
  - Glance-based widgets (QuickAddWidget, RefillWidget, ExpenseWidget)
  - Permission-aware widget rendering
  - Widget action callbacks and data updates
- **Services**:
  - SpeechRecognitionService for voice input
  - LocationProvider for GPS tracking
  - NotificationManager for reminders
- **Utilities**:
  - DeviceUtils for proper tablet/landscape detection
  - Data validation (RefillValidator, ExpenseValidator)
  - Import/export managers (JSON, Excel/CSV)
  - AI parsing with OpenAI integration

### Data Models
- **Car**: Vehicle information with statistics
- **FuelRefill**: Refill details with consumption calculations
- **Expense**: General expenses with optional reminder settings
- **ExpenseReminder**: Service reminder configuration
- **Statistics**: Aggregated data for charts and analytics
- **Transaction**: Unified model for refills and expenses display

### Offline-First Design
- All data stored locally in Room database
- No internet connection required
- Data persistence across app restarts
- Export for backup and sharing

## 🛡️ Data Validation & Edge Cases

The app includes comprehensive validation and edge case handling:

### Input Validation
- **Refills**:
  - Amount, liters, and distance must be positive
  - Liters: 0.1 - 2000 L (reasonable range for cars)
  - Cost: 0.01 - 10,000 € (prevents accidental large values)
  - Distance: > 0 km, warning if > 2000 km
  - Consumption must be within reasonable range (0.5 - 50 L/100km)
  - Odometer values validated (must be greater than previous)
  - Centralized validation via `RefillValidator`
  
- **Expenses**:
  - Amount must be positive
  - Category cannot be empty
  - Category name length limits
  - Validation enforced via `AddExpenseUseCase`
  
- **Cars**:
  - Name and license plate required
  - Odometer must be numeric
  - Duplicate license plate detection

### Error Handling
- **User-Friendly Messages**:
  - Localized error descriptions (Greek and English)
  - Field-specific error indicators
  - General error cards for save failures
  - Clear guidance on how to fix issues
- **Widget Error Display**:
  - Validation errors shown in quick entry dialogs
  - Field-level error highlighting
  - Automatic error clearing on correction
- **Graceful Failure**:
  - No crashes on invalid input
  - Recovery options provided
  - Transaction safety for database operations
  - Fallback mechanisms (e.g., regex parsing for voice)

### Permission Management
- **Runtime Permission Requests**:
  - Microphone (for voice entry)
  - Location (for GPS-tagged refills)
  - Notifications (for service reminders)
- **Graceful Degradation**:
  - Features hidden when permissions denied
  - Voice button only shows with microphone permission
  - GPS unavailable fallback (non-blocking)
  - Notification permission state tracking
- **Permission Recovery**:
  - Settings link for manual permission grant
  - Re-check on app resume
  - Clear user messaging

### Edge Case Handling
- **Single Car Optimization**: Car selector hidden when only one car exists
- **Default Car Logic**: Smart auto-selection (default → single → manual)
- **Widget Data Consistency**: Proper car odometer updates from widget entries
- **Permission-Aware UI**: Features conditionally shown based on permissions
- **Voice Parsing Fallback**: Regex parsing when AI unavailable
- **Offline Support**: All features work offline except AI voice parsing

## 🌍 Localization

Fully localized in:
- **English** (en)
- **Greek** (el)

All UI strings, error messages, tooltips, and content are translated.

## 📱 Supported Features by Screen

### Home (Cars List)
- View all cars with key statistics
- Quick refill and service buttons per car
- Add new car dialog
- Today's reminders banner (swipe to dismiss)
- Navigate to car details
- Adaptive grid layout (1-3 columns based on device)

### Car Details
- Comprehensive car statistics
- Recent refills and services
- Quick action buttons (Refill, Service, Edit)
- Incomplete information banner
- Navigate to detailed graphs
- Split-view layout on tablets/landscape

### Transactions Screen
- **Unified view** of all refills and expenses
- **Advanced filtering**: By type, car, and categories
- **Flexible sorting**: Date, amount, type
- **Quick actions**: View details, edit transactions
- **Summary statistics**: Total count and spending
- **Smart layout**: Split view on larger screens
- **Filter chips**: Visual indication of active filters

### Add/Edit Refill
- Input refill details with validation
- Automatic consumption calculation
- GPS location capture
- Calculated odometer display
- Trip distance assistance

### Add/Edit Expense
- Full-screen expense entry
- Custom or predefined categories (displayed as capsules)
- Service reminder toggle
- Date and mileage-based reminders
- Notes support
- Split-view layout on tablets/landscape

### Statistics
- Global statistics dashboard
- Per-car filtering
- Monthly trends list with detailed sorting options
- Navigate to detailed graphs
- Month-specific details with interactive tooltips

### Graph Screens
- Interactive line charts with tap interaction
- Car filtering (single or multiple)
- Time period filtering
- Statistics summary cards
- Recent items list
- Split-view layout optimization

### Notifications/Reminders
- All upcoming service reminders
- Enable/disable per reminder
- Edit reminder settings
- Remaining time/distance display
- Smart deduplication

### Settings
- **Organized into groups**:
  - Appearance & Localization (themes, colors, language)
  - Data & Storage (import/export, storage info)
  - Expense Categories (custom category management)
  - Help & About (app info, onboarding guide)
- **Color customization** with 30+ themes
- **Theme preview** with real-time updates
- Debug tools (dev builds)

## 🔧 Installation & Setup

**System Requirements:**
- Android 9.0 (API 28) or higher
- 50MB storage space
- Optional: Location services for GPS-tagged refills
- Optional: Notification access for service reminders

**Installation:**
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run on Android device or emulator (API 28+)

**Version Information:**
- Current Version: 1.0.3 (Build 5)
- Target SDK: 36
- Minimum SDK: 28

## 🐛 Debug Features

Development builds include:
- **Generate Sample Data**: Populate database with realistic test data
- **Test Reminders**: Create reminders with various due dates/mileages
- **Sample Spreadsheet**: Generate Excel template for import testing
- **Debug Mode Indicator**: Visual indication in settings

## 📄 License

This project is a private car expense tracking application.

## 🚀 Recent Enhancements (v1.0.3)

### ✅ Implemented Features

#### 🎤 Voice-Based Fuel Refill Entry
- **Natural Language Input**: Add refills by speaking naturally
  - Example: "35 ευρώ, 25 λίτρα, 384 χιλιόμετρα"
- **AI-Powered Parsing**: Uses OpenAI GPT models to understand spoken input
- **Multi-Language Support**: Works in both Greek and English
- **Smart Field Pre-filling**: Automatically fills form fields with parsed data
- **Confirmation Dialog**: Review and edit parsed data before saving
- **Manual Control**: User-controlled recording with explicit stop button
- **Location Integration**: Automatically captures current location (if permitted)
- **Fallback Parsing**: Regex-based fallback if AI parsing fails
- **Deterministic Rules**: Special handling for simple numeric sequences (e.g., "50 20 100")

#### 🏠 Home Screen Widgets
- **Quick Add Widget (2×2)**: Fast access to add refills and expenses
  - Refill button
  - Expense button
  - Voice button (permission-aware)
- **Single-Purpose Widgets (1×1)**:
  - Refill Widget: Direct refill entry
  - Expense Widget: Direct expense entry
- **Smart Features**:
  - Permission-aware UI (voice button only shows with microphone permission)
  - Context-aware car selection (hides when only one car exists)
  - Auto-selects default car or single available car
  - Clean, minimal design with app branding

#### 📊 Advanced Statistics
- **Year-to-Year Comparison**:
  - Compare any two years side-by-side
  - Metrics: Total cost, distance, avg consumption, cost per km
  - Percentage change indicators
  - Visual trend arrows
  - Optional overlay line chart
- **Car-to-Car Comparison**:
  - Compare multiple vehicles performance
  - Cost per km analysis
  - Fuel efficiency comparison
  - Maintenance cost per year
  - AI-generated insights and recommendations
  - Best/worst performer highlighting

#### 🎨 Enhanced Theming System
- **Dynamic Background Tinting**: Backgrounds tint based on selected primary color
- **Configurable Intensity**: Adjustable tint strength for personalization
- **Smart Contrast**: Automatic contrast adjustment for card visibility
- **Consistent Across Themes**: Works with all 30+ color palettes
- **Light & Dark Mode**: Full support for both modes

#### 🔄 Smooth Navigation Animations
- **Professional Transitions**: Slide + fade animations between screens
- **Optimized Performance**: Fast, non-intrusive animations (220-260ms)
- **Context-Aware**: Different animations for different navigation types
  - Horizontal slide for main navigation
  - Vertical slide for modal screens
- **Back Navigation**: Smooth reverse animations

#### ⚙️ Centralized Validation & Business Logic
- **Single Source of Truth**: RefillValidator for all validation rules
- **Consistent Behavior**: Same validation across all entry points
- **Use Case Architecture**:
  - AddFuelRefillUseCase: Centralized refill saving logic
  - AddExpenseUseCase: Centralized expense saving logic
- **Widget Integration**: Widgets use same validation as main app
- **Error Message Display**: Clear, user-friendly error messages in widget dialogs

#### 🚗 Default Car Feature
- **Set Default Car**: Mark one car as default for quick entry
- **Auto-Selection**: Automatically selected in add screens
- **Visual Indicator**: Subtle badge in car list
- **Smart Logic**: Fallback to single car if no default set

#### 📱 Improved Widget UX
- **Error Messages**: Validation errors displayed in widget dialogs
- **Field-Level Errors**: Specific errors shown under each input field
- **General Error Card**: Prominent error display for save failures
- **Auto-Clear**: Errors clear when user corrects input
- **Localized**: All error messages available in English and Greek

#### 🔧 Edge Case Handling
- **Permission-Aware UI**: Voice features only show when permissions granted
- **Single Car Optimization**: Car selector hidden when only one car exists
- **Widget Data Consistency**: Proper car odometer updates from widget entries
- **Validation Everywhere**: Same rules enforced across all entry points

---

## 🎤 Voice Recognition Feature - Maintenance Guide

### Overview

The Voice Recognition feature allows users to add fuel refills by speaking naturally. The system uses Android's Speech Recognition combined with OpenAI's GPT models to parse spoken input into structured refill data.

---

### How It Works

#### 1. Speech-to-Text Conversion

**Technology Used:** Android `SpeechRecognizer` API

**Process:**
1. User taps the microphone button in Add Refill screen or widget
2. App requests microphone permission (if not already granted)
3. `SpeechRecognitionService` starts listening
4. Android converts spoken audio to text
5. Supports both Greek and English language detection

**Implementation:**
- **File:** `domain/service/SpeechRecognitionService.kt`
- **Key Method:** `startListening()`
- **Lifecycle:** Properly releases resources on stop/cancel

**User Control:**
- Manual start: Tap microphone button
- Manual stop: Tap "Stop" button
- Auto-stop: After 30 seconds (safety timeout)
- Does NOT auto-stop on short pauses (user-controlled)

---

#### 2. AI-Powered Parsing

**Technology Used:** OpenAI API (GPT models)

**Supported Models:**
- `gpt-3.5-turbo` (default, fastest)
- `gpt-3.5-turbo-0125` (newer, 50% cheaper)
- `gpt-4-turbo` (higher accuracy)
- `gpt-4o` (latest multimodal)
- `gpt-4o-mini` (smallest, fastest)

**Model Selection:**
- Configurable in Settings → Appearance & Localization → AI Model Selection
- Default: `gpt-4o-mini` (best cost/performance ratio)
- Can be changed at runtime

**Parsing Process:**
1. Transcript sent to OpenAI API
2. AI extracts structured data:
   - Cost (€)
   - Liters (L)
   - Distance (km)
3. Returns JSON with confidence score
4. Falls back to regex parsing if AI fails

**Example Input/Output:**

Input: `"35 ευρώ, 25 λίτρα, 384 χιλιόμετρα"`

Output:
```json
{
  "cost": 35.0,
  "liters": 25.0,
  "distance": 384.0,
  "confidence": 0.95
}
```

**Special Rules:**
- Simple numeric sequences (e.g., "50 20 100") automatically map to:
  - 1st number → Cost
  - 2nd number → Liters
  - 3rd number → Distance

---

#### 3. Fallback Parsing

If AI parsing fails (network error, API error, low confidence):

**Regex-based Parser:**
- Extracts numbers with units (€, ευρώ, λίτρα, L, χιλιόμετρα, km)
- Language-independent pattern matching
- Less accurate but always available offline

**Implementation:**
- **File:** `domain/usecase/voice/ParseVoiceRefillUseCase.kt`
- **Method:** `parseWithRegex()`

---

### Configuration & Setup

#### OpenAI API Key

**Storage:** `SettingsPreferences` (DataStore)

**Setting the API Key:**

1. **Via Settings UI:**
   - Go to Settings → Appearance & Localization
   - Tap "AI Model Selection"
   - Enter API key in the text field
   - Key is saved immediately

2. **Programmatically:**
```kotlin
settingsPreferences.setVoiceParsingApiKey("sk-proj-...")
```

**Security:**
- API key stored in encrypted DataStore
- Never logged or exposed
- Only used for OpenAI API calls

**Getting an API Key:**
1. Visit [OpenAI Platform](https://platform.openai.com/api-keys)
2. Create account or sign in
3. Generate new API key
4. Copy and paste into app settings

---

### API Usage & Costs

#### Token-Based Pricing

OpenAI charges per token (roughly 4 characters = 1 token).

**Average Cost per Voice Entry:**

| Model | Input Tokens | Output Tokens | Cost per Entry | Notes |
|-------|--------------|---------------|----------------|-------|
| gpt-3.5-turbo | ~150 | ~50 | $0.000175 | Fast, cheap |
| gpt-3.5-turbo-0125 | ~150 | ~50 | $0.000088 | 50% cheaper |
| gpt-4-turbo | ~150 | ~50 | $0.001500 | Higher accuracy |
| gpt-4o | ~150 | ~50 | $0.001250 | Latest model |
| gpt-4o-mini | ~150 | ~50 | $0.000030 | **Recommended** |

**Estimated Usage:**

With **gpt-4o-mini** (default):
- 100 voice entries ≈ $0.003 (~0.3 cents)
- 1,000 voice entries ≈ $0.03 (~3 cents)
- 10,000 voice entries ≈ $0.30 (~30 cents)

**OpenAI Free Tier:**
- New accounts: $5 free credit
- Valid for 3 months
- Approximately **166,000 voice entries** with gpt-4o-mini

**Monitoring Usage:**
- Check usage at [OpenAI Usage Dashboard](https://platform.openai.com/usage)
- Set spending limits in OpenAI account settings
- Monitor API calls in app logs (debug builds)

---

### Permissions Required

#### 1. Microphone Permission (`RECORD_AUDIO`)

**Status:** Required for voice feature

**Handling:**
- Requested during onboarding
- Requested when user taps microphone button (if not granted)
- Graceful degradation: Voice button hidden if denied
- Can be granted later in Android Settings

**Permission Flow:**
```kotlin
// Check permission
WidgetPermissionChecker.hasMicrophonePermission(context)

// Request permission (handled automatically)
rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) {
        // Start voice entry
    }
}
```

**UI Behavior:**
- Widget voice button only shows if permission granted
- Add Refill screen shows voice button with permission check
- Permission denial shows explanatory message

#### 2. Internet Permission (`INTERNET`)

**Status:** Required for AI parsing

**Behavior:**
- Automatically granted (declared in manifest)
- Falls back to regex parsing if no internet

---

### Troubleshooting

#### Common Issues

**1. Voice Button Not Showing**

**Cause:** Microphone permission not granted

**Solution:**
- Grant permission via Settings → Permissions → Microphone
- Or tap "Voice Entry" in Add Refill screen to trigger permission request

---

**2. "Recording Stopped Too Early"**

**Cause:** Old silence detection behavior (now fixed)

**Solution:**
- Current version uses manual stop button
- Recording continues until user taps "Stop" or 30-second timeout
- Short pauses do NOT stop recording

---

**3. "Parsing Failed" Error**

**Possible Causes:**
- No internet connection
- OpenAI API key not set
- API key invalid/expired
- API rate limit exceeded
- Insufficient API credits

**Solutions:**
- Check internet connection
- Verify API key in Settings
- Check OpenAI account balance
- Wait a few minutes if rate limited
- Use regex fallback (automatic)

---

**4. Incorrect Parsing Results**

**Cause:** Unclear speech, background noise, or ambiguous input

**Solutions:**
- Speak clearly and distinctly
- Use numbers instead of words ("35" not "thirty-five")
- Include units when speaking ("ευρώ", "λίτρα", "χιλιόμετρα")
- Review and edit in confirmation dialog before saving
- Switch to higher accuracy model (gpt-4-turbo) in Settings

---

**5. "Odometer Field Not Filled"**

**Cause:** Greek language parsing issue (now fixed)

**Solution:**
- Current version correctly handles "χιλιόμετρα"
- Use "distance" or "km" as alternative
- Use simple sequence: "50 20 100" (cost, liters, distance)

---

### Maintenance Tasks

#### Regular Maintenance

**1. Monitor API Usage** (Monthly)
- Check OpenAI usage dashboard
- Verify costs are within budget
- Renew credits if needed

**2. Update API Key** (As Needed)
- Rotate keys for security
- Update in Settings → AI Model Selection
- Test voice entry after update

**3. Test Voice Feature** (After App Updates)
- Test Greek and English input
- Verify parsing accuracy
- Check permission handling
- Test fallback parsing

**4. Review Model Performance** (Quarterly)
- Compare accuracy across models
- Evaluate cost vs. accuracy trade-offs
- Switch models if needed

---

#### Code Maintenance

**Key Files to Monitor:**

1. **`SpeechRecognitionService.kt`**
   - Android Speech Recognition lifecycle
   - Permission handling
   - Recording control

2. **`ParseVoiceRefillUseCase.kt`**
   - OpenAI API integration
   - Regex fallback logic
   - JSON parsing

3. **`AddRefillViewModel_fixed.kt`**
   - Voice state management
   - UI integration
   - Error handling

4. **`SettingsPreferences.kt`**
   - API key storage
   - Model selection
   - User preferences

**Testing:**

```kotlin
// Unit test for parsing
@Test
fun `parseVoiceRefill should extract correct values`() = runTest {
    val result = parseVoiceRefillUseCase("35 ευρώ 25 λίτρα 100 χιλιόμετρα")
    
    assertTrue(result.isSuccess)
    assertEquals(35.0, result.getOrNull()?.cost)
    assertEquals(25.0, result.getOrNull()?.liters)
    assertEquals(100.0, result.getOrNull()?.distance)
}
```

**Logging:**

Debug builds include detailed logging:
- `VoiceParser` tag: Parsing attempts and results
- `SpeechRecognition` tag: Recording lifecycle
- `QuickEntryViewModel` tag: Voice state changes

---

### Advanced Configuration

#### Customizing AI Prompts

**Location:** `ParseVoiceRefillUseCase.kt` → `parseLLM()`

**Current Prompt:**
```kotlin
"""
You are parsing spoken car refueling data in Greek or English.
Extract: cost (€), liters (L), distance (km).
Examples:
- "35 ευρώ 25 λίτρα 100 χιλιόμετρα" → cost:35, liters:25, distance:100
- "50€ 40L 200km" → cost:50, liters:40, distance:200
...
"""
```

**Customization:**
- Add more examples for better accuracy
- Specify additional formats
- Add language-specific hints
- Include error correction instructions

---

#### Adding New Models

**Steps:**

1. Define model in `LLMModel` enum:
```kotlin
// SettingsPreferences.kt
enum class LLMModel(val modelId: String, val displayName: String, val costPer1M: String) {
    GPT_4_TURBO_PREVIEW("gpt-4-turbo-preview", "GPT-4 Turbo Preview", "~$0.01500"),
    // Add new model here
}
```

2. Update UI in `AIModelSelectionDialog.kt`

3. Test with voice entries

---

### Security Considerations

**API Key Protection:**
- ✅ Stored in encrypted DataStore
- ✅ Never logged in release builds
- ✅ Transmitted only over HTTPS
- ✅ Not included in crash reports

**Best Practices:**
- Don't share API keys
- Rotate keys periodically
- Set spending limits on OpenAI account
- Monitor usage for suspicious activity
- Revoke compromised keys immediately

---

### Performance Optimization

**Current Optimizations:**
- Async API calls (non-blocking)
- Timeout handling (30-second max)
- Resource cleanup (releases microphone)
- Efficient JSON parsing
- Minimal UI updates

**Future Improvements:**
- Cache common phrases
- Batch processing for multiple entries
- Local LLM integration (offline mode)
- Improve regex fallback accuracy

---

### Debugging Guide

**Enable Debug Logging:**

```kotlin
// In debug builds, detailed logs are automatically enabled
adb logcat | grep -E "VoiceParser|SpeechRecognition|QuickEntryViewModel"
```

**Test API Key:**

```kotlin
// Check if API key is set
val apiKey = settingsPreferences.getVoiceParsingApiKey().first()
Log.d("VoiceDebug", "API Key present: ${!apiKey.isNullOrBlank()}")
```

**Simulate Voice Input:**

```kotlin
// Manually trigger parsing with test transcript
viewModel.parseVoiceTranscript("35 ευρώ 25 λίτρα 100 χιλιόμετρα")
```

---

### Documentation References

**Internal Documentation:**
- `REFILL_EXPENSE_VALIDATION.md` - Validation architecture
- `EDGE_CASES_SUMMARY.md` - Edge case handling
- `VOICE_RECOGNITION_GUIDE.md` - Detailed voice feature guide (if exists)

**External Documentation:**
- [OpenAI API Reference](https://platform.openai.com/docs/api-reference)
- [Android Speech Recognition](https://developer.android.com/reference/android/speech/SpeechRecognizer)
- [Material 3 Components](https://m3.material.io/)

---


Potential features for future development:
- Cloud sync and backup
- Multiple user support
- Advanced analytics and predictions
- Fuel efficiency comparisons
- Integration with car APIs
- Receipt photo attachment
- Expense sharing and reports
- Wear OS companion app
- Machine learning for expense categorization
- Fuel price tracking and alerts
- Voice input for expenses (expand voice feature)
- Multi-language voice support expansion

---

**Built with ❤️ using Kotlin and Jetpack Compose**

*Last updated: February 2026*


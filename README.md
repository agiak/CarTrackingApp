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
- **Flexible Sorting**: Sort by date, amount, or type
- **Split View Layout**: Optimized for tablets and landscape orientation
- **Quick Actions**: Direct access to transaction details and editing
- **Summary Statistics**: Overview of total transactions and spending

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

- **Monthly Details View**:
  - Detailed breakdown of specific months
  - Refills and expenses grouped by date
  - Monthly totals and averages
  - **Interactive Tooltips**: Info tooltips explaining metrics like "Fuel as % of spending"

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
  - **Configurable Intensity**: Adjustable background color intensity
- **Responsive Design**:
  - **Adaptive Layouts**: Different layouts for phones vs tablets
  - **Smart Split Views**: Automatic split-screen layouts on tablets and landscape phones
  - **Optimized TopBar**: Reduced padding on mobile devices in landscape mode
- **Bottom Navigation**: 
  - Home (Cars List)
  - Transactions (All fuel refills and expenses)
  - Statistics
  - Settings
- **Advanced UI Features**:
  - Floating Action Buttons for quick actions
  - Swipe gestures for banner dismissal
  - Smooth animations and transitions
  - Interactive tooltips and info popups
  - Smart grid layouts (1-3 columns based on device)
- **Localization**: Greek and English support

### ⚙️ Settings & Customization
- **Appearance & Localization**:
  - **Theme Selection**: Light, Dark, or System theme
  - **Color Palette**: Choose from 30+ color themes
  - **Language Selection**: Greek and English support

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
- **Device Detection**: Smart tablet vs phone detection using smallest width
- **Location Services**: GPS integration for refill locations
- **File Handling**: Apache POI for Excel/CSV processing
- **Serialization**: Kotlinx Serialization for JSON export/import
- **Theming System**: Advanced color palette management with dynamic backgrounds
- **Responsive Design**: Automatic layout adaptation for different screen sizes

### Key Components
- **Domain Layer**: Use cases, models, repositories
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
- **Utilities**:
  - DeviceUtils for proper tablet/landscape detection
  - Location provider for GPS tracking
  - Notification manager for reminders
  - Data validation (RefillValidator, ExpenseValidator)
  - Import/export managers (JSON, Excel/CSV)

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
  - Consumption must be within reasonable range (0.5 - 50 L/100km)
  - Odometer values validated
  
- **Expenses**:
  - Amount must be positive
  - Category cannot be empty
  - Category name length limits
  
- **Cars**:
  - Name and license plate required
  - Odometer must be numeric
  - Duplicate license plate detection

### Error Handling
- Graceful failure without crashes
- User-friendly error messages
- Localized error descriptions
- Recovery options provided
- Transaction safety for database operations

### Permission Management
- Runtime permission requests
- Handling permanently denied permissions
- Settings link for manual permission grant
- GPS unavailable fallback (non-blocking)
- Notification permission state tracking

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

## 🚀 Future Enhancements

Potential features for future development:
- Cloud sync and backup
- Multiple user support
- Advanced analytics and predictions
- Fuel efficiency comparisons
- Integration with car APIs
- Receipt photo attachment
- Expense sharing and reports
- Widget support
- Wear OS companion app
- Voice input for quick data entry
- Machine learning for expense categorization
- Fuel price tracking and alerts

---

**Built with ❤️ using Kotlin and Jetpack Compose**

*Last updated: February 2026*


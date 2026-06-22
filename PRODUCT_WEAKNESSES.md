# CarTrackingApp — Product Weaknesses & Improvement Roadmap

> Ανάλυση από product perspective βασισμένη στον κώδικα και τη λειτουργικότητα της έκδοσης v1.1.0.

---

## 1. Χωρίς Cloud Sync — Σοβαρό Liability

**Αδυναμία:** Το app είναι 100% offline-first χωρίς καμία επιλογή cloud backup. Αν ο χρήστης χάσει το κινητό, αλλάξει συσκευή ή κάνει factory reset, χάνει **όλα** τα δεδομένα. Το export/import με JSON είναι manual process που η πλειοψηφία των χρηστών δεν θα κάνει.

**Impact:** Υψηλό. Είναι το νούμερο ένα λόγος που χρήστες εγκαταλείπουν expense tracking apps.

**Βελτίωση:**
- Αυτόματο backup στο **Google Drive** μέσω Drive API (χωρίς server κόστος)
- Silent background sync κάθε φορά που ο χρήστης ανοίγει το app
- Restore wizard κατά την πρώτη εγκατάσταση ("Εντοπίστηκε αντίγραφο ασφαλείας από [ημερομηνία], θέλετε να επαναφέρετε;")

---

## 2. OpenAI API Key — Τεράστιο UX Friction

**Αδυναμία:** Τα AI features (voice parsing, car comparison insights, fuel forecasting με tips) απαιτούν ο χρήστης να έχει δικό του OpenAI API key, να κατανοεί τι είναι το OpenAI, να φτιάξει account, να βρει και να αντιγράψει το key στις ρυθμίσεις. Αυτό αποκλείει >90% των mainstream χρηστών.

**Impact:** Υψηλό. Τα AI features είναι από τα πιο differentiating του app — αλλά ουσιαστικά δεν υπάρχουν για τον μέσο χρήστη.

**Βελτίωση:**
- **Βραχυπρόθεσμα:** Ενσωμάτωση **on-device speech-to-text** (Android SpeechRecognizer είναι ήδη εκεί) + regex parsing χωρίς GPT για βασική voice entry
- **Μεσοπρόθεσμα:** Backend proxy με ελεγχόμενο rate limiting (π.χ. 10 AI calls/μήνα δωρεάν per user, χρέωση για παραπάνω) — ο χρήστης δεν χρειάζεται API key
- **Μακροπρόθεσμα:** Αντικατάσταση AI insights με deterministic rules που δεν χρειάζονται LLM (η anomaly detection ήδη το κάνει αυτό)

---

## 3. Android Μόνο — Περιορισμένη Αγορά

**Αδυναμία:** Δεν υπάρχει iOS έκδοση, web app ή tablet-first UI. Ένας χρήστης που αλλάζει από Android σε iPhone χάνει το app εντελώς.

**Impact:** Μέτριο-Υψηλό. ~55% της ελληνικής αγοράς smartphone είναι iOS.

**Βελτίωση:**
- Μεταφορά σε **Kotlin Multiplatform Mobile (KMM)** — η business logic (use cases, domain models) είναι ήδη καθαρή και portable. Θα χρειαστεί νέο SwiftUI UI layer μόνο.
- Εναλλακτικά: **Flutter** rewrite αν στοχεύεται ταυτόχρονη κυκλοφορία iOS/Android
- **Web dashboard** (read-only) για desktop χρήση — π.χ. εξαγωγή reports, σύγκριση ετών

---

## 4. Trips: Υποανάπτυκτο Feature

**Αδυναμία:** Τα trips είναι απλώς manual groupings ανταλλαγών. Δεν υπάρχει GPS tracking, αυτόματη ανίχνευση αναχώρησης/άφιξης, ή στατιστικά ανά διαδρομή (fuel cost per trip, average speed κ.λπ.). Ο χρήστης πρέπει manually να προσθέτει refills σε ένα trip — πολύ friction.

**Impact:** Μέτριο. Ένα "trips" feature που δεν κάνει tracking δεν έχει σαφές value proposition.

**Βελτίωση:**
- Αυτόματη δημιουργία trip όταν ο χρήστης καταγράφει refill με GPS location
- Προσθήκη trip summary: συνολική απόσταση, κόστος, μέση κατανάλωση
- Ή: Απλοποίηση — μετονομασία σε "Labels/Tags" αντί για trips αν δεν θα γίνει full GPS tracking

---

## 5. Location: Half-Baked Implementation

**Αδυναμία:** Υπάρχει GPS tagging σε refills αλλά το reverse geocoding (εμφάνιση ονόματος τοποθεσίας) αναφέρεται στο README ως "future feature". Άρα ο χρήστης βλέπει coordinates αντί για "Shell, Κηφισίας 45". Επίσης δεν υπάρχει "πού γεμίζω φθηνότερα" ανάλυση βάσει τοποθεσίας.

**Impact:** Μέτριο. Αν η location δεν φαίνεται ως human-readable κείμενο, είναι άχρηστη για τον χρήστη.

**Βελτίωση:**
- Υλοποίηση reverse geocoding με **Nominatim (OpenStreetMap)** — δωρεάν, χωρίς API key
- Εμφάνιση station name στο refill history ("Γέμισμα @ BP Λεωφ. Αθηνών")
- "Φθηνότερα πρατήρια" map view βάσει ιστορικού τιμών

---

## 6. Onboarding: Πολύ Αδύναμο για Νέους Χρήστες

**Αδυναμία:** Το onboarding είναι 5 static slides. Δεν υπάρχει interactive demo, sample data, ή guided first-action flow. Ένας νέος χρήστης βλέπει κενή λίστα αυτοκινήτων και δεν ξέρει από πού να αρχίσει. Επιπλέον, τα AI features δεν παράγουν αποτελέσματα χωρίς 3+ μήνες δεδομένα — ο νέος χρήστης δεν βλέπει αξία νωρίς.

**Impact:** Υψηλό. Η πλειοψηφία των apps χάνει >60% των χρηστών στις πρώτες 3 ημέρες λόγω κακού onboarding.

**Βελτίωση:**
- **Sample data mode**: "Θέλετε να φορτωθούν δείγματα δεδομένων για να δείτε πώς λειτουργεί το app;"
- **First-action guide**: Μετά την προσθήκη αυτοκινήτου, prompt "Προσθέστε το πρώτο σας γέμισμα →"
- **Empty state illustrations** σε κάθε screen με CTA button
- Tooltip overlays στα πρώτα 3 screens (coach marks)

---

## 7. Δεν Υπάρχει Multi-Currency / Διεθνής Υποστήριξη

**Αδυναμία:** Το app φαίνεται να υποθέτει Euro (€) ως νόμισμα. Δεν υπάρχει ρύθμιση νομίσματος. Αυτό αποκλείει χρήστες εκτός Ευρωζώνης και χρήστες που γεμίζουν σε ταξίδια σε διαφορετικές χώρες.

**Impact:** Χαμηλό-Μέτριο για την ελληνική αγορά, Υψηλό αν στοχεύεται διεθνής κυκλοφορία.

**Βελτίωση:**
- Ρύθμιση νομίσματος στις Settings (από λίστα ή free-text σύμβολο)
- Multi-currency per refill (χρήσιμο για οδηγούς που ταξιδεύουν)
- Αυτόματη ανίχνευση από locale του συστήματος

---

## 8. Στατιστικά: Εστίαση σε Κατανάλωση, Όχι σε Πλήρες Cost of Ownership

**Αδυναμία:** Το app εξαιρετικά καλά τεκμηριώνει κατανάλωση καυσίμου αλλά δεν δίνει **συνολική εικόνα κόστους κατοχής** (Total Cost of Ownership). Λείπουν: απόσβεση οχήματος, ασφάλιση ανά km, ΚΤΕΟ κόστος, φορολογία.

**Impact:** Μέτριο. Οι "power users" που θέλουν πλήρη financial tracking θα χρησιμοποιούν παράλληλα spreadsheet.

**Βελτίωση:**
- **TCO Dashboard**: "Πόσο σας κοστίζει το αυτοκίνητο ανά km / ανά μήνα" (καύσιμα + service + ασφάλεια + ΚΤΕΟ)
- Προσθήκη "fixed annual costs" στο car profile (ασφάλεια, τέλη κυκλοφορίας)
- "Break-even analysis" αν ο χρήστης σκέφτεται να αλλάξει αυτοκίνητο

---

## 9. Reminders: Περιορισμένη Ευφυΐα

**Αδυναμία:** Τα reminders είναι binary — date-based ή mileage-based. Δεν υπάρχουν smart reminders βάσει pattern recognition, π.χ. "το λάδι αλλάζεται συνήθως κάθε 6 μήνες στο ιστορικό σου — να σου βάλω reminder;". Επίσης δεν υπάρχει integration με Google/Apple Calendar.

**Impact:** Μέτριο.

**Βελτίωση:**
- Auto-suggest reminders βάσει ιστορικού service ("Η τελευταία αλλαγή λαδιού ήταν πριν 5,800km — η συνιστώμενη είναι κάθε 5,000km")
- Export reminder σε Google Calendar / iCal
- Snooze functionality (αναβολή reminder κατά X ημέρες)

---

## 10. Attachment Management: Μόνο Local Storage

**Αδυναμία:** Τα attachments (PDF τιμολόγια, φωτογραφίες) αποθηκεύονται μόνο τοπικά. Δεν υπάρχει OCR για αυτόματη εξαγωγή δεδομένων από αποδείξεις (ποσό, ημερομηνία, τύπος καυσίμου). Όριο 10MB ανά αρχείο.

**Impact:** Μέτριο. Οι χρήστες που θέλουν να κρατούν τιμολόγια θα πρέπει να καταχωρούν τα δεδομένα manually παράλληλα.

**Βελτίωση:**
- **Receipt OCR**: Με Google ML Kit (on-device, δωρεάν) αυτόματη ανάγνωση αποδείξεων
- Pre-fill φόρμας από OCR (ποσό, ημερομηνία, τύπος καυσίμου)
- Cloud storage για attachments (ενσωμάτωση με το cloud backup του #1)

---

## 11. Δεν Υπάρχει Σύγκριση με Αγορά / Benchmarking

**Αδυναμία:** Ο χρήστης βλέπει ότι καταναλώνει 7.5 L/100km αλλά δεν ξέρει αν αυτό είναι καλό ή κακό για το μοντέλο του. Δεν υπάρχει benchmark σύγκριση με τον ίδιο τύπο αυτοκινήτου ή με τον μέσο χρήστη.

**Impact:** Χαμηλό-Μέτριο. Προσθέτει context στα στατιστικά.

**Βελτίωση:**
- **"Πώς τα πάω"** section: Σύγκριση με factory specs (ο χρήστης εισάγει το επίσημο L/100km του μοντέλου)
- Ανώνυμο community benchmarking (opt-in): "Άλλοι με Toyota Yaris: μέση κατανάλωση 6.8 L/100km"
- Επίσης: tracking απόκλισης από factory specs με el tiempo

---

## 12. Testing Coverage: Ανεπαρκές για Production

**Αδυναμία:** Υπάρχει μόνο βασικό JUnit + Espresso setup. Δεν αναφέρονται unit tests για τα use cases, integration tests για τη βάση δεδομένων, ή UI tests για τα screens. Το forecasting και anomaly detection algorithms δεν έχουν verified test coverage.

**Impact:** Υψηλό για maintainability. Κάθε νέο feature risk regression.

**Βελτίωση:**
- Unit tests για κάθε use case (51 ήδη υπάρχουν — χρειάζονται tests)
- `HoltLinearSmoothingEngine` με test suite για edge cases (λίγα δεδομένα, outliers)
- Instrumented tests για Room DAOs
- GitHub Actions CI pipeline με test reports

---

## 13. Κανένα Feedback / Analytics Loop

**Αδυναμία:** Ο developer δεν έχει ορατότητα στο πώς χρησιμοποιείται το app — ποια features χρησιμοποιούνται, πού σκοντάφτουν οι χρήστες, ποια screens εγκαταλείπονται. Δεν υπάρχει crash reporting (Firebase Crashlytics ή παρόμοιο).

**Impact:** Υψηλό για product decisions. Χωρίς data, οι αποφάσεις είναι guesswork.

**Βελτίωση:**
- **Firebase Crashlytics**: Δωρεάν crash reporting για production builds
- **Privacy-respecting analytics**: Firebase Analytics ή Plausible (opt-in, anonymized)
- In-app feedback button (κουμπί "Αποστολή σχολίου" στο About screen)
- Ερώτηση NPS μετά από 7 ημέρες χρήσης: "Πόσο πιθανό είναι να το προτείνετε;"

---

## 14. Fuel Type: Δεν Επιβάλλεται στο Car Profile

**Αδυναμία:** Στο car profile δεν υπάρχει υποχρεωτικό πεδίο για τύπο καυσίμου (βενζίνη, diesel, LPG, ηλεκτρικό, υβριδικό). Αυτό σημαίνει ότι δεν μπορεί να γίνει validation κατά την καταχώρηση refill, δεν μπορούν να εμφανιστούν διαφορετικά benchmarks, και τα statistics δεν έχουν context.

**Impact:** Μέτριο.

**Βελτίωση:**
- Προσθήκη `fuelType` enum στο CarEntity (Petrol, Diesel, LPG, Electric, Hybrid)
- Για Electric: αντικατάσταση "Λίτρα" με "kWh" και "L/100km" με "kWh/100km"
- Για Hybrid: tracking και των δύο (καύσιμο + ηλεκτρισμός)
- Validation: αν car είναι Electric, να μην εμφανίζεται refill form αλλά charge log form

---

## 15. Widgets: Μόνο Quick-Add, Χωρίς Stats

**Αδυναμία:** Τα 3 widgets είναι αποκλειστικά για γρήγορη καταχώρηση. Δεν υπάρχει info widget που να εμφανίζει πχ "Τρέχουσα κατανάλωση: 7.2 L/100km" ή "Επόμενο service: 450km" στην home screen χωρίς να ανοίξει το app.

**Impact:** Χαμηλό-Μέτριο.

**Βελτίωση:**
- **Stats widget (3x2)**: Εμφάνιση τρέχουσας κατανάλωσης, κόστους τρέχοντος μήνα, επόμενου reminder
- **Reminder countdown widget (2x1)**: "Service σε 320km"
- Configurable widget content (ο χρήστης επιλέγει ποια metric θέλει)

---

## Σύνοψη Προτεραιοτήτων

| # | Αδυναμία | Impact | Δυσκολία | Προτεραιότητα |
|---|----------|--------|----------|---------------|
| 1 | Χωρίς Cloud Sync | Υψηλό | Μέτρια | **P0** |
| 2 | OpenAI API Key friction | Υψηλό | Υψηλή | **P0** |
| 6 | Αδύναμο Onboarding | Υψηλό | Χαμηλή | **P0** |
| 13 | Κανένα Crash Reporting | Υψηλό | Χαμηλή | **P1** |
| 12 | Ανεπαρκές Testing | Υψηλό | Μέτρια | **P1** |
| 3 | Android-only | Υψηλό | Πολύ Υψηλή | **P2** |
| 5 | Location χωρίς reverse geocoding | Μέτριο | Χαμηλή | **P1** |
| 4 | Trips: Half-baked | Μέτριο | Μέτρια | **P2** |
| 8 | Χωρίς TCO stats | Μέτριο | Μέτρια | **P2** |
| 14 | Fuel type δεν επιβάλλεται | Μέτριο | Χαμηλή | **P1** |
| 10 | Attachment OCR | Μέτριο | Υψηλή | **P3** |
| 9 | Reminders: Basic | Μέτριο | Μέτρια | **P2** |
| 7 | Mono-currency | Χαμηλό | Χαμηλή | **P2** |
| 15 | Widgets Stats | Χαμηλό | Χαμηλή | **P3** |
| 11 | Benchmarking | Χαμηλό | Υψηλή | **P3** |

---

*Δημιουργήθηκε: Ιούνιος 2026 — βάσει κώδικα v1.1.0 (Build 17)*

# Αδυναμίες Product & Προτάσεις Βελτίωσης — CarTrackingApp

> Έκδοση ανάλυσης: 1.1.0 (Build 17) | Ημερομηνία: 2026-06-23

---

## 1. Απουσία Cloud Sync & Multi-Device Υποστήριξης

**Πρόβλημα:**  
Τα δεδομένα αποθηκεύονται αποκλειστικά τοπικά στη συσκευή. Αν ο χρήστης αλλάξει τηλέφωνο ή χάσει τη συσκευή του, χάνει όλα τα δεδομένα του. Δεν υπάρχει τρόπος να συγχρονιστεί η εφαρμογή μεταξύ δύο συσκευών (π.χ. κινητό + tablet).

**Επίπτωση:** Υψηλός κίνδυνος απώλειας δεδομένων. Εμπόδιο σε σενάρια οικογένειας ή πολλαπλών χρηστών που μοιράζονται αυτοκίνητα.

**Πρόταση βελτίωσης:**
- Ενσωμάτωση Google Drive Backup μέσω του Drive API (το app έχει ήδη Google στο stack).
- Αυτόματο sync κάθε εβδομάδα + manual sync button στις Ρυθμίσεις.
- Ως ενδιάμεσο βήμα: βελτίωση του ήδη υπάρχοντος auto-backup (Excel στο Downloads) ώστε να εξάγει αυτόματα και στο Google Drive.
- Μακροπρόθεσμα: Firebase Firestore για real-time sync με conflict resolution.

---

## 2. Μηδενική Test Coverage

**Πρόβλημα:**  
Από τα 374 αρχεία Kotlin υπάρχουν μόνο 2 placeholder tests χωρίς ουσιαστικό περιεχόμενο. Η πολύπλοκη business logic (forecasting με Holt's Smoothing, anomaly detection, validation rules, 19 database migrations) δεν έχει κανένα test coverage.

**Επίπτωση:** Κάθε νέο feature ή refactor φέρει υψηλό κίνδυνο regression. Δεν μπορεί να γίνει αξιόπιστη ανάπτυξη σε ταχύτητα.

**Πρόταση βελτίωσης:**
- **Άμεσα (unit tests):** RefillValidator, CarValidator, ExpenseValidator, FuelForecastEngine, AnomalyDetector — αυτά είναι pure functions και μπαίνουν εύκολα.
- **Βραχυπρόθεσμα (integration tests):** Repository tests με in-memory Room database.
- **Μακροπρόθεσμα:** ViewModel tests με Turbine για Flow testing, UI snapshot tests με Paparazzi.
- Στόχος: 60%+ coverage σε domain + data layers εντός 3 μηνών.

---

## 3. Εξάρτηση από OpenAI για Voice Entry

**Πρόβλημα:**  
Το voice entry για ανεφοδιασμό απαιτεί manual εισαγωγή OpenAI API key από τον χρήστη. Αυτό είναι τεχνικό εμπόδιο για το μέσο χρήστη. Το fallback (regex parsing) είναι πολύ βασικό και δεν ενημερώνει σαφώς τον χρήστη για τη μειωμένη ποιότητα.

**Επίπτωση:** Το feature είναι ουσιαστικά απενεργοποιημένο για τους περισσότερους χρήστες. Η εμπειρία υποβαθμίζεται σιωπηλά.

**Πρόταση βελτίωσης:**
- Ενσωμάτωση on-device ML model (π.χ. Google ML Kit ή Gemini Nano) για parsing χωρίς API key.
- Επέκταση voice entry και για expenses (τώρα λείπει τελείως).
- Σαφές UI feedback όταν γίνεται χρήση του regex fallback ("Αναγνώριση offline — μειωμένη ακρίβεια").
- Προσθήκη retry logic και timeout handling για τα API calls.

---

## 4. Δεν Υπάρχει Crash Reporting & Analytics

**Πρόβλημα:**  
Δεν υπάρχει ενσωμάτωση Firebase Crashlytics, Sentry ή άλλου crash reporting tool. Αν ένας χρήστης αντιμετωπίσει crash, δεν υπάρχει τρόπος να το γνωρίζει ο developer. Επίσης, δεν γνωρίζουμε ποια features χρησιμοποιούνται πραγματικά.

**Επίπτωση:** Αδύνατη η τεκμηριωμένη λήψη αποφάσεων για product priorities. Bugs παραμένουν undetected.

**Πρόταση βελτίωσης:**
- Ενσωμάτωση **Firebase Crashlytics** για crash reporting (δωρεάν, ελάχιστο setup).
- Προαιρετική ενσωμάτωση **Firebase Analytics** με opt-in consent screen για GDPR compliance.
- Tracking βασικών events: feature usage, voice entry success rate, export frequency.
- Εμφάνιση σχετικής ερώτησης κατά το onboarding ("Επιτρέπεις anonymous crash reports;").

---

## 5. Αναζήτηση & Φιλτράρισμα Δεδομένων

**Πρόβλημα:**  
Η οθόνη Transactions έχει βασικό φιλτράρισμα (ανά τύπο, αυτοκίνητο, κατηγορία) αλλά δεν υπάρχει:
- Search bar για αναζήτηση βάσει κειμένου/σημειώσεων
- Custom date range picker
- Αποθηκευμένα φίλτρα (saved filters)
- Ταξινόμηση κατά κόστος, ημερομηνία, κατηγορία

**Επίπτωση:** Για χρήστες με πολλές καταχωρήσεις (100+ refills, 50+ expenses), η εύρεση συγκεκριμένης εγγραφής γίνεται χρονοβόρα.

**Πρόταση βελτίωσης:**
- Προσθήκη search bar στην οθόνη Transactions με real-time filtering μέσω Room SQL LIKE queries.
- Date range picker με preset επιλογές (Αυτός ο μήνας, Τελευταίοι 3 μήνες, Αυτό το χρόνο).
- Δυνατότητα αποθήκευσης έως 3 custom φίλτρων.
- Multi-sort (πρώτα κατά ημερομηνία, μετά κατά κόστος).

---

## 6. Μη Ανακαλύψιμο "Recently Deleted" (Trash)

**Πρόβλημα:**  
Υπάρχει σύστημα soft delete με "Recently Deleted" αλλά δεν είναι εμφανές στο navigation. Ο χρήστης που διαγράφει κατά λάθος μια εγγραφή πιθανότατα δεν γνωρίζει ότι μπορεί να την ανακτήσει.

**Επίπτωση:** Feature υπάρχει αλλά δεν αξιοποιείται. Χρήστες χάνουν δεδομένα χωρίς λόγο.

**Πρόταση βελτίωσης:**
- Εμφάνιση **undo snackbar** αμέσως μετά τη διαγραφή ("Διαγράφηκε — Αναίρεση" για 5 δευτερόλεπτα).
- Προσθήκη "Πρόσφατα Διαγραμμένα" ως επιλογή στο Settings → Data & Storage.
- Notification reminder ("Έχεις 3 στοιχεία στον κάδο που θα διαγραφούν μόνιμα σε 30 μέρες").

---

## 7. Επαναλαμβανόμενα Έξοδα & Templates

**Πρόβλημα:**  
Δεν υπάρχει δυνατότητα επαναλαμβανόμενων εξόδων (recurring expenses). Ο χρήστης που πληρώνει ασφάλιση κάθε χρόνο ή service κάθε 6 μήνες πρέπει να ξαναμπεί τα στοιχεία κάθε φορά.

**Επίπτωση:** Τριβή για συνηθισμένες, προβλέψιμες δαπάνες. Χαμένη ευκαιρία για proactive reminders.

**Πρόταση βελτίωσης:**
- "Αντιγραφή εξόδου" — ένα tap για να δημιουργηθεί νέα έξοδος με τα ίδια στοιχεία, μόνο με νέα ημερομηνία.
- Recurring expense option: "Επανάληψη κάθε X μήνες" με αυτόματη δημιουργία εγγραφής + reminder.
- Expense templates: αποθήκευση κοινών εξόδων (π.χ. "ΚΤΕΟ — 80€") για γρήγορη χρήση.

---

## 8. Widgets: Περιορισμένη Λειτουργικότητα

**Πρόβλημα:**  
Τα υπάρχοντα widgets (Quick Add, Refill, Expense) είναι αποκλειστικά για εισαγωγή δεδομένων. Δεν υπάρχει widget που να εμφανίζει πληροφορίες (στατιστικά, επόμενο reminder, τελευταία κατανάλωση).

**Επίπτωση:** Η αρχική οθόνη του χρήστη δεν του δίνει γρήγορη εικόνα της κατάστασης των οχημάτων του.

**Πρόταση βελτίωσης:**
- **Stats Widget (4x2):** Εμφάνιση τελευταίας κατανάλωσης, τρέχοντος μηνιαίου κόστους, επόμενου reminder.
- **Car Summary Widget (2x2):** Ανά αυτοκίνητο — τελευταίος ανεφοδιασμός, ημέρες μέχρι ΚΤΕΟ/ασφάλιση.
- Configurable widgets: ο χρήστης επιλέγει τι θέλει να βλέπει.

---

## 9. Έλλειψη Receipt Scanning / OCR

**Πρόβλημα:**  
Ο χρήστης μπορεί να επισυνάψει αρχεία (PDF, εικόνες) στο αυτοκίνητο αλλά δεν μπορεί να σκανάρει μια απόδειξη και να εξαγάγει αυτόματα στοιχεία (κόστος, λίτρα, σταθμός).

**Επίπτωση:** Χαμένη ευκαιρία για αυτοματισμό της πιο συνηθισμένης ενέργειας (καταγραφή βενζίνης).

**Πρόταση βελτίωσης:**
- Ενσωμάτωση **Google ML Kit Text Recognition** για OCR από κάμερα.
- "Σκάναρε απόδειξη" button στην οθόνη Add Refill — εξαγωγή κόστους και λίτρων αυτόματα.
- Low-cost υλοποίηση: on-device, χωρίς εξωτερικό API.

---

## 10. Ασθενής Onboarding για Νέους Χρήστες

**Πρόβλημα:**  
Υπάρχει onboarding guide (welcome slides) αλλά παρουσιάζεται μόνο μία φορά. Δεν υπάρχουν contextual tooltips, empty states με guidance, ή progressive disclosure για advanced features (forecasting, anomaly detection, trips).

**Επίπτωση:** Ο νέος χρήστης μπορεί να αγνοεί σημαντικές λειτουργίες (π.χ. Insights, FuelForecast) που προσθέτουν μεγάλη αξία.

**Πρόταση βελτίωσης:**
- **Empty states με action guidance:** Όταν δεν υπάρχουν δεδομένα, εμφάνιση "Πρόσθεσε τον πρώτο σου ανεφοδιασμό →" αντί για κενή οθόνη.
- **Feature discovery cards:** Στατιστικά και Insights να εμφανίζουν εξηγητικό card την πρώτη φορά.
- **Contextual tooltips:** Tooltip "Χρειάζεσαι δεδομένα 3+ μηνών για προβλέψεις" στο FuelForecast όταν τα δεδομένα είναι ανεπαρκή.
- Επαναλαμβανόμενο onboarding guide επιλογής στο Settings → Help.

---

## 11. Έλλειψη Export σε PDF

**Πρόβλημα:**  
Το export υποστηρίζει JSON και Excel. Δεν υπάρχει export σε PDF (λογιστική χρήση, ασφαλιστικές εταιρείες, εφορία).

**Επίπτωση:** Ο χρήστης που θέλει να υποβάλει έξοδα σε εργοδότη ή λογιστή δεν έχει "έτοιμο" έγγραφο.

**Πρόταση βελτίωσης:**
- PDF report generator με iText ή PdfDocument API.
- Report options: ανά αυτοκίνητο, ανά χρονική περίοδο, ανά κατηγορία εξόδου.
- Μορφή: summary page + detailed transaction list + graphs (εικόνες από charts).

---

## 12. Περιορισμένη Προσβασιμότητα (Accessibility)

**Πρόβλημα:**  
Δεν υπάρχει επίσημος accessibility audit. Δεν είναι γνωστό αν το app λειτουργεί σωστά με TalkBack, αν τα content descriptions είναι πλήρη, ή αν υπάρχουν επαρκείς contrast ratios σε όλα τα 30+ themes.

**Επίπτωση:** Αποκλεισμός χρηστών με αναπηρίες. Κίνδυνος μη συμμόρφωσης με accessibility guidelines.

**Πρόταση βελτίωσης:**
- Εκτέλεση **Accessibility Scanner** (Google tool) για αυτόματη ανίχνευση προβλημάτων.
- Προσθήκη `contentDescription` σε όλα τα εικονίδια και buttons χωρίς κείμενο.
- Έλεγχος contrast ratio για τα custom color themes (WCAG AA: 4.5:1).
- Manual TalkBack testing σε βασικές ροές (Add Refill, View Statistics).

---

## Σύνοψη Προτεραιοτήτων

| # | Αδυναμία | Επίπτωση | Δυσκολία | Προτεραιότητα |
|---|----------|----------|----------|---------------|
| 1 | Cloud Sync | Απώλεια δεδομένων | Υψηλή | 🔴 Κρίσιμη |
| 2 | Μηδενικά Tests | Regression risk | Μέτρια | 🔴 Κρίσιμη |
| 3 | Crash Reporting | Blind spots σε bugs | Χαμηλή | 🔴 Κρίσιμη |
| 4 | Voice/OpenAI εξάρτηση | Feature απροσπέλαστο | Υψηλή | 🟠 Υψηλή |
| 5 | Αναζήτηση & Φίλτρα | UX τριβή | Χαμηλή | 🟠 Υψηλή |
| 6 | Undo/Trash Discovery | Απώλεια δεδομένων | Χαμηλή | 🟠 Υψηλή |
| 7 | Επαναλαμβανόμενα Έξοδα | Χαμένος αυτοματισμός | Μέτρια | 🟡 Μέτρια |
| 8 | Widgets Info | Missed engagement | Μέτρια | 🟡 Μέτρια |
| 9 | Receipt OCR | Χαμένος αυτοματισμός | Μέτρια | 🟡 Μέτρια |
| 10 | Onboarding UX | Feature discovery | Χαμηλή | 🟡 Μέτρια |
| 11 | PDF Export | Λογιστική χρήση | Μέτρια | 🟢 Χαμηλή |
| 12 | Accessibility | Αποκλεισμός χρηστών | Μέτρια | 🟢 Χαμηλή |

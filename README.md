# Kor Constructions - Management System

A local web application for managing customers, receipts, payments, and transactions.

## 🚀 Quick Start

### First Time Setup:
1. **Install Java 17+**: https://adoptium.net/
2. **Run Application**: Double-click `run-jar.bat`
3. **Browser Opens**: Automatically opens to http://localhost:8080

### Files:
- `START-HERE.txt` - Quick start guide
- `run-jar.bat` - **Start application** (main file to use)
- `WINDOWS-SETUP.md` - Complete Windows installation guide
- `SHARED-DATA-GUIDE.md` - Share data between laptops

---

## 📂 Application Structure

```
KorConstructions/
├── target/
│   └── kor-constructions-app-1.0.0.jar  ← The application (19MB)
├── data/                                 ← YOUR DATA (backup this!)
│   ├── customers.json
│   ├── receipts.json
│   └── payments.json
├── run-jar.bat                          ← Double-click to start
├── setup-cloud-sync.bat                 ← Setup cloud storage sharing
├── backup-data.bat                      ← Backup your data
└── build-jar.bat                        ← Rebuild if you modify code
```

---

## 💻 Features

### Tab 1: Πελάτες (Customers)
- Manage customer information
- Store contact details, tax ID (ΑΦΜ)
- View customer receipts

### Tab 2: Απόδειξη Είσπραξης (Collection Receipts)
- Create receipt for money received
- Track customer payments
- **Print professional receipts**

### Tab 3: Απόδειξη Πληρωμής (Payment Receipts)
- Create receipts for money paid out
- Track expenses
- **Print professional receipts**

### Tab 4: Κινήσεις (Transactions)
- View all financial movements
- Filter receipts vs payments
- See complete transaction history

---

## 🌐 Sharing Between Two Laptops

### Method 1: Cloud Storage (Recommended) ⭐

**Setup (10 minutes):**
1. Install Dropbox/OneDrive on both laptops (same account)
2. Run `setup-cloud-sync.bat` on both laptops
3. Enter the same cloud folder path
4. Done! Data syncs automatically

**Files:**
- `setup-cloud-sync.bat` - One-time setup
- `SHARING-QUICK-START.txt` - Quick guide
- `SHARED-DATA-GUIDE.md` - Detailed instructions

### Method 2: Shared Network Folder

For office networks. See `SHARED-DATA-GUIDE.md`

### Method 3: USB Drive / Manual Sync

Copy `data` folder between laptops manually.

---

## 💾 Backup

### Manual Backup:
Double-click: `backup-data.bat`

Creates timestamped backup in: `C:\Backups\KorConstructions\`

### Important:
- Backup weekly
- Before major updates
- Before sharing with another laptop
- Copy backup to external drive monthly

---

## 🛠️ Technical Details

### Technology Stack:
- **Backend**: Spring Boot 3.1.5
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Storage**: JSON files (no database needed)
- **Language**: Java 17

### System Requirements:
- Windows 10/11 (also works on Mac/Linux)
- Java 17 or higher
- 50 MB disk space
- No internet required (except for cloud sync)

### API Endpoints:
```
GET    /api/customers
POST   /api/customers
PUT    /api/customers/{id}
DELETE /api/customers/{id}

GET    /api/receipts
GET    /api/receipts/customer/{id}
POST   /api/receipts
PUT    /api/receipts/{id}
DELETE /api/receipts/{id}

GET    /api/payments
POST   /api/payments
PUT    /api/payments/{id}
DELETE /api/payments/{id}
```

---

## 📖 User Guides

### For New Users:
1. `START-HERE.txt` - First thing to read
2. `WINDOWS-SETUP.md` - Installation guide

### For Sharing Data:
1. `SHARING-QUICK-START.txt` - Quick setup
2. `SHARED-DATA-GUIDE.md` - Comprehensive guide

### For Developers:
- Source code in `src/`
- Run with Maven: `mvn spring-boot:run`
- Build JAR: `mvn clean package`

---

## ⚠️ Important Notes

### Data Safety:
- All data stored in `data/` folder
- **Backup regularly!**
- Cloud sync recommended for safety

### Multi-Laptop Usage:
- ⚠️ **Never run on both laptops simultaneously**
- Close on Laptop 1 → Wait 10 seconds → Open on Laptop 2
- Prevents data corruption

### Printing:
- Use "Εκτύπωση" button in receipts/payments tabs
- Browser print dialog opens
- Professional format with company header

---

## 🔧 Troubleshooting

### Application won't start:
- Check Java is installed: `java -version`
- Verify JAR file exists in `target/` folder
- Try running `build-jar.bat`

### Browser doesn't open:
- Manually go to: http://localhost:8080
- Check port 8080 is not in use

### Data not syncing between laptops:
- Check cloud storage is running and synced
- Verify same folder path on both laptops
- Wait longer (30 seconds after closing app)

### Lost data:
- Check `C:\Backups\KorConstructions\`
- Check cloud storage website for old versions
- Right-click `data` folder → "Restore previous versions"

---

## 📝 Development

### Prerequisites:
- Java 17+
- Maven 3.6+

### Run Development Server:
```batch
mvn spring-boot:run
```

### Build Production JAR:
```batch
mvn clean package
```

### Project Structure:
```
src/
├── main/
│   ├── java/com/korconstructions/
│   │   ├── model/          # Data models
│   │   ├── repository/     # File storage
│   │   ├── service/        # Business logic
│   │   └── controller/     # REST API
│   └── resources/
│       ├── static/          # Frontend files
│       │   ├── css/
│       │   └── js/
│       └── application.properties
└── test/
```

---

## 🆘 Support

### Documentation:
- `START-HERE.txt` - Quick start
- `WINDOWS-SETUP.md` - Windows installation
- `SHARED-DATA-GUIDE.md` - Sharing between laptops
- `SHARING-QUICK-START.txt` - Quick sharing guide

### Common Files:
- `run-jar.bat` - Start application
- `backup-data.bat` - Backup data
- `setup-cloud-sync.bat` - Setup cloud sync
- `build-jar.bat` - Rebuild application

---

## 📄 License

Private use only.

---

## 🎯 Quick Reference

| Action | File to Use |
|--------|-------------|
| Start app | `run-jar.bat` |
| Setup sharing | `setup-cloud-sync.bat` |
| Backup data | `backup-data.bat` |
| Rebuild app | `build-jar.bat` |
| Read instructions | `START-HERE.txt` |

**URL**: http://localhost:8080

**Data location**: `./data/`

**Backups**: `C:\Backups\KorConstructions/`

---

Made for Kor Constructions Management
